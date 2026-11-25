package pe.inkacuida.api.servicio;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.inkacuida.api.dominio.EstadoToma;
import pe.inkacuida.api.dominio.Medicamento;
import pe.inkacuida.api.dominio.ReglasNegocio;
import pe.inkacuida.api.dto.MedicamentoDto;
import pe.inkacuida.api.dto.MedicamentoRequest;
import pe.inkacuida.api.repositorio.MedicamentoRepository;
import pe.inkacuida.api.repositorio.TomaRepository;
import pe.inkacuida.api.repositorio.UsuarioRepository;

/** HU01 – Registro de tratamiento y carnet digital. */
@Service
@Transactional
public class MedicamentoService {

    private final MedicamentoRepository medicamentos;
    private final UsuarioRepository usuarios;
    private final TomaRepository tomas;
    private final TomaService tomaService;

    public MedicamentoService(MedicamentoRepository medicamentos, UsuarioRepository usuarios, TomaRepository tomas,
                              TomaService tomaService) {
        this.medicamentos = medicamentos;
        this.usuarios = usuarios;
        this.tomas = tomas;
        this.tomaService = tomaService;
    }

    @Transactional(readOnly = true)
    public List<MedicamentoDto> listar(Long usuarioId) {
        return medicamentos.findByUsuarioIdAndActivoTrueOrderByIdAsc(usuarioId).stream().map(MedicamentoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public MedicamentoDto obtener(Long usuarioId, Long id) {
        return MedicamentoDto.de(buscar(usuarioId, id));
    }

    public MedicamentoDto crear(Long usuarioId, MedicamentoRequest req) {
        validar(req);
        Medicamento m = new Medicamento(usuarios.getReferenceById(usuarioId));
        aplicar(m, req);
        m = medicamentos.save(m);
        tomaService.generarTomas(usuarioId, hoy());
        return MedicamentoDto.de(m);
    }

    public MedicamentoDto actualizar(Long usuarioId, Long id, MedicamentoRequest req) {
        validar(req);
        Medicamento m = buscar(usuarioId, id);
        aplicar(m, req);
        medicamentos.saveAndFlush(m);
        // Los horarios pueden haber cambiado: se regeneran las tomas sin responder desde hoy.
        tomas.borrarDesde(id, EstadoToma.PENDIENTE, hoy());
        tomaService.generarTomas(usuarioId, hoy());
        return MedicamentoDto.de(medicamentos.findById(id).orElseThrow());
    }

    /** Borrado lógico: deja de generar recordatorios pero conserva el historial. */
    public void eliminar(Long usuarioId, Long id) {
        Medicamento m = buscar(usuarioId, id);
        m.setActivo(false);
        medicamentos.saveAndFlush(m);
        tomas.borrarDesde(id, EstadoToma.PENDIENTE, hoy());
    }

    private Medicamento buscar(Long usuarioId, Long id) {
        return medicamentos.findByIdAndUsuarioIdAndActivoTrue(id, usuarioId)
                .orElseThrow(() -> ApiException.noEncontrado("Medicamento no encontrado"));
    }

    private void validar(MedicamentoRequest req) {
        if (req.fechaFin() != null && req.fechaFin().isBefore(req.fechaInicio())) {
            throw ApiException.invalido("La fecha de fin no puede ser anterior a la de inicio");
        }
        if (new HashSet<>(req.horarios()).size() != req.horarios().size()) {
            throw ApiException.invalido("Hay horarios repetidos");
        }
    }

    /** Copia los datos tal como los ingresó el paciente: no se corrige ni recalcula la dosis. */
    private void aplicar(Medicamento m, MedicamentoRequest req) {
        m.setNombre(req.nombre().trim());
        m.setDosis(req.dosis().trim());
        m.setFrecuencia(req.frecuencia());
        m.setHorarios(req.horarios().stream().sorted().toList());
        m.setFechaInicio(req.fechaInicio());
        m.setFechaFin(req.fechaFin());
        m.setIndicaciones(req.indicaciones() == null || req.indicaciones().isBlank() ? null : req.indicaciones().trim());
        m.setUnidadesDisponibles(req.unidadesDisponibles());
    }

    private static LocalDate hoy() {
        return LocalDate.now(ReglasNegocio.ZONA);
    }
}
