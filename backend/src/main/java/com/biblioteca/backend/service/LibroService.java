package com.biblioteca.backend.service;

import com.biblioteca.backend.entity.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Set;
import java.util.UUID;

public interface LibroService {
    Page<Libro> obtenerTodos(String buscar, Boolean estado, Long categoriaId, Long autorId, Pageable pageable);
    Libro obtenerPorId(Long id);
    Libro obtenerPorIsbn(String isbn);
    Libro crearLibro(Libro libro, Long categoriaId, UUID portadaId, Set<Long> autoresIds);
    Libro actualizar(Long id, Libro libroDetails, Long categoriaId, UUID portadaId, Set<Long> autoresIds, Boolean estado);
    Libro actualizarStock(Long id, int cantidad);
    void eliminar(Long id);
    Libro activar(Long id);
}
