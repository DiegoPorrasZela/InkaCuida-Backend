package pe.inkacuida.api.repositorio;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.inkacuida.api.dominio.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
