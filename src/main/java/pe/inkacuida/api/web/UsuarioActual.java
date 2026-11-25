package pe.inkacuida.api.web;

import org.springframework.security.oauth2.jwt.Jwt;

/** El id del usuario sale siempre del token (claim "sub"), nunca del body ni de la URL. */
final class UsuarioActual {
    private UsuarioActual() {}

    static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
