package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.request.LibroRequest;
import com.biblioteca.backend.dto.response.LibroResponse;
import com.biblioteca.backend.entity.Autor;
import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.entity.Libro;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    LibroMapperImpl.class,
    CategoriaMapperImpl.class,
    AutorMapperImpl.class,
    ImagenMapperImpl.class
})
class LibroMapperTest {

    @Autowired
    private LibroMapper libroMapper;

    @Nested
    class ToResponse {

        @Test
        void conTodosLosCampos_MapearCorrectamente() {
            Categoria categoria = Categoria.builder()
                .id(1L).nombre("Ficción").descripcion("Literatura de ficción").estado(true).build();
            Autor autor = Autor.builder()
                .id(10L).nombre("García Márquez").estado(true).build();
            Libro libro = Libro.builder()
                .id(100L)
                .titulo("Cien años de soledad")
                .isbn("978-0307474728")
                .stock(5)
                .categoria(categoria)
                .autores(Set.of(autor))
                .estado(true)
                .build();

            LibroResponse response = libroMapper.toResponse(libro);

            assertNotNull(response);
            assertEquals(100L, response.id());
            assertEquals("Cien años de soledad", response.titulo());
            assertEquals("978-0307474728", response.isbn());
            assertEquals(5, response.stock());
            assertTrue(response.estado());
            assertNotNull(response.categoria());
            assertEquals("Ficción", response.categoria().nombre());
            assertFalse(response.autores().isEmpty());
            assertEquals("García Márquez", response.autores().iterator().next().nombre());
        }

        @Test
        void sinPortada_PortadaEsNull() {
            Libro libro = Libro.builder()
                .id(1L).titulo("Test").isbn("000").stock(1)
                .categoria(Categoria.builder().id(1L).nombre("Cat").estado(true).build())
                .estado(true).build();

            LibroResponse response = libroMapper.toResponse(libro);

            assertNull(response.portada());
        }

        @Test
        void conEntidadNula_RetornaNull() {
            assertNull(libroMapper.toResponse(null));
        }
    }

    @Nested
    class ToEntity {

        @Test
        void conTodosLosCampos_MapearCamposDirectos() {
            LibroRequest request = new LibroRequest(
                "Clean Code", "978-0132350884", 10, 1L, Set.of(1L), null, false
            );

            Libro entity = libroMapper.toEntity(request);

            assertNotNull(entity);
            assertNull(entity.getId());                 // ignorado
            assertNull(entity.getCategoria());          // ignorado (set por servicio)
            assertNull(entity.getPortada());            // ignorado (set por servicio)
            assertTrue(entity.getAutores().isEmpty()); // ignorado: @Builder.Default → set vacío
            assertEquals("Clean Code", entity.getTitulo());
            assertEquals("978-0132350884", entity.getIsbn());
            assertEquals(10, entity.getStock());
            assertFalse(entity.isEstado());
        }

        @Test
        void sinEstado_EstadoDefaultTrue() {
            LibroRequest request = new LibroRequest(
                "Nuevo Libro", "000-000", 1, 1L, Set.of(1L), null, null
            );

            Libro entity = libroMapper.toEntity(request);

            assertTrue(entity.isEstado());      // defaultValue = "true"
        }
    }
}
