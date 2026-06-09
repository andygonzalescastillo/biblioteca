package com.biblioteca.backend.repository;

import com.biblioteca.backend.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;


    @BeforeEach
    void setUp() {
        usuarioRepository.save(
            Usuario.builder()
                .nombre("Ana García López")
                .email("ana.garcia@example.com")
                .telefono("555-1234")
                .estado(true)
                .build()
        );
        usuarioRepository.save(
            Usuario.builder()
                .nombre("Carlos Mendoza")
                .email("carlos.mendoza@example.com")
                .telefono("555-5678")
                .estado(false)
                .build()
        );
    }

    @Nested
    class FindByEmail {

        @Test
        void emailExistente_RetornaUsuario() {
            Optional<Usuario> resultado = usuarioRepository.findByEmail("ana.garcia@example.com");

            assertTrue(resultado.isPresent());
            assertEquals("Ana García López", resultado.get().getNombre());
        }

        @Test
        void emailInexistente_RetornaVacio() {
            Optional<Usuario> resultado = usuarioRepository.findByEmail("noexiste@example.com");

            assertFalse(resultado.isPresent());
        }
    }

    @Nested
    class ExistsChecks {

        @Test
        void existsByEmail_EmailRegistrado_RetornaTrue() {
            assertTrue(usuarioRepository.existsByEmail("ana.garcia@example.com"));
        }

        @Test
        void existsByEmail_EmailNoRegistrado_RetornaFalse() {
            assertFalse(usuarioRepository.existsByEmail("nuevo@example.com"));
        }
    }

    @Nested
    class FindByFilters {

        @Test
        void sinFiltros_RetornaTodos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Usuario> resultado = usuarioRepository.findByFilters(null, null, pageable);

            assertEquals(2, resultado.getTotalElements());
        }

        @Test
        void buscarPorNombre_RetornaCoincidencias() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Usuario> resultado = usuarioRepository.findByFilters("Ana", null, pageable);

            assertEquals(1, resultado.getTotalElements());
            assertEquals("Ana García López", resultado.getContent().getFirst().getNombre());
        }

        @Test
        void buscarPorEmail_RetornaCoincidencias() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Usuario> resultado = usuarioRepository.findByFilters("carlos.mendoza", null, pageable);

            assertEquals(1, resultado.getTotalElements());
            assertEquals("Carlos Mendoza", resultado.getContent().getFirst().getNombre());
        }

        @Test
        void filtroEstadoActivo_RetornaSoloActivos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Usuario> activos = usuarioRepository.findByFilters(null, true, pageable);

            assertEquals(1, activos.getTotalElements());
            assertEquals("Ana García López", activos.getContent().getFirst().getNombre());
        }

        @Test
        void filtroEstadoInactivo_RetornaSoloInactivos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Usuario> inactivos = usuarioRepository.findByFilters(null, false, pageable);

            assertEquals(1, inactivos.getTotalElements());
            assertEquals("Carlos Mendoza", inactivos.getContent().getFirst().getNombre());
        }

        @Test
        void buscarSinCoincidencias_RetornaVacio() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Usuario> resultado = usuarioRepository.findByFilters("xxxxxx", null, pageable);

            assertEquals(0, resultado.getTotalElements());
        }
    }

    @Nested
    class Contadores {

        @Test
        void countByEstadoTrue_RetornaCantidadDeActivos() {
            assertEquals(1, usuarioRepository.countByEstadoTrue());
        }
    }
}
