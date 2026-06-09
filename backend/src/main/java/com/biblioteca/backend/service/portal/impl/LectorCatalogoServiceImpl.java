package com.biblioteca.backend.service.portal.impl;

import com.biblioteca.backend.entity.Autor;
import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.entity.Libro;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.service.AutorService;
import com.biblioteca.backend.service.CategoriaService;
import com.biblioteca.backend.service.LibroService;
import com.biblioteca.backend.service.portal.LectorCatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LectorCatalogoServiceImpl implements LectorCatalogoService {

    private final LibroService libroService;
    private final CategoriaService categoriaService;
    private final AutorService autorService;

    @Override
    @Transactional(readOnly = true)
    public Page<Libro> obtenerCatalogo(String buscar, Long categoriaId, Long autorId, Pageable pageable) {
        return libroService.obtenerTodos(normalizarBusqueda(buscar), true, categoriaId, autorId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Libro obtenerLibroActivo(Long libroId) {
        Libro libro = libroService.obtenerPorId(libroId);
        if (!libro.isEstado()) {
            throw BusinessException.conflict("BOOK_INACTIVE", "Este libro no está disponible en el catálogo público.");
        }
        return libro;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> obtenerCategoriasActivas() {
        return categoriaService.obtenerTodas(null, true, PageRequest.of(0, 200, Sort.by("nombre").ascending())).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Autor> obtenerAutoresActivos() {
        return autorService.obtenerTodos(null, true, PageRequest.of(0, 200, Sort.by("nombre").ascending())).getContent();
    }

    private String normalizarBusqueda(String buscar) {
        return Optional.ofNullable(buscar)
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .orElse(null);
    }
}
