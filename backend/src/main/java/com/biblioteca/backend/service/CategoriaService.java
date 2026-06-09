package com.biblioteca.backend.service;

import com.biblioteca.backend.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoriaService {
    Page<Categoria> obtenerTodas(String buscar, Boolean estado, Pageable pageable);
    Categoria obtenerPorId(Long id);
    Categoria crearCategoria(Categoria categoria);
    Categoria actualizar(Long id, Categoria categoriaDetails, Boolean estado);
    void eliminar(Long id);
    Categoria activar(Long id);
}
