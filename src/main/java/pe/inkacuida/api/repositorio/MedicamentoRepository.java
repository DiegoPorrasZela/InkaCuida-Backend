package pe.inkacuida.api.repositorio;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.inkacuida.api.dominio.Medicamento;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    List<Medicamento> findByUsuarioIdAndActivoTrueOrderByIdAsc(Long usuarioId);
    Optional<Medicamento> findByIdAndUsuarioIdAndActivoTrue(Long id, Long usuarioId);
}
