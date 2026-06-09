package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.response.ImagenResponse;
import com.biblioteca.backend.entity.Imagen;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ImagenMapperImpl.class)
class ImagenMapperTest {

    @Autowired
    private ImagenMapper imagenMapper;

    @Nested
    class ToResponse {

        @Test
        void conTodosLosCampos_MapearCorrectamente() {
            UUID id = UUID.randomUUID();
            Instant ahora = Instant.now();
            Imagen imagen = Imagen.builder()
                .id(id)
                .nombreArchivo("foto.jpg")
                .urlAlmacenamiento("/uploads/foto.jpg")
                .fechaCreacion(ahora)
                .build();

            ImagenResponse response = imagenMapper.toResponse(imagen);

            assertNotNull(response);
            assertEquals(id, response.id());
            assertEquals("foto.jpg", response.nombreArchivo());
            assertEquals("/uploads/foto.jpg", response.urlAlmacenamiento());
            assertEquals(ahora, response.fechaCreacion());
        }

        @Test
        void conEntidadNula_RetornaNull() {
            assertNull(imagenMapper.toResponse(null));
        }
    }
}
