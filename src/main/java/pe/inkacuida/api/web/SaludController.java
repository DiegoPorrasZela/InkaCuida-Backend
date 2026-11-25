package pe.inkacuida.api.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.inkacuida.api.repositorio.UsuarioRepository;

/** Endpoint público para comprobar que la API y la base de datos responden. */
@RestController
public class SaludController {

    private final UsuarioRepository usuarios;

    public SaludController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/api/salud")
    public Map<String, Object> salud() {
        return Map.of("estado", "ok", "usuariosRegistrados", usuarios.count());
    }
}
