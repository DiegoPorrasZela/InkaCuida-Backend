package pe.inkacuida.api.repositorio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.inkacuida.api.dominio.EstadoToma;
import pe.inkacuida.api.dominio.Toma;

public interface TomaRepository extends JpaRepository<Toma, Long> {

    @Query("select t from Toma t join fetch t.medicamento "
         + "where t.usuario.id = :usuarioId and t.fecha between :desde and :hasta order by t.fecha, t.hora")
    List<Toma> buscarRango(@Param("usuarioId") Long usuarioId,
                           @Param("desde") LocalDate desde,
                           @Param("hasta") LocalDate hasta);

    Optional<Toma> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByMedicamentoIdAndFechaAndHora(Long medicamentoId, LocalDate fecha, String hora);

    /** Al eliminar o editar un medicamento se borran sus tomas sin responder desde una fecha; el historial se conserva. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Toma t where t.medicamento.id = :medicamentoId and t.estado = :estado and t.fecha >= :desde")
    int borrarDesde(@Param("medicamentoId") Long medicamentoId,
                    @Param("estado") EstadoToma estado,
                    @Param("desde") LocalDate desde);
}
