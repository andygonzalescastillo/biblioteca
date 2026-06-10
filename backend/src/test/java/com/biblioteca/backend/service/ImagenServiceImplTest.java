package com.biblioteca.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.ImagenRepository;
import com.biblioteca.backend.service.impl.ImagenServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImagenServiceImplTest {

    @Mock
    private ImagenRepository imagenRepository;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private ImagenServiceImpl imagenService;

    private Imagen imagenValida;

    @BeforeEach
    void setUp() {
        imagenValida = Imagen.builder()
                .id(UUID.randomUUID())
                .nombreArchivo("test.jpg")
                .urlAlmacenamiento("https://res.cloudinary.com/test/image/upload/v1/biblioteca/test-uuid.jpg")
                .build();
    }

    @Nested
    class ObtenerImagenPorId {

        @Test
        void obtenerImagenPorId_Exitoso() {
            UUID id = imagenValida.getId();
            when(imagenRepository.findById(id)).thenReturn(Optional.of(imagenValida));

            Imagen resultado = imagenService.obtenerImagenPorId(id);

            assertNotNull(resultado);
            assertEquals(id, resultado.getId());
        }

        @Test
        void obtenerImagenPorId_NoEncontrada_LanzaException() {
            UUID id = UUID.randomUUID();
            when(imagenRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> imagenService.obtenerImagenPorId(id));
        }
    }

    @Nested
    class GuardarImagen {

        @Test
        void guardarImagen_Exitoso() throws IOException {
            String cloudinaryUrl = "https://res.cloudinary.com/test/image/upload/v1/biblioteca/abc123.jpg";
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap()))
                    .thenReturn(Map.of("secure_url", cloudinaryUrl));
            when(imagenRepository.save(any(Imagen.class))).thenAnswer(inv -> inv.getArgument(0));

            MockMultipartFile file = new MockMultipartFile(
                    "archivo", "imagen.png", "image/png", new byte[]{1, 2, 3});

            Imagen resultado = imagenService.guardarImagen(file);

            assertNotNull(resultado);
            assertEquals("imagen.png", resultado.getNombreArchivo());
            assertEquals(cloudinaryUrl, resultado.getUrlAlmacenamiento());
            verify(imagenRepository).save(any(Imagen.class));
        }

        @Test
        void guardarImagen_Error_ArchivoVacio_LanzaBusinessException() {
            MockMultipartFile file = new MockMultipartFile("archivo", "", "image/jpeg", new byte[0]);

            BusinessException ex = assertThrows(BusinessException.class, () -> imagenService.guardarImagen(file));
            assertEquals("EMPTY_FILE", ex.errorCode());
            verify(imagenRepository, never()).save(any(Imagen.class));
        }

        @Test
        void guardarImagen_Error_ContentTypeInvalido_LanzaBusinessException() {
            MockMultipartFile file = new MockMultipartFile("archivo", "doc.pdf", "application/pdf", new byte[]{1, 2});

            BusinessException ex = assertThrows(BusinessException.class, () -> imagenService.guardarImagen(file));
            assertEquals("INVALID_IMAGE_TYPE", ex.errorCode());
            verify(imagenRepository, never()).save(any(Imagen.class));
        }

        @Test
        void guardarImagen_Error_ExtensionInvalida_LanzaBusinessException() {
            MockMultipartFile file = new MockMultipartFile("archivo", "imagen.txt", "image/jpeg", new byte[]{1, 2});

            BusinessException ex = assertThrows(BusinessException.class, () -> imagenService.guardarImagen(file));
            assertEquals("INVALID_IMAGE_EXTENSION", ex.errorCode());
            verify(imagenRepository, never()).save(any(Imagen.class));
        }
    }

    @Nested
    class EliminarImagen {

        @Test
        void eliminarImagen_Exitoso() throws IOException {
            UUID id = imagenValida.getId();
            when(imagenRepository.findById(id)).thenReturn(Optional.of(imagenValida));
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(anyString(), anyMap())).thenReturn(Map.of("result", "ok"));

            imagenService.eliminarImagen(id);

            verify(uploader).destroy(eq("biblioteca/test-uuid"), anyMap());
            verify(imagenRepository).delete(imagenValida);
        }

        @Test
        void eliminarImagen_NoEncontrada_LanzaException() {
            UUID id = UUID.randomUUID();
            when(imagenRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> imagenService.eliminarImagen(id));
            verify(imagenRepository, never()).delete(any(Imagen.class));
        }
    }
}
