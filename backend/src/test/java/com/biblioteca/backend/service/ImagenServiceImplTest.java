package com.biblioteca.backend.service;

import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.ImagenRepository;
import com.biblioteca.backend.service.impl.ImagenServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImagenServiceImplTest {

    @Mock
    private ImagenRepository imagenRepository;

    @InjectMocks
    private ImagenServiceImpl imagenService;

    @TempDir
    Path tempDir;

    private Imagen imagenValida;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imagenService, "uploadDir", tempDir.toString());

        imagenValida = Imagen.builder()
                .id(UUID.randomUUID())
                .nombreArchivo("test.jpg")
                .urlAlmacenamiento("/uploads/test-uuid.jpg")
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
            byte[] tinyPng = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00,
                0x00, 0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82
            };

            MockMultipartFile realFile = new MockMultipartFile(
                    "archivo",
                    "imagen.png",
                    "image/png",
                    tinyPng
            );

            when(imagenRepository.save(any(Imagen.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Imagen resultado = imagenService.guardarImagen(realFile);

            assertNotNull(resultado);
            assertEquals("imagen.png", resultado.getNombreArchivo());
            assertTrue(resultado.getUrlAlmacenamiento().startsWith("/uploads/"));
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
        void eliminarImagen_ConArchivoFisico_Exitoso() throws IOException {
            UUID id = imagenValida.getId();
            when(imagenRepository.findById(id)).thenReturn(Optional.of(imagenValida));

            Path fileInTemp = tempDir.resolve("test-uuid.jpg");
            Files.write(fileInTemp, new byte[]{1, 2});

            imagenService.eliminarImagen(id);

            assertFalse(Files.exists(fileInTemp));
            verify(imagenRepository).delete(imagenValida);
        }

        @Test
        void eliminarImagen_SinArchivoFisico_Exitoso() throws IOException {
            UUID id = imagenValida.getId();
            when(imagenRepository.findById(id)).thenReturn(Optional.of(imagenValida));

            // No creamos el archivo físico en tempDir para simular que no existe en el disco
            imagenService.eliminarImagen(id);

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
