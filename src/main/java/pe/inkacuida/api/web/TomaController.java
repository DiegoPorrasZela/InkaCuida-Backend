package pe.inkacuida.api.web;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pe.inkacuida.api.dto.TomaDto;
import pe.inkacuida.api.servicio.TomaService;

@RestController
@RequestMapping("/api/tomas")
public class TomaController {

    private final TomaService servicio;

    public TomaController(TomaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/hoy")
    public List<TomaDto> hoy(@AuthenticationPrincipal Jwt jwt) {
        return servicio.hoy(UsuarioActual.id(jwt));
    }

    @GetMapping
    public List<TomaDto> historial(@AuthenticationPrincipal Jwt jwt,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.historial(UsuarioActual.id(jwt), desde, hasta);
    }

    @GetMapping("/{id}")
    public TomaDto obtener(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return servicio.obtener(UsuarioActual.id(jwt), id);
    }

    @PatchMapping("/{id}/confirmar")
    public TomaDto confirmar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return servicio.confirmar(UsuarioActual.id(jwt), id);
    }

    @PatchMapping("/{id}/omitir")
    public TomaDto omitir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return servicio.omitir(UsuarioActual.id(jwt), id);
    }

    @PatchMapping("/{id}/posponer")
    public TomaDto posponer(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return servicio.posponer(UsuarioActual.id(jwt), id);
    }
}
