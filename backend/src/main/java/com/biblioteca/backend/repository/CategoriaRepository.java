package com.biblioteca.backend.repository;

import com.biblioteca.backend.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNombre(String nombre);

    @Query("""
        SELECT c FROM Categoria c WHERE
        (CAST(:buscar AS string) IS NULL
         OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
         OR LOWER(c.descripcion) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
        AND (:estado IS NULL OR c.estado = :estado)
        """)
    Page<Categoria> findByFilters(@Param("buscar") String buscar, @Param("estado") Boolean estado, Pageable pageable);

}
