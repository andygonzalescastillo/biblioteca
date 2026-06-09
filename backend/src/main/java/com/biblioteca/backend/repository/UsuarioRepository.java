package com.biblioteca.backend.repository;

import com.biblioteca.backend.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
        SELECT u FROM Usuario u WHERE
        (CAST(:buscar AS string) IS NULL
         OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%'))
         OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:buscar AS string), '%')))
        AND (:estado IS NULL OR u.estado = :estado)
        """)
    Page<Usuario> findByFilters(
        @Param("buscar") String buscar,
        @Param("estado") Boolean estado,
        Pageable pageable
    );

    long countByEstadoTrue();
}
