package com.biblioteca.backend.repository;

import com.biblioteca.backend.entity.EstadoPrestamo;
import com.biblioteca.backend.entity.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    @Query(value = """
        SELECT p.id FROM Prestamo p
        LEFT JOIN p.usuario u
        WHERE (:usuarioId IS NULL OR u.id = :usuarioId)
        AND (:estado IS NULL OR p.estado = :estado)
        """,
        countQuery = """
        SELECT COUNT(DISTINCT p) FROM Prestamo p
        LEFT JOIN p.usuario u
        WHERE (:usuarioId IS NULL OR u.id = :usuarioId)
        AND (:estado IS NULL OR p.estado = :estado)
        """)
    Page<Long> findIdsByFilters(
        @Param("usuarioId") Long usuarioId,
        @Param("estado") EstadoPrestamo estado,
        Pageable pageable
    );

    @Query("""
        SELECT DISTINCT p FROM Prestamo p
        LEFT JOIN FETCH p.usuario u LEFT JOIN FETCH u.foto
        LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.libro l
        LEFT JOIN FETCH l.categoria LEFT JOIN FETCH l.portada LEFT JOIN FETCH l.autores
        WHERE p.id IN :ids
        """)
    List<Prestamo> findAllByIdInWithRelations(@Param("ids") List<Long> ids);

    @Query("""
        SELECT p FROM Prestamo p
        LEFT JOIN FETCH p.usuario u LEFT JOIN FETCH u.foto
        LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.libro l
        LEFT JOIN FETCH l.categoria LEFT JOIN FETCH l.portada LEFT JOIN FETCH l.autores
        WHERE p.id = :id
        """)
    Optional<Prestamo> findByIdWithRelations(@Param("id") Long id);


    boolean existsByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);
    boolean existsByDetallesLibroIdAndEstado(Long libroId, EstadoPrestamo estado);

    long countByEstado(EstadoPrestamo estado);

    @Query("""
        SELECT COUNT(p) FROM Prestamo p
        WHERE p.estado = com.biblioteca.backend.entity.EstadoPrestamo.ACTIVO
        AND p.fechaDevolucionLimite < :ahora
        """)
    long countPrestamosActivosVencidos(@Param("ahora") Instant ahora);

    @Query("""
        SELECT COUNT(p) FROM Prestamo p
        WHERE p.estado = com.biblioteca.backend.entity.EstadoPrestamo.ACTIVO
        AND p.fechaDevolucionLimite BETWEEN :ahora AND :limite
        """)
    long countPrestamosActivosPorVencer(
        @Param("ahora") Instant ahora,
        @Param("limite") Instant limite
    );

    long countByEstadoAndFechaDevolucionRealBetween(
        EstadoPrestamo estado,
        Instant desde,
        Instant hasta
    );

    @Query(value = """
        SELECT p.id FROM Prestamo p
        WHERE p.estado = com.biblioteca.backend.entity.EstadoPrestamo.ACTIVO
        AND p.fechaDevolucionLimite < :ahora
        ORDER BY p.fechaDevolucionLimite ASC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT p) FROM Prestamo p
        WHERE p.estado = com.biblioteca.backend.entity.EstadoPrestamo.ACTIVO
        AND p.fechaDevolucionLimite < :ahora
        """)
    Page<Long> findIdsPrestamosActivosVencidos(@Param("ahora") Instant ahora, Pageable pageable);

    @Query(value = """
        SELECT p.id FROM Prestamo p
        WHERE p.estado = com.biblioteca.backend.entity.EstadoPrestamo.ACTIVO
        AND p.fechaDevolucionLimite BETWEEN :ahora AND :limite
        ORDER BY p.fechaDevolucionLimite ASC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT p) FROM Prestamo p
        WHERE p.estado = com.biblioteca.backend.entity.EstadoPrestamo.ACTIVO
        AND p.fechaDevolucionLimite BETWEEN :ahora AND :limite
        """)
    Page<Long> findIdsPrestamosActivosPorVencer(
        @Param("ahora") Instant ahora,
        @Param("limite") Instant limite,
        Pageable pageable
    );

    @Query("""
        SELECT COUNT(p) > 0 FROM Prestamo p
        WHERE p.usuario.id = :usuarioId
        AND (p.estado = com.biblioteca.backend.entity.EstadoPrestamo.ATRASADO
             OR (p.estado = com.biblioteca.backend.entity.EstadoPrestamo.ACTIVO AND p.fechaDevolucionLimite < :ahora))
        """)
    boolean tienePrestamosVencidos(@Param("usuarioId") Long usuarioId, @Param("ahora") Instant ahora);

    @Query("""
        SELECT COALESCE(SUM(d.cantidad), 0) FROM Prestamo p
        JOIN p.detalles d
        WHERE p.usuario.id = :usuarioId
        AND p.estado IN (com.biblioteca.backend.entity.EstadoPrestamo.ACTIVO, com.biblioteca.backend.entity.EstadoPrestamo.ATRASADO)
        """)
    long countTotalLibrosEnPoseccion(@Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT COUNT(p) > 0 FROM Prestamo p
        JOIN p.detalles d
        WHERE p.usuario.id = :usuarioId
        AND d.libro.id = :libroId
        AND p.estado IN (com.biblioteca.backend.entity.EstadoPrestamo.ACTIVO, com.biblioteca.backend.entity.EstadoPrestamo.ATRASADO)
        """)
    boolean tieneLibroPrestadoActivo(@Param("usuarioId") Long usuarioId, @Param("libroId") Long libroId);

    @Query("""
        SELECT DISTINCT d.libro.id FROM Prestamo p
        JOIN p.detalles d
        WHERE p.usuario.id = :usuarioId
        AND p.estado IN (com.biblioteca.backend.entity.EstadoPrestamo.ACTIVO, com.biblioteca.backend.entity.EstadoPrestamo.ATRASADO)
        """)
    List<Long> findLibrosIdsPrestadosActivos(@Param("usuarioId") Long usuarioId);
}
