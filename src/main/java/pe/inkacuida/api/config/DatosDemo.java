package pe.inkacuida.api.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.inkacuida.api.dominio.*;
import pe.inkacuida.api.repositorio.CuidadorRepository;
import pe.inkacuida.api.repositorio.MedicamentoRepository;
import pe.inkacuida.api.repositorio.TomaRepository;
import pe.inkacuida.api.repositorio.UsuarioRepository;

/**
 * Pacientes de demostración (contraseña 123456). Cada uno se crea solo si aún no existe.
 * Los historiales son sintéticos: no corresponden a pacientes reales.
 *
 *  - María González  (68, Lima):     hipertensión + diabetes + colesterol; buena racha; Metformina por agotarse.
 *  - Jorge Quispe    (74, Arequipa): adherencia irregular; dosis vespertinas 18:30 / 19:00; alerta al cuidador.
 *  - Rosa Huamán     (45, Cusco):    adherencia excelente; tratamiento nuevo que empieza hoy con toma a las 18:45.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo", havingValue = "true")
public class DatosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemo.class);
    private static final String PASSWORD_DEMO = "123456";

    /** Decide el estado de una toma histórica: índice del medicamento, días hacia atrás y hora. */
    @FunctionalInterface
    private interface Patron {
        EstadoToma estado(int medicamento, int diasAtras, String hora);
    }

    private final UsuarioRepository usuarios;
    private final MedicamentoRepository medicamentos;
    private final TomaRepository tomas;
    private final CuidadorRepository cuidadores;
    private final PasswordEncoder passwordEncoder;

    public DatosDemo(UsuarioRepository usuarios, MedicamentoRepository medicamentos, TomaRepository tomas,
                     CuidadorRepository cuidadores, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.medicamentos = medicamentos;
        this.tomas = tomas;
        this.cuidadores = cuidadores;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        LocalDate hoy = LocalDate.now(ReglasNegocio.ZONA);
        LocalTime ahora = LocalTime.now(ReglasNegocio.ZONA);
        maria(hoy, ahora);
        jorge(hoy, ahora);
        rosa(hoy, ahora);
    }

    // ------------------------------------------------------------------ pacientes

    private void maria(LocalDate hoy, LocalTime ahora) {
        Usuario u = paciente("María González", "maria@inkacuida.pe", LocalDate.of(1958, 3, 14), "Lima");
        if (u == null) return;
        List<Medicamento> meds = List.of(
                medicamento(u, "Losartán", "50 mg", Frecuencia.UNA_VEZ_DIA, List.of("08:00"),
                        hoy.minusDays(20), null, "Tomar en ayunas con un vaso de agua", 24),
                medicamento(u, "Metformina", "850 mg", Frecuencia.UNA_VEZ_DIA, List.of("13:00"),
                        hoy.minusDays(20), hoy.plusDays(60), "Tomar con el almuerzo", 4),
                medicamento(u, "Atorvastatina", "20 mg", Frecuencia.UNA_VEZ_DIA, List.of("21:00"),
                        hoy.minusDays(20), null, "Tomar antes de dormir", 30));
        cuidadores.save(new Cuidador(u, "Carlos González", "carlos@correo.com", "Hijo", true));

        // Últimos 7 días completos (racha); antes, algunas omisiones.
        historial(meds, hoy, 14, (m, d, h) -> {
            if (d <= 7) return EstadoToma.TOMADA;
            long r = (m * 7L + d * 3L) % 10;
            return r == 0 ? EstadoToma.OMITIDA : r == 1 ? EstadoToma.PENDIENTE : EstadoToma.TOMADA;
        });
        tomasDeHoy(meds, hoy, ahora, Set.of());
        log.info("Paciente demo creado: maria@inkacuida.pe");
    }

    private void jorge(LocalDate hoy, LocalTime ahora) {
        Usuario u = paciente("Jorge Quispe Mamani", "jorge@inkacuida.pe", LocalDate.of(1952, 7, 2), "Arequipa");
        if (u == null) return;
        List<Medicamento> meds = List.of(
                medicamento(u, "Enalapril", "10 mg", Frecuencia.DOS_VECES_DIA, List.of("08:00", "18:30"),
                        hoy.minusDays(30), null, "Controlar la presión antes de tomarlo", 8),
                medicamento(u, "Metformina", "500 mg", Frecuencia.TRES_VECES_DIA, List.of("07:00", "13:00", "19:00"),
                        hoy.minusDays(30), null, "Tomar con las comidas", 45),
                medicamento(u, "Ácido acetilsalicílico", "100 mg", Frecuencia.UNA_VEZ_DIA, List.of("12:00"),
                        hoy.minusDays(30), null, "Tomar después del almuerzo", null));
        cuidadores.save(new Cuidador(u, "Lucía Quispe", "lucia.quispe@correo.com", "Hija", true));
        cuidadores.save(new Cuidador(u, "Pedro Quispe", "pedro.quispe@correo.com", "Hijo", false));

        // Adherencia irregular: olvida más las dosis de la tarde/noche.
        historial(meds, hoy, 14, (m, d, h) -> {
            long r = (m * 5L + d * 7L + h.hashCode()) % 10;
            if (r < 0) r = -r;
            boolean tarde = h.compareTo("18:00") >= 0;
            if (r == 0 || (tarde && r < 3)) return EstadoToma.PENDIENTE;
            if (r == 1) return EstadoToma.OMITIDA;
            return EstadoToma.TOMADA;
        });
        // Hoy no confirmó la Metformina de las 13:00 -> alerta a su hija tras 30 min.
        tomasDeHoy(meds, hoy, ahora, Set.of("Metformina@13:00"));
        log.info("Paciente demo creado: jorge@inkacuida.pe");
    }

    private void rosa(LocalDate hoy, LocalTime ahora) {
        Usuario u = paciente("Rosa Huamán Torres", "rosa@inkacuida.pe", LocalDate.of(1981, 11, 9), "Cusco");
        if (u == null) return;
        List<Medicamento> meds = List.of(
                medicamento(u, "Levotiroxina", "50 mcg", Frecuencia.UNA_VEZ_DIA, List.of("06:30"),
                        hoy.minusDays(60), null, "En ayunas, 30 minutos antes del desayuno", 40),
                medicamento(u, "Montelukast", "10 mg", Frecuencia.UNA_VEZ_DIA, List.of("21:00"),
                        hoy.minusDays(14), null, "Tomar por la noche", 20),
                // Tratamiento nuevo: empieza hoy, dura 14 días.
                medicamento(u, "Omeprazol", "20 mg", Frecuencia.UNA_VEZ_DIA, List.of("18:45"),
                        hoy, hoy.plusDays(13), "Tomar antes de la cena", 14));
        cuidadores.save(new Cuidador(u, "Miguel Torres", "miguel.torres@correo.com", "Esposo", false));

        // Adherencia excelente: una sola omisión en dos semanas.
        historial(meds, hoy, 14, (m, d, h) -> (m == 1 && d == 12) ? EstadoToma.OMITIDA : EstadoToma.TOMADA);
        tomasDeHoy(meds, hoy, ahora, Set.of());
        log.info("Paciente demo creado: rosa@inkacuida.pe");
    }

    // ------------------------------------------------------------------ utilidades

    /** Crea el usuario o devuelve null si ya existe (así no se duplican datos al reiniciar). */
    private Usuario paciente(String nombre, String email, LocalDate nacimiento, String ciudad) {
        if (usuarios.existsByEmailIgnoreCase(email)) return null;
        Usuario u = new Usuario(nombre, email, passwordEncoder.encode(PASSWORD_DEMO), true);
        u.setFechaNacimiento(nacimiento);
        u.setCiudad(ciudad);
        return usuarios.save(u);
    }

    private Medicamento medicamento(Usuario u, String nombre, String dosis, Frecuencia frecuencia, List<String> horarios,
                                    LocalDate inicio, LocalDate fin, String indicaciones, Integer unidades) {
        Medicamento m = new Medicamento(u);
        m.setNombre(nombre);
        m.setDosis(dosis);
        m.setFrecuencia(frecuencia);
        m.setHorarios(horarios);
        m.setFechaInicio(inicio);
        m.setFechaFin(fin);
        m.setIndicaciones(indicaciones);
        m.setUnidadesDisponibles(unidades);
        return medicamentos.save(m);
    }

    /** Tomas de los días anteriores (sin incluir hoy) según el patrón del paciente. */
    private void historial(List<Medicamento> meds, LocalDate hoy, int dias, Patron patron) {
        for (int d = dias; d >= 1; d--) {
            LocalDate fecha = hoy.minusDays(d);
            for (int i = 0; i < meds.size(); i++) {
                Medicamento m = meds.get(i);
                if (!m.vigenteEn(fecha)) continue;
                for (String hora : m.getHorarios()) {
                    Toma t = new Toma(m, fecha, hora);
                    EstadoToma estado = patron.estado(i, d, hora);
                    if (estado != EstadoToma.PENDIENTE) {
                        t.registrarHistorico(estado, instante(fecha, hora, 3 + (d * 7 + i * 5) % 22));
                    }
                    tomas.save(t);
                }
            }
        }
    }

    /** Tomas de hoy: las que ya pasaron figuran confirmadas, salvo las indicadas como "Nombre@HH:mm". */
    private void tomasDeHoy(List<Medicamento> meds, LocalDate hoy, LocalTime ahora, Set<String> sinConfirmar) {
        for (Medicamento m : meds) {
            if (!m.vigenteEn(hoy)) continue;
            for (String hora : m.getHorarios()) {
                Toma t = new Toma(m, hoy, hora);
                boolean yaPaso = LocalTime.parse(hora).isBefore(ahora);
                if (yaPaso && !sinConfirmar.contains(m.getNombre() + "@" + hora)) {
                    t.registrarHistorico(EstadoToma.TOMADA, instante(hoy, hora, 6));
                }
                tomas.save(t);
            }
        }
    }

    private static java.time.Instant instante(LocalDate fecha, String hora, int minutosDespues) {
        return ZonedDateTime.of(fecha, LocalTime.parse(hora), ReglasNegocio.ZONA).plusMinutes(minutosDespues).toInstant();
    }
}
