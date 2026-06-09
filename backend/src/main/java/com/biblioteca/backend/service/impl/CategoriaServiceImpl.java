package com.biblioteca.backend.service.impl;

import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.CategoriaRepository;
import com.biblioteca.backend.repository.LibroRepository;
import com.biblioteca.backend.service.CategoriaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> obtenerTodas(String buscar, Boolean estado, Pageable pageable) {
        return categoriaRepository.findByFilters(buscar, estado, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con el ID: " + id));
    }

    @Override
    @Transactional
    public Categoria crearCategoria(Categoria categoria) {
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            throw BusinessException.conflict("CATEGORY_ALREADY_EXISTS", "Ya existe una categoría con el nombre: " + categoria.getNombre());
        }
        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public Categoria actualizar(Long id, Categoria categoriaDetails, Boolean estado) {
        Categoria categoria = obtenerPorId(id);

        if (!categoria.getNombre().equalsIgnoreCase(categoriaDetails.getNombre())
                && categoriaRepository.existsByNombre(categoriaDetails.getNombre())) {
            throw BusinessException.conflict("CATEGORY_ALREADY_EXISTS", "Ya existe una categoría con el nombre: " + categoriaDetails.getNombre());
        }

        categoria.setNombre(categoriaDetails.getNombre());
        categoria.setDescripcion(categoriaDetails.getDescripcion());

        if (estado != null) {
            categoria.setEstado(estado);
        }

        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtenerPorId(id);
        if (libroRepository.existsByCategoriaIdAndEstadoTrue(id)) {
            throw BusinessException.conflict("CATEGORY_HAS_ACTIVE_BOOKS", "No se puede desactivar la categoría porque tiene libros activos asociados.");
        }
        categoria.setEstado(false);
        categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public Categoria activar(Long id) {
        Categoria categoria = obtenerPorId(id);
        categoria.setEstado(true);
        return categoriaRepository.save(categoria);
    }
}
