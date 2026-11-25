package pe.inkacuida.api.repositorio;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.inkacuida.api.dominio.Cuidador;

public interface CuidadorRepository extends JpaRepository<Cuidador, Long> {
    List<Cuidador> findByUsuarioIdOrderByIdAsc(Long usuarioId);
    Optional<Cuidador> findByIdAndUsuarioId(Long id, Long usuarioId);
}
