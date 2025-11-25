package co.edu.javeriana.prestamos.repository;

import co.edu.javeriana.prestamos.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {
    @Query("SELECT p FROM Prestamo p WHERE p.id_usuario = :usuarioId AND (p.id_estado_prestamo = 1 OR p.id_estado_prestamo = 2 OR p.id_estado_prestamo = 4)")
    List<Prestamo> findActivosYVencidosByUsuarioId(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT p FROM Prestamo p WHERE p.id_estado_prestamo = :estado")
    List<Prestamo> findByEstado(@Param("estado") Integer estado);

    @Query("SELECT p FROM Prestamo p WHERE p.id_estado_prestamo = 2 AND p.fecha_devolucion_esperada < CURRENT_TIMESTAMP")
    List<Prestamo> findOverdueNow();

    @Query("SELECT p FROM Prestamo p WHERE p.id_estado_prestamo = 2 AND p.fecha_devolucion_esperada BETWEEN :from AND :to")
    List<Prestamo> findActiveDueBetween(@Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to);
}
