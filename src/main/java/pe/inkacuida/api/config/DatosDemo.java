package pe.inkacuida.api.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
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
 * Crea la paciente de demostración (maria@inkacuida.pe / 123456) con 3 tratamientos
 * y 14 días de historial sintético, solo si aún no existe.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo", havingValue = "true")
public class DatosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemo.class);
    private static final String EMAIL = "maria@inkacuida.pe";

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
        if (usuarios.existsByEmailIgnoreCase(EMAIL)) return;

        LocalDate hoy = LocalDate.now(ReglasNegocio.ZONA);
        Usuario maria = new Usuario("María González", EMAIL, passwordEncoder.encode("123456"), true);
        maria.setFechaNacimiento(LocalDate.of(1958, 3, 14));
        maria.setCiudad("Lima");
        maria = usuarios.save(maria);

        List<Medicamento> meds = List.of(
                medicamento(maria, "Losartán", "50 mg", "08:00", hoy.minusDays(20), null, "Tomar en ayunas con un vaso de agua", 24),
                medicamento(maria, "Metformina", "850 mg", "13:00", hoy.minusDays(20), hoy.plusDays(60), "Tomar con el almuerzo", 4),
                medicamento(maria, "Atorvastatina", "20 mg", "21:00", hoy.minusDays(20), null, "Tomar antes de dormir", 30));

        cuidadores.save(new Cuidador(maria, "Carlos González", "carlos@correo.com", "Hijo", true));

        // Historial sintético: últimos 7 días completos (racha) y días anteriores con algunas omisiones.
        for (int d = 14; d >= 1; d--) {
            LocalDate fecha = hoy.minusDays(d);
            for (Medicamento m : meds) {
                for (String hora : m.getHorarios()) {
                    EstadoToma estado = EstadoToma.TOMADA;
                    if (d > 7) {
                        long r = (m.getId() * 7 + d * 3L) % 10;
                        estado = r == 0 ? EstadoToma.OMITIDA : r == 1 ? EstadoToma.PENDIENTE : EstadoToma.TOMADA;
                    }
                    Toma t = new Toma(m, fecha, hora);
                    if (estado != EstadoToma.PENDIENTE) {
                        t.registrarHistorico(estado, ZonedDateTime.of(fecha, LocalTime.parse(hora), ReglasNegocio.ZONA)
                                .plusMinutes(8).toInstant());
                    }
                    tomas.save(t);
                }
            }
        }

        // Hoy: si ya pasaron las 08:00 en Lima, la toma de Losartán figura como confirmada.
        Toma primera = new Toma(meds.get(0), hoy, "08:00");
        if (LocalTime.now(ReglasNegocio.ZONA).isAfter(LocalTime.of(8, 0))) {
            primera.registrarHistorico(EstadoToma.TOMADA,
                    ZonedDateTime.of(hoy, LocalTime.of(8, 5), ReglasNegocio.ZONA).toInstant());
        }
        tomas.save(primera);

        log.info("Datos de demostración creados: {} / 123456", EMAIL);
    }

    private Medicamento medicamento(Usuario u, String nombre, String dosis, String hora, LocalDate inicio, LocalDate fin,
                                    String indicaciones, int unidades) {
        Medicamento m = new Medicamento(u);
        m.setNombre(nombre);
        m.setDosis(dosis);
        m.setFrecuencia(Frecuencia.UNA_VEZ_DIA);
        m.setHorarios(List.of(hora));
        m.setFechaInicio(inicio);
        m.setFechaFin(fin);
        m.setIndicaciones(indicaciones);
        m.setUnidadesDisponibles(unidades);
        return medicamentos.save(m);
    }
}
