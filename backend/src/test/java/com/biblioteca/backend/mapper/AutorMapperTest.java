package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.request.AutorRequest;
import com.biblioteca.backend.dto.response.AutorResponse;
import com.biblioteca.backend.entity.Autor;
import com.biblioteca.backend.entity.Imagen;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AutorMapperImpl.class, ImagenMapperImpl.class})
class AutorMapperTest {

    @Autowired
    private AutorMapper autorMapper;

    @Nested
    class ToResponse {

        @Test
        void conFoto_MapearTodosLosCampos() {
            Imagen foto = Imagen.builder()
                .id(UUID.randomUUID())
                .nombreArchivo("garcia.jpg")
                .urlAlmacenamiento("/uploads/garcia.jpg")
                .fechaCreacion(Instant.now())
                .build();
            Autor autor = Autor.builder()
                .id(1L)
                .nombre("Gabriel García Márquez")
                .biografia("Escritor colombiano")
                .fechaNacimiento(LocalDate.of(1927, 3, 6))
                .foto(foto)
                .estado(true)
                .build();

            AutorResponse response = autorMapper.toResponse(autor);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("Gabriel García Márquez", response.nombre());
            assertEquals("Escritor colombiano", response.biografia());
            assertEquals(LocalDate.of(1927, 3, 6), response.fechaNacimiento());
            assertTrue(response.estado());
            assertNotNull(response.foto());
            assertEquals("garcia.jpg", response.foto().nombreArchivo());
        }

        @Test
        void sinFoto_FotoEsNull() {
            Autor autor = Autor.builder()
                .id(2L)
                .nombre("Julio Cortázar")
                .estado(true)
                .build();

            AutorResponse response = autorMapper.toResponse(autor);

            assertNotNull(response);
            assertNull(response.foto());
        }

        @Test
        void conEntidadNula_RetornaNull() {
            assertNull(autorMapper.toResponse(null));
        }
    }

    @Nested
    class ToEntity {

        @Test
        void conTodosLosCampos_MapearCorrectamente() {
            AutorRequest request = new AutorRequest(
                "Borges", "Escritor argentino",
                LocalDate.of(1899, 8, 24), null, false
            );

            Autor entity = autorMapper.toEntity(request);

            assertNotNull(entity);
            assertNull(entity.getId());             // ignorado
            assertNull(entity.getFoto());           // ignorado
            assertEquals("Borges", entity.getNombre());
            assertEquals("Escritor argentino", entity.getBiografia());
            assertEquals(LocalDate.of(1899, 8, 24), entity.getFechaNacimiento());
            assertFalse(entity.isEstado());
        }

        @Test
        void sinEstado_EstadoDefaultTrue() {
            AutorRequest request = new AutorRequest("Nuevo Autor", null, null, null, null);

            Autor entity = autorMapper.toEntity(request);

            assertTrue(entity.isEstado());          // defaultValue = "true"
        }

        @Test
        void fotoIdIgnorada_FotoEsNull() {
            AutorRequest request = new AutorRequest(
                "Autor", null, null, UUID.randomUUID(), null
            );

            Autor entity = autorMapper.toEntity(request);

            assertNull(entity.getFoto());   // fotoId ignorado, foto seteada por lógica de negocio
        }
    }
}
