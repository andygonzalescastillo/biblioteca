package com.biblioteca.backend.service.portal;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.entity.Libro;
import com.biblioteca.backend.entity.Prestamo;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.service.PrestamoService;
import com.biblioteca.backend.service.portal.impl.LectorPrestamoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LectorPrestamoServiceImplTest {

    @Mock
    private PrestamoService prestamoService;

    @Mock
    private LectorCarritoService lectorCarritoService;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private LectorPrestamoServiceImpl lectorPrestamoService;

    private final AppProperties.Prestamo prestamoConfig =
            new AppProperties.Prestamo(7, 1, 30, 3, 5);

    @BeforeEach
    void setUp() {
        when(appProperties.prestamo()).thenReturn(prestamoConfig);
    }

    private Libro libro(Long id, String titulo) {
        return Libro.builder().id(id).titulo(titulo).stock(3).estado(true).build();
    }

    private Prestamo prestamo(Long id) {
        Prestamo p = new Prestamo();
        p.setId(id);
        return p;
    }

    @Nested
    class RegistrarPrestamoDesdeCarrito {

        @Test
        void registrarPrestamo_ConCarritoValido_DebeRegistrarYRetornarPrestamo() {
            List<Long> librosIds = List.of(1L, 2L);
            Libro libro1 = libro(1L, "Java");
            Libro libro2 = libro(2L, "Spring");
            Prestamo prestamoGuardado = prestamo(100L);

            when(lectorCarritoService.obtenerLibrosCarrito(librosIds))
                    .thenReturn(List.of(libro1, libro2));
            when(prestamoService.registrarPrestamo(any(PrestamoService.PrestamoRequest.class)))
                    .thenReturn(prestamoGuardado);

            Prestamo resultado = lectorPrestamoService.registrarPrestamoDesdeCarrito(1L, librosIds, 7);

            assertNotNull(resultado);
            assertEquals(100L, resultado.getId());
            verify(prestamoService).registrarPrestamo(any(PrestamoService.PrestamoRequest.class));
        }

        @Test
        void registrarPrestamo_ConCarritoNull_DebeArrojarEmptyCart() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorPrestamoService.registrarPrestamoDesdeCarrito(1L, null, 7));

            assertEquals("EMPTY_CART", ex.errorCode());
            verifyNoInteractions(prestamoService, lectorCarritoService);
        }

        @Test
        void registrarPrestamo_ConCarritoVacio_DebeArrojarEmptyCart() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorPrestamoService.registrarPrestamoDesdeCarrito(1L, List.of(), 7));

            assertEquals("EMPTY_CART", ex.errorCode());
            verifyNoInteractions(prestamoService, lectorCarritoService);
        }

        @Test
        void registrarPrestamo_ConDiasNull_DebeUsarDiasDefault() {
            Libro libro = libro(1L, "Libro");
            Prestamo prestamoGuardado = prestamo(50L);

            when(lectorCarritoService.obtenerLibrosCarrito(any())).thenReturn(List.of(libro));
            when(prestamoService.registrarPrestamo(any())).thenReturn(prestamoGuardado);

            lectorPrestamoService.registrarPrestamoDesdeCarrito(1L, List.of(1L), null);

            verify(prestamoService).registrarPrestamo(
                    argThat(req -> req.diasLimite() == 7)
            );
        }

        @Test
        void registrarPrestamo_ConDiasMenoresAlMinimo_DebeArrojarInvalidLoanDays() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorPrestamoService.registrarPrestamoDesdeCarrito(1L, List.of(1L), 0));

            assertEquals("INVALID_LOAN_DAYS", ex.errorCode());
            verifyNoInteractions(prestamoService, lectorCarritoService);
        }

        @Test
        void registrarPrestamo_ConDiasMayoresAlMaximo_DebeArrojarInvalidLoanDays() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorPrestamoService.registrarPrestamoDesdeCarrito(1L, List.of(1L), 31));

            assertEquals("INVALID_LOAN_DAYS", ex.errorCode());
            verifyNoInteractions(prestamoService, lectorCarritoService);
        }

        @Test
        void registrarPrestamo_ConDiasExactosDentroDeRango_DebeRegistrarCorrectamente() {
            Libro libro = libro(1L, "Test");
            Prestamo prestamoGuardado = prestamo(99L);

            when(lectorCarritoService.obtenerLibrosCarrito(any())).thenReturn(List.of(libro));
            when(prestamoService.registrarPrestamo(any())).thenReturn(prestamoGuardado);

            assertDoesNotThrow(() ->
                    lectorPrestamoService.registrarPrestamoDesdeCarrito(1L, List.of(1L), 1));
            assertDoesNotThrow(() ->
                    lectorPrestamoService.registrarPrestamoDesdeCarrito(1L, List.of(1L), 30));
        }

        @Test
        void registrarPrestamo_ConLibrosDuplicados_DebeAgruparCantidades() {
            List<Long> librosIds = List.of(1L, 1L);
            Libro libro = libro(1L, "Duplicado");
            Prestamo prestamoGuardado = prestamo(77L);

            when(lectorCarritoService.obtenerLibrosCarrito(librosIds)).thenReturn(List.of(libro));
            when(prestamoService.registrarPrestamo(any())).thenReturn(prestamoGuardado);

            lectorPrestamoService.registrarPrestamoDesdeCarrito(1L, librosIds, 7);

            verify(prestamoService).registrarPrestamo(
                    argThat(req -> req.detalles().stream()
                            .filter(d -> d.libroId().equals(1L))
                            .allMatch(d -> d.cantidad() == 2))
            );
        }
    }

    @Nested
    class ObtenerPrestamosDelLector {

        @Test
        void obtenerPrestamosDelLector_DebeRetornarPrestamosDelUsuario() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Prestamo> pagina = new PageImpl<>(List.of(prestamo(100L), prestamo(200L)));

            when(prestamoService.obtenerTodos(eq(5L), isNull(), eq(pageable))).thenReturn(pagina);

            Page<Prestamo> resultado = lectorPrestamoService.obtenerPrestamosDelLector(5L, pageable);

            assertEquals(2, resultado.getTotalElements());
            assertEquals(100L, resultado.getContent().getFirst().getId());
            verify(prestamoService).obtenerTodos(5L, null, pageable);
        }

        @Test
        void obtenerPrestamosDelLector_CuandoNoTienePrestamos_DebeRetornarPaginaVacia() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Prestamo> pagina = Page.empty(pageable);

            when(prestamoService.obtenerTodos(eq(99L), isNull(), eq(pageable))).thenReturn(pagina);

            Page<Prestamo> resultado = lectorPrestamoService.obtenerPrestamosDelLector(99L, pageable);

            assertTrue(resultado.isEmpty());
        }
    }
}
