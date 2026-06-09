package com.biblioteca.backend.service;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.entity.*;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.PrestamoRepository;
import com.biblioteca.backend.repository.UsuarioRepository;
import com.biblioteca.backend.service.PrestamoService.DetalleRequest;
import com.biblioteca.backend.service.PrestamoService.PrestamoRequest;
import com.biblioteca.backend.service.impl.PrestamoServiceImpl;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceImplTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LibroService libroService;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private PrestamoServiceImpl prestamoService;

    private AppProperties.Prestamo prestamoProps;
    private Usuario usuarioValido;
    private Libro libroValido;

    @BeforeEach
    void setUp() {
        prestamoProps = new AppProperties.Prestamo(14, 1, 30, 3, 5);
        lenient().when(appProperties.prestamo()).thenReturn(prestamoProps);

        usuarioValido = Usuario.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@gmail.com")
                .estado(true)
                .build();

        libroValido = Libro.builder()
                .id(1L)
                .titulo("Cien años de soledad")
                .isbn("978-3-16-148410-0")
                .estado(true)
                .stock(10)
                .build();
    }

    @Nested
    class ObtenerTodos {

        @Test
        void obtenerTodos_Exitoso() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> idsPage = new PageImpl<>(List.of(100L));
            when(prestamoRepository.findIdsByFilters(1L, EstadoPrestamo.ACTIVO, pageable)).thenReturn(idsPage);

            Prestamo prestamo = Prestamo.builder().id(100L).build();
            when(prestamoRepository.findAllByIdInWithRelations(List.of(100L))).thenReturn(List.of(prestamo));

            Page<Prestamo> resultado = prestamoService.obtenerTodos(1L, EstadoPrestamo.ACTIVO, pageable);

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals(100L, resultado.getContent().getFirst().getId());
        }

        @Test
        void obtenerTodos_Vacio() {
            Pageable pageable = PageRequest.of(0, 10);
            when(prestamoRepository.findIdsByFilters(1L, EstadoPrestamo.ACTIVO, pageable)).thenReturn(Page.empty(pageable));

            Page<Prestamo> resultado = prestamoService.obtenerTodos(1L, EstadoPrestamo.ACTIVO, pageable);

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            verify(prestamoRepository, never()).findAllByIdInWithRelations(any());
        }
    }

    @Nested
    class ObtenerPorId {

        @Test
        void obtenerPorId_Exitoso() {
            Prestamo prestamo = Prestamo.builder().id(100L).build();
            when(prestamoRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(prestamo));

            Prestamo resultado = prestamoService.obtenerPorId(100L);

            assertEquals(100L, resultado.getId());
        }

        @Test
        void obtenerPorId_Error_NoEncontrado_LanzaException() {
            when(prestamoRepository.findByIdWithRelations(100L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> prestamoService.obtenerPorId(100L));
        }
    }

    @Nested
    class RegistrarPrestamo {

        @Test
        void registrarPrestamo_Exitoso() {
            DetalleRequest detalleReq = new DetalleRequest(1L, 2);
            PrestamoRequest request = new PrestamoRequest(1L, 15, List.of(detalleReq));

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.tienePrestamosVencidos(eq(1L), any(Instant.class))).thenReturn(false);
            when(prestamoRepository.countTotalLibrosEnPoseccion(1L)).thenReturn(0L);
            when(libroService.obtenerPorId(1L)).thenReturn(libroValido);
            when(prestamoRepository.tieneLibroPrestadoActivo(1L, 1L)).thenReturn(false);

            Prestamo prestamoGuardado = Prestamo.builder()
                    .id(100L)
                    .usuario(usuarioValido)
                    .fechaPrestamo(Instant.now())
                    .estado(EstadoPrestamo.ACTIVO)
                    .build();
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamoGuardado);

            Prestamo resultado = prestamoService.registrarPrestamo(request);

            assertNotNull(resultado);
            assertEquals(100L, resultado.getId());
            verify(usuarioRepository).findById(1L);
            verify(libroService).actualizarStock(1L, -2);
            verify(prestamoRepository).save(any(Prestamo.class));
        }

        @Test
        void registrarPrestamo_Error_DetallesVacios_LanzaBusinessException() {
            PrestamoRequest request = new PrestamoRequest(1L, 15, Collections.emptyList());

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("EMPTY_LOAN_DETAILS", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_DetallesNulos_LanzaBusinessException() {
            PrestamoRequest request = new PrestamoRequest(1L, 15, null);

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("EMPTY_LOAN_DETAILS", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_UsuarioNoEncontrado_LanzaException() {
            DetalleRequest detalleReq = new DetalleRequest(1L, 2);
            PrestamoRequest request = new PrestamoRequest(1L, 15, List.of(detalleReq));

            when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> prestamoService.registrarPrestamo(request));
        }

        @Test
        void registrarPrestamo_Error_UsuarioInactivo_LanzaBusinessException() {
            usuarioValido.setEstado(false);
            DetalleRequest detalleReq = new DetalleRequest(1L, 2);
            PrestamoRequest request = new PrestamoRequest(1L, 15, List.of(detalleReq));

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("USER_INACTIVE", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_DiasLimiteMuyAlto_LanzaBusinessException() {
            DetalleRequest detalleReq = new DetalleRequest(1L, 2);
            PrestamoRequest request = new PrestamoRequest(1L, 40, List.of(detalleReq)); // 40 días es > max 30

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("INVALID_LOAN_DAYS", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_DiasLimiteMuyBajo_LanzaBusinessException() {
            DetalleRequest detalleReq = new DetalleRequest(1L, 2);
            PrestamoRequest request = new PrestamoRequest(1L, 0, List.of(detalleReq)); // 0 días es < min 1

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("INVALID_LOAN_DAYS", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_UsuarioTienePrestamosVencidos_LanzaBusinessException() {
            DetalleRequest detalleReq = new DetalleRequest(1L, 2);
            PrestamoRequest request = new PrestamoRequest(1L, 15, List.of(detalleReq));

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.tienePrestamosVencidos(eq(1L), any(Instant.class))).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("USER_HAS_OVERDUE_LOANS", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_ExcedeLimiteMaximoLibros_LanzaBusinessException() {
            DetalleRequest detalleReq = new DetalleRequest(1L, 3);
            PrestamoRequest request = new PrestamoRequest(1L, 15, List.of(detalleReq));

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.tienePrestamosVencidos(eq(1L), any(Instant.class))).thenReturn(false);
            when(prestamoRepository.countTotalLibrosEnPoseccion(1L)).thenReturn(3L); // 3 en posesión + 3 solicitados = 6 > max 5

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("MAX_CONCURRENT_LOANS_EXCEEDED", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_CantidadLibroInvalida_Cero_LanzaBusinessException() {
            DetalleRequest detalleReq = new DetalleRequest(1L, 0);
            PrestamoRequest request = new PrestamoRequest(1L, 15, List.of(detalleReq));

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.tienePrestamosVencidos(eq(1L), any(Instant.class))).thenReturn(false);
            when(prestamoRepository.countTotalLibrosEnPoseccion(1L)).thenReturn(0L);

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("INVALID_QUANTITY", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_CantidadExcedeMaximoReserva_LanzaBusinessException() {
            DetalleRequest detalleReq = new DetalleRequest(1L, 4); // 4 es > max reserva 3 por libro
            PrestamoRequest request = new PrestamoRequest(1L, 15, List.of(detalleReq));

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.tienePrestamosVencidos(eq(1L), any(Instant.class))).thenReturn(false);
            when(prestamoRepository.countTotalLibrosEnPoseccion(1L)).thenReturn(0L);

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("INVALID_QUANTITY", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_LibroInactivo_LanzaBusinessException() {
            DetalleRequest detalleReq = new DetalleRequest(1L, 2);
            PrestamoRequest request = new PrestamoRequest(1L, 15, List.of(detalleReq));

            libroValido.setEstado(false);

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.tienePrestamosVencidos(eq(1L), any(Instant.class))).thenReturn(false);
            when(prestamoRepository.countTotalLibrosEnPoseccion(1L)).thenReturn(0L);
            when(libroService.obtenerPorId(1L)).thenReturn(libroValido);

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("BOOK_INACTIVE", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_LibroYaPrestadoActivo_LanzaBusinessException() {
            DetalleRequest detalleReq = new DetalleRequest(1L, 2);
            PrestamoRequest request = new PrestamoRequest(1L, 15, List.of(detalleReq));

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.tienePrestamosVencidos(eq(1L), any(Instant.class))).thenReturn(false);
            when(prestamoRepository.countTotalLibrosEnPoseccion(1L)).thenReturn(0L);
            when(libroService.obtenerPorId(1L)).thenReturn(libroValido);
            when(prestamoRepository.tieneLibroPrestadoActivo(1L, 1L)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.registrarPrestamo(request));
            assertEquals("BOOK_ALREADY_LOANED_BY_USER", ex.errorCode());
        }

        @Test
        void registrarPrestamo_Error_LibroNoEncontrado_LanzaException() {
            DetalleRequest detalleReq = new DetalleRequest(99L, 2);
            PrestamoRequest request = new PrestamoRequest(1L, 15, List.of(detalleReq));

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(prestamoRepository.tienePrestamosVencidos(eq(1L), any(Instant.class))).thenReturn(false);
            when(prestamoRepository.countTotalLibrosEnPoseccion(1L)).thenReturn(0L);
            when(libroService.obtenerPorId(99L)).thenThrow(new EntityNotFoundException("Libro no encontrado"));

            assertThrows(EntityNotFoundException.class, () -> prestamoService.registrarPrestamo(request));
            verify(prestamoRepository, never()).save(any(Prestamo.class));
        }
    }

    @Nested
    class DevolverPrestamo {

        @Test
        void devolverPrestamo_Exitoso() {
            Prestamo prestamoActivo = Prestamo.builder()
                    .id(100L)
                    .usuario(usuarioValido)
                    .fechaPrestamo(Instant.now())
                    .estado(EstadoPrestamo.ACTIVO)
                    .detalles(new ArrayList<>())
                    .build();

            DetallePrestamo detalle = DetallePrestamo.builder()
                    .id(200L)
                    .prestamo(prestamoActivo)
                    .libro(libroValido)
                    .cantidad(2)
                    .build();
            prestamoActivo.getDetalles().add(detalle);

            when(prestamoRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(prestamoActivo));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Prestamo devuelto = prestamoService.devolverPrestamo(100L);

            assertNotNull(devuelto);
            assertEquals(EstadoPrestamo.DEVUELTO, devuelto.getEstado());
            assertNotNull(devuelto.getFechaDevolucionReal());
            verify(libroService).actualizarStock(1L, 2);
            verify(prestamoRepository).save(prestamoActivo);
        }

        @Test
        void devolverPrestamo_Error_YaDevuelto_LanzaBusinessException() {
            Prestamo prestamoYaDevuelto = Prestamo.builder()
                    .id(100L)
                    .estado(EstadoPrestamo.DEVUELTO)
                    .build();

            when(prestamoRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(prestamoYaDevuelto));

            BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.devolverPrestamo(100L));
            assertEquals("LOAN_ALREADY_RETURNED", ex.errorCode());
        }
    }
}
