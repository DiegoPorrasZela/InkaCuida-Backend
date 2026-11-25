package pe.inkacuida.api.servicio;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.inkacuida.api.dominio.Usuario;
import pe.inkacuida.api.dto.AuthResponse;
import pe.inkacuida.api.dto.LoginRequest;
import pe.inkacuida.api.dto.RegistroRequest;
import pe.inkacuida.api.dto.UsuarioDto;
import pe.inkacuida.api.repositorio.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarios.findByEmailIgnoreCase(req.email().trim())
                .filter(x -> passwordEncoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
        return new AuthResponse(tokens.generar(u), UsuarioDto.de(u));
    }

    /** Registro con consentimiento informado obligatorio (Ley N.° 29733). */
    @Transactional
    public AuthResponse registrar(RegistroRequest req) {
        if (!req.consentimientoAceptado()) {
            throw ApiException.invalido("Debes aceptar el consentimiento de datos");
        }
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo");
        }
        Usuario u = usuarios.save(new Usuario(req.nombre().trim(), email, passwordEncoder.encode(req.password()), true));
        return new AuthResponse(tokens.generar(u), UsuarioDto.de(u));
    }
}
