package com.biblioteca.backend.repository;

import com.biblioteca.backend.entity.Categoria;
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
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Nested
    class ExistsChecks {

        @Test
        void existsByNombre_NombreExistente_RetornaTrue() {
            categoriaRepository.save(Categoria.builder().nombre("Drama").descripcion("Libros de drama").estado(true).build());

            assertTrue(categoriaRepository.existsByNombre("Drama"));
            assertFalse(categoriaRepository.existsByNombre("Comedia"));
        }
    }

    @Nested
    class FindByFilters {

        @Test
        void buscarPorNombre_RetornaCoincidencias() {
            categoriaRepository.save(Categoria.builder().nombre("Ciencia Ficción").descripcion("Libros futuristas").estado(true).build());
            categoriaRepository.save(Categoria.builder().nombre("Historia").descripcion("Libros de hechos reales").estado(true).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<Categoria> resultado = categoriaRepository.findByFilters("Ficción", null, pageable);

            assertEquals(1, resultado.getTotalElements());
            assertEquals("Ciencia Ficción", resultado.getContent().getFirst().getNombre());
        }

        @Test
        void buscarPorDescripcion_RetornaCoincidencias() {
            categoriaRepository.save(Categoria.builder().nombre("Ciencia Ficción").descripcion("Libros futuristas").estado(true).build());
            categoriaRepository.save(Categoria.builder().nombre("Historia").descripcion("Libros de hechos reales").estado(true).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<Categoria> resultado = categoriaRepository.findByFilters("reales", null, pageable);

            assertEquals(1, resultado.getTotalElements());
            assertEquals("Historia", resultado.getContent().getFirst().getNombre());
        }

        @Test
        void filtroEstado_RetornaSoloActivosEInactivos() {
            categoriaRepository.save(Categoria.builder().nombre("Ciencia Ficción").descripcion("Libros futuristas").estado(true).build());
            categoriaRepository.save(Categoria.builder().nombre("Historia").descripcion("Libros de hechos reales").estado(false).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<Categoria> activos = categoriaRepository.findByFilters(null, true, pageable);
            Page<Categoria> inactivos = categoriaRepository.findByFilters(null, false, pageable);

            assertEquals(1, activos.getTotalElements());
            assertEquals("Ciencia Ficción", activos.getContent().getFirst().getNombre());
            assertEquals(1, inactivos.getTotalElements());
            assertEquals("Historia", inactivos.getContent().getFirst().getNombre());
        }

        @Test
        void sinFiltros_RetornaTodos() {
            categoriaRepository.save(Categoria.builder().nombre("Ciencia Ficción").descripcion("Libros futuristas").estado(true).build());
            categoriaRepository.save(Categoria.builder().nombre("Historia").descripcion("Libros de hechos reales").estado(true).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<Categoria> resultado = categoriaRepository.findByFilters(null, null, pageable);

            assertEquals(2, resultado.getTotalElements());
        }
    }
}
