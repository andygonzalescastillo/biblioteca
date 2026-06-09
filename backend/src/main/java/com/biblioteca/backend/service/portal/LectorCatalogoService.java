package com.biblioteca.backend.service.portal;

import com.biblioteca.backend.entity.Autor;
import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.entity.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LectorCatalogoService {
    Page<Libro> obtenerCatalogo(String buscar, Long categoriaId, Long autorId, Pageable pageable);
    Libro obtenerLibroActivo(Long libroId);
    List<Categoria> obtenerCategoriasActivas();
    List<Autor> obtenerAutoresActivos();
}
