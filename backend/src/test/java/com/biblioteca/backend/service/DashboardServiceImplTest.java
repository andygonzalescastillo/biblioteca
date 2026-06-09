package com.biblioteca.backend.service;

import com.biblioteca.backend.dto.response.DashboardLibroInventarioResponse;
import com.biblioteca.backend.dto.response.DashboardRankingResponse;
import com.biblioteca.backend.dto.response.DashboardResumenResponse;
import com.biblioteca.backend.dto.response.PrestamoResponse;
import com.biblioteca.backend.entity.EstadoPrestamo;
import com.biblioteca.backend.entity.Prestamo;
import com.biblioteca.backend.mapper.PrestamoMapper;
import com.biblioteca.backend.repository.*;
import com.biblioteca.backend.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AutorRepository autorRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private DetallePrestamoRepository detallePrestamoRepository;

    @Mock
    private PrestamoMapper prestamoMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Nested
    class ObtenerResumen {

        @Test
        void obtenerResumen_ConDatosYColeccionesCargadas_Exitoso() {
            // Arrange
            when(libroRepository.count()).thenReturn(100L);
            when(libroRepository.countByEstadoTrue()).thenReturn(95L);
            when(libroRepository.sumStockActivo()).thenReturn(500L);
            when(usuarioRepository.count()).thenReturn(50L);
            when(usuarioRepository.countByEstadoTrue()).thenReturn(48L);
            when(autorRepository.count()).thenReturn(20L);
            when(categoriaRepository.count()).thenReturn(10L);

            when(prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO)).thenReturn(15L);
            when(prestamoRepository.countPrestamosActivosVencidos(any(Instant.class))).thenReturn(2L);
            when(prestamoRepository.countPrestamosActivosPorVencer(any(Instant.class), any(Instant.class))).thenReturn(3L);
            when(prestamoRepository.countByEstadoAndFechaDevolucionRealBetween(eq(EstadoPrestamo.DEVUELTO), any(Instant.class), any(Instant.class))).thenReturn(10L);
            when(libroRepository.countByEstadoTrueAndStock(0)).thenReturn(5L);

            // Mock de préstamos recientes (IDs 1, 2)
            when(prestamoRepository.findIdsByFilters(
                    isNull(),
                    isNull(),
                    eq(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id")))
            )).thenReturn(new PageImpl<>(List.of(1L, 2L)));

            // Mock de préstamos vencidos (ID 3)
            when(prestamoRepository.findIdsPrestamosActivosVencidos(any(Instant.class), eq(PageRequest.of(0, 5))))
                    .thenReturn(new PageImpl<>(List.of(3L)));

            // Mock de préstamos próximos a vencer (ID 4)
            when(prestamoRepository.findIdsPrestamosActivosPorVencer(any(Instant.class), any(Instant.class), eq(PageRequest.of(0, 5))))
                    .thenReturn(new PageImpl<>(List.of(4L)));

            // Mock de carga de entidades por IDs
            Prestamo p1 = Prestamo.builder().id(1L).build();
            Prestamo p2 = Prestamo.builder().id(2L).build();
            Prestamo p3 = Prestamo.builder().id(3L).build();
            Prestamo p4 = Prestamo.builder().id(4L).build();

            when(prestamoRepository.findAllByIdInWithRelations(List.of(1L, 2L))).thenReturn(List.of(p1, p2));
            when(prestamoRepository.findAllByIdInWithRelations(List.of(3L))).thenReturn(List.of(p3));
            when(prestamoRepository.findAllByIdInWithRelations(List.of(4L))).thenReturn(List.of(p4));

            // Mapeo a Response
            PrestamoResponse pr1 = new PrestamoResponse(1L, null, null, null, null, null, null);
            PrestamoResponse pr2 = new PrestamoResponse(2L, null, null, null, null, null, null);
            PrestamoResponse pr3 = new PrestamoResponse(3L, null, null, null, null, null, null);
            PrestamoResponse pr4 = new PrestamoResponse(4L, null, null, null, null, null, null);

            when(prestamoMapper.toResponse(p1)).thenReturn(pr1);
            when(prestamoMapper.toResponse(p2)).thenReturn(pr2);
            when(prestamoMapper.toResponse(p3)).thenReturn(pr3);
            when(prestamoMapper.toResponse(p4)).thenReturn(pr4);

            // Mocks de Rankings y Alertas
            List<DashboardRankingResponse> librosMasPrestados = List.of(new DashboardRankingResponse(1L, "Libro A", 10L));
            List<DashboardRankingResponse> lectoresMasActivos = List.of(new DashboardRankingResponse(1L, "Lector A", 5L));
            List<DashboardRankingResponse> categoriasConMasLibros = List.of(new DashboardRankingResponse(1L, "Drama", 40L));
            List<DashboardRankingResponse> autoresConMasLibros = List.of(new DashboardRankingResponse(1L, "Autor A", 30L));
            List<DashboardLibroInventarioResponse> alertasInventario = List.of(new DashboardLibroInventarioResponse(1L, "Libro Bajo Stock", 2, "Drama"));

            when(detallePrestamoRepository.findLibrosMasPrestados(any(PageRequest.class))).thenReturn(librosMasPrestados);
            when(detallePrestamoRepository.findLectoresMasActivos(any(PageRequest.class))).thenReturn(lectoresMasActivos);
            when(libroRepository.findCategoriasConMasLibros(any(PageRequest.class))).thenReturn(categoriasConMasLibros);
            when(libroRepository.findAutoresConMasLibros(any(PageRequest.class))).thenReturn(autoresConMasLibros);
            when(libroRepository.findAlertasInventario(eq(5), any(PageRequest.class))).thenReturn(alertasInventario);

            // Act
            DashboardResumenResponse resultado = dashboardService.obtenerResumen();

            // Assert
            assertNotNull(resultado);
            assertEquals(100L, resultado.totalLibros());
            assertEquals(95L, resultado.totalLibrosActivos());
            assertEquals(500L, resultado.totalEjemplares());
            assertEquals(50L, resultado.totalUsuarios());
            assertEquals(48L, resultado.usuariosActivos());
            assertEquals(20L, resultado.totalAutores());
            assertEquals(10L, resultado.totalCategorias());
            assertEquals(15L, resultado.prestamosActivos());
            assertEquals(2L, resultado.prestamosAtrasados());
            assertEquals(3L, resultado.prestamosPorVencer());
            assertEquals(10L, resultado.prestamosDevueltosEsteMes());
            assertEquals(5L, resultado.librosAgotados());
            
            // Verificaciones de listas pobladas
            assertEquals(2, resultado.prestamosRecientes().size());
            assertEquals(1, resultado.prestamosVencidos().size());
            assertEquals(1, resultado.prestamosProximosAVencer().size());
            assertEquals(1L, resultado.prestamosRecientes().getFirst().id());
            assertEquals(3L, resultado.prestamosVencidos().getFirst().id());
            assertEquals(4L, resultado.prestamosProximosAVencer().getFirst().id());

            assertEquals("Libro A", resultado.librosMasPrestados().getFirst().nombre());
            assertEquals("Lector A", resultado.lectoresMasActivos().getFirst().nombre());
            assertEquals("Drama", resultado.categoriasConMasLibros().getFirst().nombre());
            assertEquals("Autor A", resultado.autoresConMasLibros().getFirst().nombre());
            assertEquals("Libro Bajo Stock", resultado.alertasInventario().getFirst().titulo());
        }

        @Test
        void obtenerResumen_ConListasVacias_Exitoso() {
            when(libroRepository.count()).thenReturn(0L);
            when(libroRepository.countByEstadoTrue()).thenReturn(0L);
            when(libroRepository.sumStockActivo()).thenReturn(0L);
            when(usuarioRepository.count()).thenReturn(0L);
            when(usuarioRepository.countByEstadoTrue()).thenReturn(0L);
            when(autorRepository.count()).thenReturn(0L);
            when(categoriaRepository.count()).thenReturn(0L);

            when(prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO)).thenReturn(0L);
            when(prestamoRepository.countPrestamosActivosVencidos(any(Instant.class))).thenReturn(0L);
            when(prestamoRepository.countPrestamosActivosPorVencer(any(Instant.class), any(Instant.class))).thenReturn(0L);
            when(prestamoRepository.countByEstadoAndFechaDevolucionRealBetween(eq(EstadoPrestamo.DEVUELTO), any(Instant.class), any(Instant.class))).thenReturn(0L);
            when(libroRepository.countByEstadoTrueAndStock(0)).thenReturn(0L);

            when(prestamoRepository.findIdsByFilters(isNull(), isNull(), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));
            when(prestamoRepository.findIdsPrestamosActivosVencidos(any(Instant.class), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));
            when(prestamoRepository.findIdsPrestamosActivosPorVencer(any(Instant.class), any(Instant.class), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            when(detallePrestamoRepository.findLibrosMasPrestados(any(PageRequest.class))).thenReturn(Collections.emptyList());
            when(detallePrestamoRepository.findLectoresMasActivos(any(PageRequest.class))).thenReturn(Collections.emptyList());
            when(libroRepository.findCategoriasConMasLibros(any(PageRequest.class))).thenReturn(Collections.emptyList());
            when(libroRepository.findAutoresConMasLibros(any(PageRequest.class))).thenReturn(Collections.emptyList());
            when(libroRepository.findAlertasInventario(eq(5), any(PageRequest.class))).thenReturn(Collections.emptyList());

            DashboardResumenResponse resultado = dashboardService.obtenerResumen();

            assertNotNull(resultado);
            assertEquals(0L, resultado.totalLibros());
            assertTrue(resultado.prestamosRecientes().isEmpty());
            assertTrue(resultado.prestamosVencidos().isEmpty());
            assertTrue(resultado.prestamosProximosAVencer().isEmpty());
            assertTrue(resultado.librosMasPrestados().isEmpty());
            assertTrue(resultado.alertasInventario().isEmpty());
            
            // Verificar que no se llame a findAllByIdInWithRelations ni mapper si la lista de IDs está vacía
            verify(prestamoRepository, never()).findAllByIdInWithRelations(any());
            verify(prestamoMapper, never()).toResponse(any());
        }
    }
}
