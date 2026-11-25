package pe.inkacuida.api.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pe.inkacuida.api.dto.MedicamentoDto;
import pe.inkacuida.api.dto.MedicamentoRequest;
import pe.inkacuida.api.servicio.MedicamentoService;

@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoController {

    private final MedicamentoService servicio;

    public MedicamentoController(MedicamentoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<MedicamentoDto> listar(@AuthenticationPrincipal Jwt jwt) {
        return servicio.listar(UsuarioActual.id(jwt));
    }

    @GetMapping("/{id}")
    public MedicamentoDto obtener(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return servicio.obtener(UsuarioActual.id(jwt), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicamentoDto crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MedicamentoRequest req) {
        return servicio.crear(UsuarioActual.id(jwt), req);
    }

    @PutMapping("/{id}")
    public MedicamentoDto actualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                     @Valid @RequestBody MedicamentoRequest req) {
        return servicio.actualizar(UsuarioActual.id(jwt), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        servicio.eliminar(UsuarioActual.id(jwt), id);
    }
}
