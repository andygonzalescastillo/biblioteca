package com.biblioteca.backend.repository;

import com.biblioteca.backend.entity.Libro;
import com.biblioteca.backend.dto.response.DashboardLibroInventarioResponse;
import com.biblioteca.backend.dto.response.DashboardRankingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    @Query(value = """
        SELECT l.id FROM Libro l
        LEFT JOIN l.categoria c
        WHERE (CAST(:buscar AS string) IS NULL
        OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
        OR LOWER(l.isbn) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
        OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
        OR EXISTS (
            SELECT 1 FROM l.autores autorBusqueda
            WHERE LOWER(autorBusqueda.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
        ))
        AND (:estado IS NULL OR l.estado = :estado)
        AND (:categoriaId IS NULL OR c.id = :categoriaId)
        AND (:autorId IS NULL OR EXISTS (
            SELECT 1 FROM l.autores autorFiltro
            WHERE autorFiltro.id = :autorId
        ))
        """,
        countQuery = """
        SELECT COUNT(l) FROM Libro l
        LEFT JOIN l.categoria c
        WHERE (CAST(:buscar AS string) IS NULL
        OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
        OR LOWER(l.isbn) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
        OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
        OR EXISTS (
            SELECT 1 FROM l.autores autorBusqueda
            WHERE LOWER(autorBusqueda.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
        ))
        AND (:estado IS NULL OR l.estado = :estado)
        AND (:categoriaId IS NULL OR c.id = :categoriaId)
        AND (:autorId IS NULL OR EXISTS (
            SELECT 1 FROM l.autores autorFiltro
            WHERE autorFiltro.id = :autorId
        ))
        """)
    Page<Long> findIdsByFilters(
        @Param("buscar") String buscar,
        @Param("estado") Boolean estado,
        @Param("categoriaId") Long categoriaId,
        @Param("autorId") Long autorId,
        Pageable pageable
    );

    @Query("""
        SELECT DISTINCT l FROM Libro l
        LEFT JOIN FETCH l.categoria
        LEFT JOIN FETCH l.portada
        LEFT JOIN FETCH l.autores
        WHERE l.id IN :ids
        """)
    List<Libro> findAllByIdInWithRelations(@Param("ids") List<Long> ids);

    @Query("""
        SELECT l FROM Libro l
        LEFT JOIN FETCH l.categoria
        LEFT JOIN FETCH l.portada
        LEFT JOIN FETCH l.autores
        WHERE l.id = :id
        """)
    Optional<Libro> findByIdWithRelations(@Param("id") Long id);

    @Query("""
        SELECT l FROM Libro l
        LEFT JOIN FETCH l.categoria
        LEFT JOIN FETCH l.portada
        LEFT JOIN FETCH l.autores
        WHERE l.isbn = :isbn
        """)
    Optional<Libro> findByIsbnWithRelations(@Param("isbn") String isbn);

    boolean existsByIsbn(String isbn);

    boolean existsByCategoriaId(Long categoriaId);
    boolean existsByAutoresId(Long autorId);

    boolean existsByCategoriaIdAndEstadoTrue(Long categoriaId);
    boolean existsByAutoresIdAndEstadoTrue(Long autorId);

    @Query("""
        SELECT new com.biblioteca.backend.dto.response.DashboardLibroInventarioResponse(
            l.id, l.titulo, l.stock, c.nombre
        )
        FROM Libro l JOIN l.categoria c
        WHERE l.estado = true AND l.stock <= :stockMaximo
        ORDER BY l.stock ASC, l.titulo ASC
        """)
    List<DashboardLibroInventarioResponse> findAlertasInventario(
        @Param("stockMaximo") Integer stockMaximo,
        Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(l.stock), 0) FROM Libro l WHERE l.estado = true")
    long sumStockActivo();

    long countByEstadoTrueAndStock(Integer stock);

    long countByEstadoTrue();

    @Query("""
        SELECT new com.biblioteca.backend.dto.response.DashboardRankingResponse(
            c.id, c.nombre, COUNT(l.id)
        )
        FROM Libro l JOIN l.categoria c
        WHERE l.estado = true
        GROUP BY c.id, c.nombre
        ORDER BY COUNT(l.id) DESC, c.nombre ASC
        """)
    List<DashboardRankingResponse> findCategoriasConMasLibros(Pageable pageable);

    @Query("""
        SELECT new com.biblioteca.backend.dto.response.DashboardRankingResponse(
            a.id, a.nombre, COUNT(l.id)
        )
        FROM Libro l JOIN l.autores a
        WHERE l.estado = true
        GROUP BY a.id, a.nombre
        ORDER BY COUNT(l.id) DESC, a.nombre ASC
        """)
    List<DashboardRankingResponse> findAutoresConMasLibros(Pageable pageable);
}
