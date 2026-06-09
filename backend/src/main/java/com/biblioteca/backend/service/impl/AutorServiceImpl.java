package com.biblioteca.backend.service.impl;

import com.biblioteca.backend.entity.Autor;
import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.AutorRepository;
import com.biblioteca.backend.repository.ImagenRepository;
import com.biblioteca.backend.repository.LibroRepository;
import com.biblioteca.backend.service.AutorService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutorServiceImpl implements AutorService {

    private final AutorRepository autorRepository;
    private final LibroRepository libroRepository;
    private final ImagenRepository imagenRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Autor> obtenerTodos(String buscar, Boolean estado, Pageable pageable) {
        return autorRepository.findByFilters(buscar, estado, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Autor obtenerPorId(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado con el ID: " + id));
    }

    @Override
    @Transactional
    public Autor crearAutor(Autor autor, UUID fotoId) {
        autor.setFoto(buscarImagenOpcional(fotoId));
        return autorRepository.save(autor);
    }

    @Override
    @Transactional
    public Autor actualizar(Long id, Autor autorDetails, UUID fotoId, Boolean estado) {
        Autor autor = obtenerPorId(id);
        autor.setNombre(autorDetails.getNombre());
        autor.setBiografia(autorDetails.getBiografia());
        autor.setFechaNacimiento(autorDetails.getFechaNacimiento());

        autor.setFoto(buscarImagenOpcional(fotoId));

        if (estado != null) {
            autor.setEstado(estado);
        }

        return autorRepository.save(autor);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Autor autor = obtenerPorId(id);
        if (libroRepository.existsByAutoresIdAndEstadoTrue(id)) {
            throw BusinessException.conflict("AUTHOR_HAS_ACTIVE_BOOKS", "No se puede desactivar el autor porque tiene libros activos asociados.");
        }
        autor.setEstado(false);
        autorRepository.save(autor);
    }

    @Override
    @Transactional
    public Autor activar(Long id) {
        Autor autor = obtenerPorId(id);
        autor.setEstado(true);
        return autorRepository.save(autor);
    }

    private Imagen buscarImagenOpcional(UUID fotoId) {
        if (fotoId == null) {
            return null;
        }
        return imagenRepository.findById(fotoId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada con el ID: " + fotoId));
    }
}
