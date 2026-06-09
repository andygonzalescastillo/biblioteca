package com.biblioteca.backend.service;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.response.UsuarioCupoPrestamoResponse;
import com.biblioteca.backend.entity.EstadoPrestamo;
import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.entity.Usuario;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.ImagenRepository;
import com.biblioteca.backend.repository.PrestamoRepository;
import com.biblioteca.backend.repository.UsuarioRepository;
import com.biblioteca.backend.service.impl.UsuarioServiceImpl;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ImagenRepository imagenRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioValido;
    private Imagen fotoValida;
    private AppProperties.Prestamo prestamoProps;

    @BeforeEach
    void setUp() {
        fotoValida = Imagen.builder()
                .id(UUID.randomUUID())
                .nombreArchivo("foto.jpg")
                .urlAlmacenamiento("http://example.com/foto.jpg")
                .build();

        usuarioValido = Usuario.builder()
                .id(1L)
                .nombre("Pedro Perez")
                .email("pedro@gmail.com")
                .telefono("999888777")
                .direccion("Av. Siempreviva 123")
                .estado(true)
                .foto(fotoValida)
                .build();

        prestamoProps = new AppProperties.Prestamo(14, 1, 30, 3, 5);
        lenient().when(appProperties.prestamo()).thenReturn(prestamoProps);
    }

    @Nested
    class ObtenerTodos {

        @Test
        void obtenerTodos_Exitoso() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Usuario> page = new PageImpl<>(List.of(usuarioValido));
            when(usuarioRepository.findByFilters("Pedro", true, pageable)).thenReturn(page);

            Page<Usuario> resultado = usuarioService.obtenerTodos("Pedro", true, pageable);

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals("Pedro Perez", resultado.getContent().getFirst().getNombre());
        }
    }

    @Nested
    class ObtenerPorId {

        @Test
        void obtenerPorId_Exitoso() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

            Usuario resultado = usuarioService.obtenerPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
        }

        @Test
        void obtenerPorId_NoEncontrado_LanzaException() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> usuarioService.obtenerPorId(1L));
        }
    }

    @Nested
    class ObtenerPorEmail {

        @Test
        void obtenerPorEmail_Exitoso() {
            when(usuarioRepository.findByEmail("pedro@gmail.com")).thenReturn(Optional.of(usuarioValido));

            Usuario resultado = usuarioService.obtenerPorEmail("pedro@gmail.com");

            assertNotNull(resultado);
            assertEquals("pedro@gmail.com", resultado.getEmail());
        }

        @Test
        void obtenerPorEmail_NoEncontrado_LanzaException() {
            when(usuarioRepository.findByEmail("pedro@gmail.com")).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> usuarioService.obtenerPorEmail("pedro@gmail.com"));
        }
    }

    @Nested
    class CrearUsuario {

        @Test
        void crearUsuario_ConFotoValida_Exitoso() {
            Usuario nuevoUsuario = Usuario.builder()
                    .nombre("Carlos Gomez")
                    .email("carlos@gmail.com")
                    .build();

            when(usuarioRepository.existsByEmail("carlos@gmail.com")).thenReturn(false);
            when(imagenRepository.findById(fotoValida.getId())).thenReturn(Optional.of(fotoValida));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Usuario resultado = usuarioService.crearUsuario(nuevoUsuario, fotoValida.getId());

            assertNotNull(resultado);
            assertEquals("Carlos Gomez", resultado.getNombre());
            assertEquals(fotoValida, resultado.getFoto());
            verify(usuarioRepository).save(nuevoUsuario);
        }

        @Test
        void crearUsuario_ConFotoNula_Exitoso() {
            Usuario nuevoUsuario = Usuario.builder()
                    .nombre("Carlos Gomez")
                    .email("carlos@gmail.com")
                    .build();

            when(usuarioRepository.existsByEmail("carlos@gmail.com")).thenReturn(false);
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Usuario resultado = usuarioService.crearUsuario(nuevoUsuario, null);

            assertNotNull(resultado);
            assertNull(resultado.getFoto());
            verify(imagenRepository, never()).findById(any());
            verify(usuarioRepository).save(nuevoUsuario);
        }

        @Test
        void crearUsuario_ConFotoInvalida_LanzaException() {
            Usuario nuevoUsuario = Usuario.builder()
                    .nombre("Carlos Gomez")
                    .email("carlos@gmail.com")
                    .build();
            UUID fotoIdInexistente = UUID.randomUUID();

            when(usuarioRepository.existsByEmail("carlos@gmail.com")).thenReturn(false);
            when(imagenRepository.findById(fotoIdInexistente)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () ->
                    usuarioService.crearUsuario(nuevoUsuario, fotoIdInexistente));
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        void crearUsuario_Error_EmailDuplicado_LanzaBusinessException() {
            when(usuarioRepository.existsByEmail("pedro@gmail.com")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    usuarioService.crearUsuario(usuarioValido, fotoValida.getId()));
            assertEquals("EMAIL_ALREADY_EXISTS", ex.errorCode());
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    @Nested
    class Actualizar {

        @Test
        void actualizar_Exitoso() {
            Usuario detallesActualizados = Usuario.builder()
                    .nombre("Pedro Perez Actualizado")
                    .email("pedro@gmail.com")
                    .telefono("111222333")
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(imagenRepository.findById(fotoValida.getId())).thenReturn(Optional.of(fotoValida));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Usuario resultado = usuarioService.actualizar(1L, detallesActualizados, fotoValida.getId(), false);

            assertNotNull(resultado);
            assertEquals("Pedro Perez Actualizado", resultado.getNombre());
            assertEquals("111222333", resultado.getTelefono());
            assertFalse(resultado.isEstado());
            verify(usuarioRepository).save(usuarioValido);
        }

        @Test
        void actualizar_MismoEmail_NoValidaDuplicado_Exitoso() {
            Usuario detallesMismoEmail = Usuario.builder()
                    .nombre("Pedro Perez Actualizado")
                    .email("pedro@gmail.com") // Mismo Email
                    .telefono("111222333")
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Usuario resultado = usuarioService.actualizar(1L, detallesMismoEmail, null, null);

            assertNotNull(resultado);
            assertTrue(resultado.isEstado()); // No cambia porque pasamos null
            assertNull(resultado.getFoto()); // Foto cambia a nulo al pasar null
            verify(usuarioRepository, never()).existsByEmail(anyString());
            verify(usuarioRepository).save(usuarioValido);
        }

        @Test
        void actualizar_Error_EmailDuplicado_LanzaBusinessException() {
            Usuario detallesActualizados = Usuario.builder()
                    .nombre("Pedro Perez")
                    .email("carlos@gmail.com")
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(usuarioRepository.existsByEmail("carlos@gmail.com")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    usuarioService.actualizar(1L, detallesActualizados, fotoValida.getId(), true));
            assertEquals("EMAIL_ALREADY_EXISTS", ex.errorCode());
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        void actualizar_FotoInvalida_LanzaException() {
            Usuario detallesActualizados = Usuario.builder()
                    .nombre("Pedro Perez")
                    .email("pedro@gmail.com")
                    .build();
            UUID fotoIdInexistente = UUID.randomUUID();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(imagenRepository.findById(fotoIdInexistente)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () ->
                    usuarioService.actualizar(1L, detallesActualizados, fotoIdInexistente, true));
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    @Nested
    class Eliminar {

        @Test
        void eliminar_SinPrestamosActivos_Exitoso() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.existsByUsuarioIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(false);
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            usuarioService.eliminar(1L);

            assertFalse(usuarioValido.isEstado());
            verify(usuarioRepository).save(usuarioValido);
        }

        @Test
        void eliminar_ConPrestamosActivos_LanzaBusinessException() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.existsByUsuarioIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> usuarioService.eliminar(1L));
            assertEquals("USER_HAS_ACTIVE_LOANS", ex.errorCode());
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    @Nested
    class Activar {

        @Test
        void activar_Exitoso() {
            usuarioValido.setEstado(false);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Usuario resultado = usuarioService.activar(1L);

            assertTrue(resultado.isEstado());
            verify(usuarioRepository).save(usuarioValido);
        }
    }

    @Nested
    class ObtenerCupoPrestamo {

        @Test
        void obtenerCupoPrestamo_Exitoso() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.countTotalLibrosEnPoseccion(1L)).thenReturn(2L);
            when(prestamoRepository.findLibrosIdsPrestadosActivos(1L)).thenReturn(List.of(10L, 11L));

            UsuarioCupoPrestamoResponse resultado = usuarioService.obtenerCupoPrestamo(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.usuarioId());
            assertEquals(5L, resultado.maximoPermitido());
            assertEquals(2L, resultado.librosEnPosesion());
            assertEquals(3L, resultado.cupoDisponible());
            assertEquals(2, resultado.librosPrestadosIds().size());
        }

        @Test
        void obtenerCupoPrestamo_UsuarioNoEncontrado_LanzaException() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> usuarioService.obtenerCupoPrestamo(99L));
        }
    }
}
