package com.biblioteca.backend.repository;

import com.biblioteca.backend.dto.response.DashboardRankingResponse;
import com.biblioteca.backend.entity.DetallePrestamo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetallePrestamoRepository extends JpaRepository<DetallePrestamo, Long> {

    @Query("""
        SELECT new com.biblioteca.backend.dto.response.DashboardRankingResponse(
            l.id, l.titulo, SUM(d.cantidad)
        )
        FROM DetallePrestamo d JOIN d.libro l
        GROUP BY l.id, l.titulo
        ORDER BY SUM(d.cantidad) DESC, l.titulo ASC
        """)
    List<DashboardRankingResponse> findLibrosMasPrestados(Pageable pageable);

    @Query("""
        SELECT new com.biblioteca.backend.dto.response.DashboardRankingResponse(
            u.id, u.nombre, SUM(d.cantidad)
        )
        FROM DetallePrestamo d JOIN d.prestamo p JOIN p.usuario u
        GROUP BY u.id, u.nombre
        ORDER BY SUM(d.cantidad) DESC, u.nombre ASC
        """)
    List<DashboardRankingResponse> findLectoresMasActivos(Pageable pageable);
}
