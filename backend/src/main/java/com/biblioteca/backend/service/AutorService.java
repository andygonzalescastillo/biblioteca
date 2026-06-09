package com.biblioteca.backend.service;

import com.biblioteca.backend.entity.Autor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface AutorService {
    Page<Autor> obtenerTodos(String buscar, Boolean estado, Pageable pageable);
    Autor obtenerPorId(Long id);
    Autor crearAutor(Autor autor, UUID fotoId);
    Autor actualizar(Long id, Autor autorDetails, UUID fotoId, Boolean estado);
    void eliminar(Long id);
    Autor activar(Long id);
}
