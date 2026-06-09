package com.biblioteca.backend.service.portal;

import com.biblioteca.backend.entity.Libro;
import com.biblioteca.backend.entity.Usuario;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.PrestamoRepository;
import com.biblioteca.backend.service.portal.impl.LectorCarritoServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LectorCarritoServiceImplTest {

    @Mock
    private LectorCatalogoService lectorCatalogoService;

    @Mock
    private LectorAuthService lectorAuthService;

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private LectorCarritoServiceImpl lectorCarritoService;

    private Libro libroConStock(Long id, String titulo, Integer stock) {
        return Libro.builder().id(id).titulo(titulo).stock(stock).estado(true).build();
    }

    private Usuario usuarioActivo(Long id) {
        return Usuario.builder().id(id).nombre("Andy").email("andy@gmail.com").estado(true).build();
    }

    @Nested
    class ObtenerLibroAgregable {

        @Test
        void obtenerLibroAgregable_SinLector_ConStock_DebeRetornarLibro() {
            Libro libro = libroConStock(1L, "Java", 5);
            when(lectorCatalogoService.obtenerLibroActivo(1L)).thenReturn(libro);

            Libro resultado = lectorCarritoService.obtenerLibroAgregable(1L, null);

            assertEquals("Java", resultado.getTitulo());
            verify(lectorCatalogoService).obtenerLibroActivo(1L);
            verifyNoInteractions(lectorAuthService, prestamoRepository);
        }

        @Test
        void obtenerLibroAgregable_SinStock_DebeArrojarInsufficientStock() {
            Libro libro = libroConStock(2L, "Agotado", 0);
            when(lectorCatalogoService.obtenerLibroActivo(2L)).thenReturn(libro);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorCarritoService.obtenerLibroAgregable(2L, null));

            assertEquals("INSUFFICIENT_STOCK", ex.errorCode());
        }

        @Test
        void obtenerLibroAgregable_ConStockNulo_DebeArrojarInsufficientStock() {
            Libro libro = libroConStock(2L, "Agotado", null);
            when(lectorCatalogoService.obtenerLibroActivo(2L)).thenReturn(libro);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorCarritoService.obtenerLibroAgregable(2L, null));

            assertEquals("INSUFFICIENT_STOCK", ex.errorCode());
        }

        @Test
        void obtenerLibroAgregable_ConLectorActivo_SinDeudas_DebeRetornarLibro() {
            Libro libro = libroConStock(1L, "Spring Boot", 3);
            Usuario lector = usuarioActivo(10L);

            when(lectorCatalogoService.obtenerLibroActivo(1L)).thenReturn(libro);
            when(lectorAuthService.obtenerLector(10L)).thenReturn(lector);
            when(prestamoRepository.tienePrestamosVencidos(eq(10L), any(Instant.class))).thenReturn(false);
            when(prestamoRepository.tieneLibroPrestadoActivo(10L, 1L)).thenReturn(false);

            Libro resultado = lectorCarritoService.obtenerLibroAgregable(1L, 10L);

            assertEquals("Spring Boot", resultado.getTitulo());
            verify(prestamoRepository).tienePrestamosVencidos(eq(10L), any(Instant.class));
            verify(prestamoRepository).tieneLibroPrestadoActivo(10L, 1L);
        }

        @Test
        void obtenerLibroAgregable_ConLectorInactivo_DebeArrojarUserInactive() {
            Libro libro = libroConStock(1L, "Libro", 2);
            Usuario lectorInactivo = Usuario.builder().id(5L).estado(false).build();

            when(lectorCatalogoService.obtenerLibroActivo(1L)).thenReturn(libro);
            when(lectorAuthService.obtenerLector(5L)).thenReturn(lectorInactivo);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorCarritoService.obtenerLibroAgregable(1L, 5L));

            assertEquals("USER_INACTIVE", ex.errorCode());
            verifyNoInteractions(prestamoRepository);
        }

        @Test
        void obtenerLibroAgregable_ConPrestamosVencidos_DebeArrojarOverdueLoans() {
            Libro libro = libroConStock(1L, "Libro", 2);
            Usuario lector = usuarioActivo(7L);

            when(lectorCatalogoService.obtenerLibroActivo(1L)).thenReturn(libro);
            when(lectorAuthService.obtenerLector(7L)).thenReturn(lector);
            when(prestamoRepository.tienePrestamosVencidos(eq(7L), any(Instant.class))).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorCarritoService.obtenerLibroAgregable(1L, 7L));

            assertEquals("USER_HAS_OVERDUE_LOANS", ex.errorCode());
            verify(prestamoRepository, never()).tieneLibroPrestadoActivo(anyLong(), anyLong());
        }

        @Test
        void obtenerLibroAgregable_CuandoLibroYaPrestadoAlLector_DebeArrojarAlreadyLoaned() {
            Libro libro = libroConStock(1L, "Libro Duplicado", 4);
            Usuario lector = usuarioActivo(8L);

            when(lectorCatalogoService.obtenerLibroActivo(1L)).thenReturn(libro);
            when(lectorAuthService.obtenerLector(8L)).thenReturn(lector);
            when(prestamoRepository.tienePrestamosVencidos(eq(8L), any(Instant.class))).thenReturn(false);
            when(prestamoRepository.tieneLibroPrestadoActivo(8L, 1L)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorCarritoService.obtenerLibroAgregable(1L, 8L));

            assertEquals("BOOK_ALREADY_LOANED_BY_USER", ex.errorCode());
        }

        @Test
        void obtenerLibroAgregable_LibroNoExiste_PropagaException() {
            when(lectorCatalogoService.obtenerLibroActivo(99L)).thenThrow(new EntityNotFoundException("Libro no existe"));

            assertThrows(EntityNotFoundException.class,
                    () -> lectorCarritoService.obtenerLibroAgregable(99L, null));
        }

        @Test
        void obtenerLibroAgregable_LectorNoExiste_PropagaException() {
            Libro libro = libroConStock(1L, "Java", 5);
            when(lectorCatalogoService.obtenerLibroActivo(1L)).thenReturn(libro);
            when(lectorAuthService.obtenerLector(99L)).thenThrow(new EntityNotFoundException("Lector no existe"));

            assertThrows(EntityNotFoundException.class,
                    () -> lectorCarritoService.obtenerLibroAgregable(1L, 99L));
        }
    }

    @Nested
    class ObtenerLibrosCarrito {

        @Test
        void obtenerLibrosCarrito_ConListaNull_DebeRetornarListaVacia() {
            List<Libro> resultado = lectorCarritoService.obtenerLibrosCarrito(null);

            assertTrue(resultado.isEmpty());
            verifyNoInteractions(lectorCatalogoService);
        }

        @Test
        void obtenerLibrosCarrito_ConListaVacia_DebeRetornarListaVacia() {
            List<Libro> resultado = lectorCarritoService.obtenerLibrosCarrito(List.of());

            assertTrue(resultado.isEmpty());
            verifyNoInteractions(lectorCatalogoService);
        }

        @Test
        void obtenerLibrosCarrito_ConIds_DebeRetornarLibrosCorrespondientes() {
            Libro libro1 = libroConStock(1L, "Libro A", 3);
            Libro libro2 = libroConStock(2L, "Libro B", 5);

            when(lectorCatalogoService.obtenerLibroActivo(1L)).thenReturn(libro1);
            when(lectorCatalogoService.obtenerLibroActivo(2L)).thenReturn(libro2);

            List<Libro> resultado = lectorCarritoService.obtenerLibrosCarrito(List.of(1L, 2L));

            assertEquals(2, resultado.size());
            verify(lectorCatalogoService).obtenerLibroActivo(1L);
            verify(lectorCatalogoService).obtenerLibroActivo(2L);
        }

        @Test
        void obtenerLibrosCarrito_ConIdsDuplicados_DebeDeduplicar() {
            Libro libro = libroConStock(1L, "Único", 2);
            when(lectorCatalogoService.obtenerLibroActivo(1L)).thenReturn(libro);

            List<Libro> resultado = lectorCarritoService.obtenerLibrosCarrito(List.of(1L, 1L, 1L));

            assertEquals(1, resultado.size());
            verify(lectorCatalogoService, times(1)).obtenerLibroActivo(1L);
        }
    }
}
