package com.biblioteca.backend.service.portal;

import com.biblioteca.backend.entity.Libro;

import java.util.List;

public interface LectorCarritoService {
    Libro obtenerLibroAgregable(Long libroId, Long lectorId);
    List<Libro> obtenerLibrosCarrito(List<Long> librosIds);
}
