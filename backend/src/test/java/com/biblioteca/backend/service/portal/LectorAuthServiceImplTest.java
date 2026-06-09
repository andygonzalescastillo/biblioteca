package com.biblioteca.backend.service.portal;

import com.biblioteca.backend.entity.Usuario;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.service.UsuarioService;
import com.biblioteca.backend.service.portal.impl.LectorAuthServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LectorAuthServiceImplTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private LectorAuthServiceImpl lectorAuthService;

    @Nested
    class AutenticarPorEmail {

        @Test
        void autenticarPorEmail_ConEmailValido_DebeRetornarUsuarioActivo() {
            Usuario usuario = Usuario.builder()
                    .id(1L).nombre("Andy Gonzales")
                    .email("andy@gmail.com").estado(true).build();

            when(usuarioService.obtenerPorEmail("andy@gmail.com")).thenReturn(usuario);

            Usuario resultado = lectorAuthService.autenticarPorEmail("andy@gmail.com");

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            assertEquals("andy@gmail.com", resultado.getEmail());
            verify(usuarioService).obtenerPorEmail("andy@gmail.com");
        }

        @Test
        void autenticarPorEmail_DebeNormalizarEmailAMinusculas() {
            Usuario usuario = Usuario.builder()
                    .id(1L).email("andy@gmail.com").estado(true).build();

            when(usuarioService.obtenerPorEmail("andy@gmail.com")).thenReturn(usuario);

            Usuario resultado = lectorAuthService.autenticarPorEmail("  ANDY@GMAIL.COM  ");

            verify(usuarioService).obtenerPorEmail("andy@gmail.com");
            assertEquals("andy@gmail.com", resultado.getEmail());
        }

        @Test
        void autenticarPorEmail_ConEmailNull_DebeArrojarBusinessException() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorAuthService.autenticarPorEmail(null));

            assertEquals("EMAIL_REQUIRED", ex.errorCode());
            verifyNoInteractions(usuarioService);
        }

        @Test
        void autenticarPorEmail_ConEmailEnBlanco_DebeArrojarBusinessException() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorAuthService.autenticarPorEmail("   "));

            assertEquals("EMAIL_REQUIRED", ex.errorCode());
            verifyNoInteractions(usuarioService);
        }

        @Test
        void autenticarPorEmail_CuandoEmailNoRegistrado_DebeArrojarReaderNotFound() {
            when(usuarioService.obtenerPorEmail("noexiste@gmail.com"))
                    .thenThrow(new EntityNotFoundException("Usuario no encontrado"));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorAuthService.autenticarPorEmail("noexiste@gmail.com"));

            assertEquals("READER_NOT_FOUND", ex.errorCode());
            verify(usuarioService).obtenerPorEmail("noexiste@gmail.com");
        }

        @Test
        void autenticarPorEmail_CuandoUsuarioInactivo_DebeArrojarUserInactive() {
            Usuario usuarioInactivo = Usuario.builder()
                    .id(2L).email("inactivo@gmail.com").estado(false).build();

            when(usuarioService.obtenerPorEmail("inactivo@gmail.com")).thenReturn(usuarioInactivo);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorAuthService.autenticarPorEmail("inactivo@gmail.com"));

            assertEquals("USER_INACTIVE", ex.errorCode());
            verify(usuarioService).obtenerPorEmail("inactivo@gmail.com");
        }
    }

    @Nested
    class ObtenerLector {

        @Test
        void obtenerLector_CuandoExiste_DebeRetornarUsuario() {
            Usuario usuario = Usuario.builder()
                    .id(5L).nombre("Carlos Lopez").email("carlos@gmail.com").estado(true).build();

            when(usuarioService.obtenerPorId(5L)).thenReturn(usuario);

            Usuario resultado = lectorAuthService.obtenerLector(5L);

            assertNotNull(resultado);
            assertEquals(5L, resultado.getId());
            assertEquals("Carlos Lopez", resultado.getNombre());
            verify(usuarioService).obtenerPorId(5L);
        }

        @Test
        void obtenerLector_CuandoNoExiste_DebePropagarEntityNotFoundException() {
            when(usuarioService.obtenerPorId(99L))
                    .thenThrow(new EntityNotFoundException("Lector no encontrado"));

            assertThrows(EntityNotFoundException.class,
                    () -> lectorAuthService.obtenerLector(99L));

            verify(usuarioService).obtenerPorId(99L);
        }
    }
}
