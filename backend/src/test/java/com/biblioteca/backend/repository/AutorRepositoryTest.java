package com.biblioteca.backend.repository;

import com.biblioteca.backend.entity.Autor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AutorRepositoryTest {

    @Autowired
    private AutorRepository autorRepository;

    @Nested
    class FindByFilters {

        @Test
        void buscarPorNombre_RetornaCoincidencias() {
            autorRepository.save(Autor.builder().nombre("Gabriel García Márquez").biografia("Escritor colombiano").estado(true).build());
            autorRepository.save(Autor.builder().nombre("Julio Cortázar").biografia("Escritor argentino").estado(true).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<Autor> resultado = autorRepository.findByFilters("Gabriel", null, pageable);

            assertEquals(1, resultado.getTotalElements());
            assertEquals("Gabriel García Márquez", resultado.getContent().getFirst().getNombre());
        }

        @Test
        void buscarPorBiografia_RetornaCoincidencias() {
            autorRepository.save(Autor.builder().nombre("Gabriel García Márquez").biografia("Escritor colombiano").estado(true).build());
            autorRepository.save(Autor.builder().nombre("Julio Cortázar").biografia("Escritor argentino").estado(true).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<Autor> resultado = autorRepository.findByFilters("argentino", null, pageable);

            assertEquals(1, resultado.getTotalElements());
            assertEquals("Julio Cortázar", resultado.getContent().getFirst().getNombre());
        }

        @Test
        void filtroEstado_RetornaSoloActivos() {
            autorRepository.save(Autor.builder().nombre("Gabriel García Márquez").biografia("Escritor colombiano").estado(true).build());
            autorRepository.save(Autor.builder().nombre("Julio Cortázar").biografia("Escritor argentino").estado(false).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<Autor> activos = autorRepository.findByFilters(null, true, pageable);
            Page<Autor> inactivos = autorRepository.findByFilters(null, false, pageable);

            assertEquals(1, activos.getTotalElements());
            assertEquals("Gabriel García Márquez", activos.getContent().getFirst().getNombre());
            assertEquals(1, inactivos.getTotalElements());
            assertEquals("Julio Cortázar", inactivos.getContent().getFirst().getNombre());
        }

        @Test
        void sinFiltros_RetornaTodos() {
            autorRepository.save(Autor.builder().nombre("Gabriel García Márquez").biografia("Escritor colombiano").estado(true).build());
            autorRepository.save(Autor.builder().nombre("Julio Cortázar").biografia("Escritor argentino").estado(true).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<Autor> resultado = autorRepository.findByFilters(null, null, pageable);

            assertEquals(2, resultado.getTotalElements());
        }
    }
}
