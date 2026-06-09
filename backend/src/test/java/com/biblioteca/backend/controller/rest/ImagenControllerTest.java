package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.response.ImagenResponse;
import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.mapper.ImagenMapper;
import com.biblioteca.backend.service.ImagenService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImagenController.class)
@Import(ImagenControllerTest.TestConfig.class)
class ImagenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImagenService imagenService;

    @MockitoBean
    private ImagenMapper imagenMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AppProperties appProperties() {
            return new AppProperties(
                "uploads",
                new AppProperties.Cors(List.of("http://localhost:3000")),
                new AppProperties.Prestamo(5, 1, 30, 3, 5)
            );
        }
    }

    @Nested
    class SubirImagen {

        @Test
        void subirImagen_ConArchivoValido_DebeRetornarCreated() throws Exception {
            UUID imagenId = UUID.randomUUID();
            MockMultipartFile archivo = new MockMultipartFile(
                    "archivo", "portada.jpg", MediaType.IMAGE_JPEG_VALUE,
                    "fake-image-bytes".getBytes()
            );

            Imagen imagenEntity = new Imagen();
            ImagenResponse responseDto = new ImagenResponse(
                    imagenId, "portada.jpg",
                    "/uploads/portada.jpg", Instant.now()
            );

            when(imagenService.guardarImagen(any())).thenReturn(imagenEntity);
            when(imagenMapper.toResponse(any(Imagen.class))).thenReturn(responseDto);

            mockMvc.perform(multipart("/api/imagenes").file(archivo))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(imagenId.toString()))
                    .andExpect(jsonPath("$.nombreArchivo").value("portada.jpg"))
                    .andExpect(jsonPath("$.urlAlmacenamiento").value("/uploads/portada.jpg"));

            verify(imagenService).guardarImagen(any());
            verify(imagenMapper).toResponse(any(Imagen.class));
        }

        @Test
        void subirImagen_CuandoIOException_DebeRetornarErrorInterno() throws Exception {
            MockMultipartFile archivo = new MockMultipartFile(
                    "archivo", "error.jpg", MediaType.IMAGE_JPEG_VALUE,
                    "bytes".getBytes()
            );

            when(imagenService.guardarImagen(any())).thenThrow(new IOException("Disco lleno"));

            mockMvc.perform(multipart("/api/imagenes").file(archivo))
                    .andExpect(status().isInternalServerError());

            verify(imagenService).guardarImagen(any());
        }

        @Test
        void subirImagen_CuandoArchivoInvalido_DebeRetornarBadRequest() throws Exception {
            MockMultipartFile archivo = new MockMultipartFile(
                    "archivo", "script.exe", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    "fake".getBytes()
            );

            when(imagenService.guardarImagen(any()))
                    .thenThrow(BusinessException.badRequest("INVALID_FILE_TYPE", "Solo se permiten imágenes"));

            mockMvc.perform(multipart("/api/imagenes").file(archivo))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_TYPE"));

            verify(imagenService).guardarImagen(any());
        }
    }

    @Nested
    class EliminarImagen {

        @Test
        void eliminarImagen_CuandoExiste_DebeRetornarNoContent() throws Exception {
            UUID imagenId = UUID.randomUUID();
            doNothing().when(imagenService).eliminarImagen(imagenId);

            mockMvc.perform(delete("/api/imagenes/" + imagenId))
                    .andExpect(status().isNoContent());

            verify(imagenService).eliminarImagen(imagenId);
        }

        @Test
        void eliminarImagen_CuandoNoExiste_DebeRetornarNotFound() throws Exception {
            UUID imagenId = UUID.randomUUID();
            doThrow(new EntityNotFoundException("Imagen no encontrada"))
                    .when(imagenService).eliminarImagen(imagenId);

            mockMvc.perform(delete("/api/imagenes/" + imagenId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));

            verify(imagenService).eliminarImagen(imagenId);
        }

        @Test
        void eliminarImagen_CuandoErrorAlBorrarArchivo_DebeRetornarErrorInterno() throws Exception {
            UUID imagenId = UUID.randomUUID();
            doThrow(new IOException("No se pudo eliminar el archivo"))
                    .when(imagenService).eliminarImagen(imagenId);

            mockMvc.perform(delete("/api/imagenes/" + imagenId))
                    .andExpect(status().isInternalServerError());

            verify(imagenService).eliminarImagen(imagenId);
        }
    }
}
