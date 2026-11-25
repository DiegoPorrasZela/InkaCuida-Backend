package pe.inkacuida.api.servicio;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.inkacuida.api.dominio.Cuidador;
import pe.inkacuida.api.dto.CuidadorDto;
import pe.inkacuida.api.dto.CuidadorRequest;
import pe.inkacuida.api.repositorio.CuidadorRepository;
import pe.inkacuida.api.repositorio.UsuarioRepository;

/** Cuidadores autorizados por el paciente (seguimiento familiar). */
@Service
@Transactional
public class CuidadorService {

    private final CuidadorRepository cuidadores;
    private final UsuarioRepository usuarios;

    public CuidadorService(CuidadorRepository cuidadores, UsuarioRepository usuarios) {
        this.cuidadores = cuidadores;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<CuidadorDto> listar(Long usuarioId) {
        return cuidadores.findByUsuarioIdOrderByIdAsc(usuarioId).stream().map(CuidadorDto::de).toList();
    }

    public CuidadorDto agregar(Long usuarioId, CuidadorRequest req) {
        String parentesco = req.parentesco() == null || req.parentesco().isBlank() ? "Familiar" : req.parentesco().trim();
        Cuidador c = new Cuidador(usuarios.getReferenceById(usuarioId), req.nombre().trim(),
                req.email().trim().toLowerCase(), parentesco, req.recibeAlertas());
        return CuidadorDto.de(cuidadores.save(c));
    }

    public void eliminar(Long usuarioId, Long id) {
        Cuidador c = cuidadores.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> ApiException.noEncontrado("Cuidador no encontrado"));
        cuidadores.delete(c);
    }
}
