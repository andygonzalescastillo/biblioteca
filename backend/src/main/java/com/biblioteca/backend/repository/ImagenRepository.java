package com.biblioteca.backend.repository;

import com.biblioteca.backend.entity.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, UUID> {
}
