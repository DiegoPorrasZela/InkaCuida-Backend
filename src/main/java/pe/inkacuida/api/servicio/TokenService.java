package pe.inkacuida.api.servicio;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import pe.inkacuida.api.dominio.Usuario;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final long horasValidez;

    public TokenService(JwtEncoder encoder, @Value("${app.jwt.horas-validez:168}") long horasValidez) {
        this.encoder = encoder;
        this.horasValidez = horasValidez;
    }

    /** JWT HS256 cuyo "sub" es el id del usuario. */
    public String generar(Usuario u) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("inkacuida-api")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(horasValidez, ChronoUnit.HOURS))
                .subject(String.valueOf(u.getId()))
                .claim("email", u.getEmail())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
