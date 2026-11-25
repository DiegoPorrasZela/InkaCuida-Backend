package pe.inkacuida.api.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pe.inkacuida.api.dto.CuidadorDto;
import pe.inkacuida.api.dto.CuidadorRequest;
import pe.inkacuida.api.servicio.CuidadorService;

@RestController
@RequestMapping("/api/cuidadores")
public class CuidadorController {

    private final CuidadorService servicio;

    public CuidadorController(CuidadorService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<CuidadorDto> listar(@AuthenticationPrincipal Jwt jwt) {
        return servicio.listar(UsuarioActual.id(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuidadorDto agregar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CuidadorRequest req) {
        return servicio.agregar(UsuarioActual.id(jwt), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        servicio.eliminar(UsuarioActual.id(jwt), id);
    }
}
