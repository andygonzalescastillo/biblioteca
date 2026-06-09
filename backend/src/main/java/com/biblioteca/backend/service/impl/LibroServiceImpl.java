package com.biblioteca.backend.service.impl;

import com.biblioteca.backend.entity.Autor;
import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.entity.Libro;
import com.biblioteca.backend.entity.EstadoPrestamo;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.AutorRepository;
import com.biblioteca.backend.repository.CategoriaRepository;
import com.biblioteca.backend.repository.ImagenRepository;
import com.biblioteca.backend.repository.LibroRepository;
import com.biblioteca.backend.repository.PrestamoRepository;
import com.biblioteca.backend.service.LibroService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final ImagenRepository imagenRepository;
    private final AutorRepository autorRepository;
    private final PrestamoRepository prestamoRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Libro> obtenerTodos(String buscar, Boolean estado, Long categoriaId, Long autorId, Pageable pageable) {
        Page<Long> idsPage = libroRepository.findIdsByFilters(buscar, estado, categoriaId, autorId, pageable);
        if (idsPage.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, Libro> librosPorId = libroRepository.findAllByIdInWithRelations(idsPage.getContent())
                .stream()
                .collect(Collectors.toMap(Libro::getId, Function.identity()));

        List<Libro> librosOrdenados = idsPage.getContent().stream()
                .map(librosPorId::get)
                .toList();

        return new PageImpl<>(librosOrdenados, pageable, idsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Libro obtenerPorId(Long id) {
        return libroRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Libro no encontrado con el ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Libro obtenerPorIsbn(String isbn) {
        return libroRepository.findByIsbnWithRelations(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Libro no encontrado con el ISBN: " + isbn));
    }

    @Override
    @Transactional
    public Libro crearLibro(Libro libro, Long categoriaId, UUID portadaId, Set<Long> autoresIds) {
        if (libroRepository.existsByIsbn(libro.getIsbn())) {
            throw BusinessException.conflict("ISBN_ALREADY_EXISTS", "Ya existe un libro registrado con el ISBN: " + libro.getIsbn());
        }

        libro.setCategoria(buscarCategoria(categoriaId));
        libro.setPortada(buscarPortadaOpcional(portadaId));
        libro.setAutores(buscarAutores(autoresIds));

        return libroRepository.save(libro);
    }

    @Override
    @Transactional
    public Libro actualizar(Long id, Libro libroDetails, Long categoriaId, UUID portadaId, Set<Long> autoresIds, Boolean estado) {
        Libro libro = obtenerPorId(id);

        if (!libro.getIsbn().equals(libroDetails.getIsbn()) && libroRepository.existsByIsbn(libroDetails.getIsbn())) {
            throw BusinessException.conflict("ISBN_ALREADY_EXISTS", "Ya existe un libro registrado con el ISBN: " + libroDetails.getIsbn());
        }

        libro.setTitulo(libroDetails.getTitulo());
        libro.setIsbn(libroDetails.getIsbn());
        libro.setStock(libroDetails.getStock());

        libro.setCategoria(buscarCategoria(categoriaId));
        libro.setPortada(buscarPortadaOpcional(portadaId));
        libro.setAutores(buscarAutores(autoresIds));

        if (estado != null) {
            libro.setEstado(estado);
        }

        return libroRepository.save(libro);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Libro libro = obtenerPorId(id);
        if (prestamoRepository.existsByDetallesLibroIdAndEstado(id, EstadoPrestamo.ACTIVO)) {
            throw BusinessException.conflict("BOOK_HAS_ACTIVE_LOANS", "No se puede desactivar el libro porque está asociado a un préstamo activo.");
        }
        libro.setEstado(false);
        libroRepository.save(libro);
    }

    @Override
    @Transactional
    public Libro activar(Long id) {
        Libro libro = obtenerPorId(id);
        libro.setEstado(true);
        return libroRepository.save(libro);
    }

    @Override
    @Transactional
    public Libro actualizarStock(Long id, int cantidad) {
        Libro libro = obtenerPorId(id);
        int nuevoStock = libro.getStock() + cantidad;
        
        if (nuevoStock < 0) {
            throw BusinessException.conflict("INSUFFICIENT_STOCK", "No hay suficiente stock para el libro: " + libro.getTitulo() + ". Stock actual: " + libro.getStock());
        }
        
        libro.setStock(nuevoStock);
        return libroRepository.save(libro);
    }

    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con el ID: " + categoriaId));
    }

    private Imagen buscarPortadaOpcional(UUID portadaId) {
        if (portadaId == null) {
            return null;
        }
        return imagenRepository.findById(portadaId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen de portada no encontrada con el ID: " + portadaId));
    }

    private Set<Autor> buscarAutores(Set<Long> autoresIds) {
        if (autoresIds == null || autoresIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Autor> autores = new HashSet<>(autorRepository.findAllById(autoresIds));
        if (autores.size() != autoresIds.size()) {
            throw new EntityNotFoundException("Uno o más autores especificados no fueron encontrados.");
        }
        return autores;
    }
}
