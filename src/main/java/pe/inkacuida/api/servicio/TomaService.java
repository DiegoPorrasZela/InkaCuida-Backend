package pe.inkacuida.api.servicio;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.inkacuida.api.dominio.Medicamento;
import pe.inkacuida.api.dominio.ReglasNegocio;
import pe.inkacuida.api.dominio.Toma;
import pe.inkacuida.api.dto.TomaDto;
import pe.inkacuida.api.repositorio.MedicamentoRepository;
import pe.inkacuida.api.repositorio.TomaRepository;

/** HU02 / HU05 / HU06 – Recordatorios, confirmación de tomas e historial de adherencia. */
@Service
@Transactional
public class TomaService {

    private static final long MAX_DIAS_HISTORIAL = 366;

    private final TomaRepository tomas;
    private final MedicamentoRepository medicamentos;

    public TomaService(TomaRepository tomas, MedicamentoRepository medicamentos) {
        this.tomas = tomas;
        this.medicamentos = medicamentos;
    }

    /** Devuelve las tomas de hoy, creándolas a partir de los medicamentos vigentes si aún no existen. */
    public List<TomaDto> hoy(Long usuarioId) {
        LocalDate hoy = LocalDate.now(ReglasNegocio.ZONA);
        generarTomas(usuarioId, hoy);
        return tomas.buscarRango(usuarioId, hoy, hoy).stream().map(TomaDto::de).toList();
    }

    @Transactional(readOnly = true)
    public List<TomaDto> historial(Long usuarioId, LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) throw ApiException.invalido("La fecha 'desde' debe ser anterior a 'hasta'");
        if (ChronoUnit.DAYS.between(desde, hasta) > MAX_DIAS_HISTORIAL) {
            throw ApiException.invalido("El rango máximo del historial es de un año");
        }
        return tomas.buscarRango(usuarioId, desde, hasta).stream().map(TomaDto::de).toList();
    }

    @Transactional(readOnly = true)
    public TomaDto obtener(Long usuarioId, Long id) {
        return TomaDto.de(buscar(usuarioId, id));
    }

    public TomaDto confirmar(Long usuarioId, Long id) {
        Toma t = buscarPendiente(usuarioId, id);
        t.confirmar(ahora());
        return TomaDto.de(t);
    }

    public TomaDto omitir(Long usuarioId, Long id) {
        Toma t = buscarPendiente(usuarioId, id);
        t.omitir(ahora());
        return TomaDto.de(t);
    }

    public TomaDto posponer(Long usuarioId, Long id) {
        Toma t = buscarPendiente(usuarioId, id);
        t.posponer(ahora().plus(ReglasNegocio.MINUTOS_POSPONER, ChronoUnit.MINUTES));
        return TomaDto.de(t);
    }

    /** Crea una toma PENDIENTE por cada (medicamento vigente × horario) del día, si no existe. */
    public void generarTomas(Long usuarioId, LocalDate fecha) {
        for (Medicamento m : medicamentos.findByUsuarioIdAndActivoTrueOrderByIdAsc(usuarioId)) {
            if (!m.vigenteEn(fecha)) continue;
            for (String hora : m.getHorarios()) {
                if (!tomas.existsByMedicamentoIdAndFechaAndHora(m.getId(), fecha, hora)) {
                    tomas.save(new Toma(m, fecha, hora));
                }
            }
        }
        tomas.flush();
    }

    /** Instante actual con precisión de milisegundos (compatible con Date de JavaScript). */
    private static Instant ahora() {
        return Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }

    private Toma buscar(Long usuarioId, Long id) {
        return tomas.findByIdAndUsuarioId(id, usuarioId).orElseThrow(() -> ApiException.noEncontrado("Toma no encontrada"));
    }

    private Toma buscarPendiente(Long usuarioId, Long id) {
        Toma t = buscar(usuarioId, id);
        if (!t.isPendiente()) throw ApiException.conflicto("Esta toma ya fue registrada");
        return t;
    }
}
