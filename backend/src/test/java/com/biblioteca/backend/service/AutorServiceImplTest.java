package com.biblioteca.backend.service;

import com.biblioteca.backend.entity.Autor;
import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.AutorRepository;
import com.biblioteca.backend.repository.ImagenRepository;
import com.biblioteca.backend.repository.LibroRepository;
import com.biblioteca.backend.service.impl.AutorServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutorServiceImplTest {

    @Mock
    private AutorRepository autorRepository;

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private ImagenRepository imagenRepository;

    @InjectMocks
    private AutorServiceImpl autorService;

    private Autor autorValido;
    private Imagen fotoValida;

    @BeforeEach
    void setUp() {
        fotoValida = Imagen.builder()
                .id(UUID.randomUUID())
                .nombreArchivo("autor.jpg")
                .urlAlmacenamiento("http://example.com/autor.jpg")
                .build();

        autorValido = Autor.builder()
                .id(1L)
                .nombre("Gabriel García Márquez")
                .biografia("Escritor colombiano, premio Nobel.")
                .fechaNacimiento(LocalDate.of(1927, 3, 6))
                .estado(true)
                .foto(fotoValida)
                .build();
    }

    @Nested
    class ObtenerTodos {

        @Test
        void obtenerTodos_Exitoso() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Autor> page = new PageImpl<>(List.of(autorValido));
            when(autorRepository.findByFilters("Gabriel", true, pageable)).thenReturn(page);

            Page<Autor> resultado = autorService.obtenerTodos("Gabriel", true, pageable);

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals("Gabriel García Márquez", resultado.getContent().getFirst().getNombre());
        }
    }

    @Nested
    class ObtenerPorId {

        @Test
        void obtenerPorId_Exitoso() {
            when(autorRepository.findById(1L)).thenReturn(Optional.of(autorValido));

            Autor resultado = autorService.obtenerPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
        }

        @Test
        void obtenerPorId_NoEncontrado_LanzaException() {
            when(autorRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> autorService.obtenerPorId(1L));
        }
    }

    @Nested
    class CrearAutor {

        @Test
        void crearAutor_ConFotoValida_Exitoso() {
            Autor nuevoAutor = Autor.builder()
                    .nombre("Julio Cortázar")
                    .biografia("Escritor argentino.")
                    .build();

            when(imagenRepository.findById(fotoValida.getId())).thenReturn(Optional.of(fotoValida));
            when(autorRepository.save(any(Autor.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Autor resultado = autorService.crearAutor(nuevoAutor, fotoValida.getId());

            assertNotNull(resultado);
            assertEquals("Julio Cortázar", resultado.getNombre());
            assertEquals(fotoValida, resultado.getFoto());
            verify(autorRepository).save(nuevoAutor);
        }

        @Test
        void crearAutor_ConFotoNula_Exitoso() {
            Autor nuevoAutor = Autor.builder()
                    .nombre("Julio Cortázar")
                    .biografia("Escritor argentino.")
                    .build();

            when(autorRepository.save(any(Autor.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Autor resultado = autorService.crearAutor(nuevoAutor, null);

            assertNotNull(resultado);
            assertEquals("Julio Cortázar", resultado.getNombre());
            assertNull(resultado.getFoto());
            verify(autorRepository).save(nuevoAutor);
            verifyNoInteractions(imagenRepository);
        }

        @Test
        void crearAutor_ConFotoNoExistente_LanzaException() {
            Autor nuevoAutor = Autor.builder()
                    .nombre("Julio Cortázar")
                    .biografia("Escritor argentino.")
                    .build();
            UUID fotoIdInvalido = UUID.randomUUID();

            when(imagenRepository.findById(fotoIdInvalido)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> autorService.crearAutor(nuevoAutor, fotoIdInvalido));
            verify(autorRepository, never()).save(any(Autor.class));
        }
    }

    @Nested
    class Actualizar {

        @Test
        void actualizar_ConFotoValidaYEstado_Exitoso() {
            Autor detallesActualizados = Autor.builder()
                    .nombre("Gabo")
                    .biografia("Biografía actualizada")
                    .fechaNacimiento(LocalDate.of(1927, 3, 6))
                    .build();

            when(autorRepository.findById(1L)).thenReturn(Optional.of(autorValido));
            when(imagenRepository.findById(fotoValida.getId())).thenReturn(Optional.of(fotoValida));
            when(autorRepository.save(any(Autor.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Autor resultado = autorService.actualizar(1L, detallesActualizados, fotoValida.getId(), false);

            assertNotNull(resultado);
            assertEquals("Gabo", resultado.getNombre());
            assertEquals("Biografía actualizada", resultado.getBiografia());
            assertFalse(resultado.isEstado());
            verify(autorRepository).save(autorValido);
        }

        @Test
        void actualizar_ConFotoNulaYEstadoNulo_Exitoso() {
            Autor detallesActualizados = Autor.builder()
                    .nombre("Gabo")
                    .biografia("Biografía actualizada")
                    .fechaNacimiento(LocalDate.of(1927, 3, 6))
                    .build();

            when(autorRepository.findById(1L)).thenReturn(Optional.of(autorValido));
            when(autorRepository.save(any(Autor.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Autor resultado = autorService.actualizar(1L, detallesActualizados, null, null);

            assertNotNull(resultado);
            assertEquals("Gabo", resultado.getNombre());
            assertNull(resultado.getFoto());
            assertTrue(resultado.isEstado()); // No cambia porque estado era true y pasamos null
            verify(autorRepository).save(autorValido);
        }

        @Test
        void actualizar_ConFotoInvalida_LanzaException() {
            Autor detallesActualizados = Autor.builder()
                    .nombre("Gabo")
                    .build();
            UUID fotoIdInvalido = UUID.randomUUID();

            when(autorRepository.findById(1L)).thenReturn(Optional.of(autorValido));
            when(imagenRepository.findById(fotoIdInvalido)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> autorService.actualizar(1L, detallesActualizados, fotoIdInvalido, true));
            verify(autorRepository, never()).save(any(Autor.class));
        }
    }

    @Nested
    class Eliminar {

        @Test
        void eliminar_SinLibrosActivos_Exitoso() {
            when(autorRepository.findById(1L)).thenReturn(Optional.of(autorValido));
            when(libroRepository.existsByAutoresIdAndEstadoTrue(1L)).thenReturn(false);
            when(autorRepository.save(any(Autor.class))).thenAnswer(invocation -> invocation.getArgument(0));

            autorService.eliminar(1L);

            assertFalse(autorValido.isEstado());
            verify(autorRepository).save(autorValido);
        }

        @Test
        void eliminar_ConLibrosActivos_LanzaBusinessException() {
            when(autorRepository.findById(1L)).thenReturn(Optional.of(autorValido));
            when(libroRepository.existsByAutoresIdAndEstadoTrue(1L)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> autorService.eliminar(1L));
            assertEquals("AUTHOR_HAS_ACTIVE_BOOKS", ex.errorCode());
            verify(autorRepository, never()).save(any(Autor.class));
        }
    }

    @Nested
    class Activar {

        @Test
        void activar_Exitoso() {
            autorValido.setEstado(false);
            when(autorRepository.findById(1L)).thenReturn(Optional.of(autorValido));
            when(autorRepository.save(any(Autor.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Autor resultado = autorService.activar(1L);

            assertTrue(resultado.isEstado());
            verify(autorRepository).save(autorValido);
        }
    }
}
