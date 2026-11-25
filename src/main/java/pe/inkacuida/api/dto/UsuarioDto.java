package pe.inkacuida.api.dto;

import java.time.LocalDate;
import pe.inkacuida.api.dominio.Usuario;

public record UsuarioDto(Long id, String nombre, String email, LocalDate fechaNacimiento, String ciudad,
                         boolean consentimientoAceptado) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombre(), u.getEmail(), u.getFechaNacimiento(), u.getCiudad(),
                u.isConsentimientoAceptado());
    }
}
