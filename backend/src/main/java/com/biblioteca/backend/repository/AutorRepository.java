package com.biblioteca.backend.repository;

import com.biblioteca.backend.entity.Autor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {

    @Query("""
        SELECT a FROM Autor a WHERE
        (CAST(:buscar AS string) IS NULL
         OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
         OR LOWER(a.biografia) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
        AND (:estado IS NULL OR a.estado = :estado)
        """)
    Page<Autor> findByFilters(@Param("buscar") String buscar, @Param("estado") Boolean estado, Pageable pageable);

}
