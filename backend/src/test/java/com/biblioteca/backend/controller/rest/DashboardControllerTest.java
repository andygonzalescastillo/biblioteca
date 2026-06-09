package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.response.*;
import com.biblioteca.backend.entity.EstadoPrestamo;
import com.biblioteca.backend.service.DashboardService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(DashboardControllerTest.TestConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

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

    private DashboardResumenResponse buildResumen() {
        UsuarioResponse usuario = new UsuarioResponse(
                1L, "Andy Gonzales", "andy@gmail.com", null, null, Instant.now(), null, true);

        PrestamoResponse prestamo = new PrestamoResponse(
                10L, usuario,
                Instant.now(), Instant.now().plusSeconds(86400 * 7), null,
                EstadoPrestamo.ACTIVO, List.of());

        DashboardRankingResponse ranking = new DashboardRankingResponse(1L, "El Quijote", 5L);
        DashboardLibroInventarioResponse alerta = new DashboardLibroInventarioResponse(
                2L, "Java Avanzado", 1, "Tecnología");

        return new DashboardResumenResponse(
                100, 80, 350, 50, 45, 30, 10,
                12, 3, 2, 8, 5,
                List.of(prestamo),   // prestamosRecientes
                List.of(prestamo),   // prestamosVencidos
                List.of(prestamo),   // prestamosProximosAVencer
                List.of(ranking),    // librosMasPrestados
                List.of(ranking),    // lectoresMasActivos
                List.of(ranking),    // categoriasConMasLibros
                List.of(ranking),    // autoresConMasLibros
                List.of(alerta)      // alertasInventario
        );
    }

    @Nested
    class ObtenerResumen {

        @Test
        void obtenerResumen_DebeRetornarMetricasCompletas() throws Exception {
            DashboardResumenResponse resumen = buildResumen();
            when(dashboardService.obtenerResumen()).thenReturn(resumen);

            mockMvc.perform(get("/api/dashboard/resumen")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalLibros").value(100))
                    .andExpect(jsonPath("$.totalLibrosActivos").value(80))
                    .andExpect(jsonPath("$.totalEjemplares").value(350))
                    .andExpect(jsonPath("$.totalUsuarios").value(50))
                    .andExpect(jsonPath("$.usuariosActivos").value(45))
                    .andExpect(jsonPath("$.totalAutores").value(30))
                    .andExpect(jsonPath("$.totalCategorias").value(10))
                    .andExpect(jsonPath("$.prestamosActivos").value(12))
                    .andExpect(jsonPath("$.prestamosAtrasados").value(3))
                    .andExpect(jsonPath("$.prestamosPorVencer").value(2))
                    .andExpect(jsonPath("$.prestamosDevueltosEsteMes").value(8))
                    .andExpect(jsonPath("$.librosAgotados").value(5));

            verify(dashboardService).obtenerResumen();
        }

        @Test
        void obtenerResumen_DebeRetornarListasEmbebidas() throws Exception {
            DashboardResumenResponse resumen = buildResumen();
            when(dashboardService.obtenerResumen()).thenReturn(resumen);

            mockMvc.perform(get("/api/dashboard/resumen")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.prestamosRecientes[0].id").value(10L))
                    .andExpect(jsonPath("$.prestamosRecientes[0].estado").value("ACTIVO"))
                    .andExpect(jsonPath("$.librosMasPrestados[0].nombre").value("El Quijote"))
                    .andExpect(jsonPath("$.librosMasPrestados[0].total").value(5))
                    .andExpect(jsonPath("$.alertasInventario[0].titulo").value("Java Avanzado"))
                    .andExpect(jsonPath("$.alertasInventario[0].stock").value(1))
                    .andExpect(jsonPath("$.alertasInventario[0].categoriaNombre").value("Tecnología"));

            verify(dashboardService).obtenerResumen();
        }

        @Test
        void obtenerResumen_CuandoServicioFalla_DebeRetornarErrorInterno() throws Exception {
            when(dashboardService.obtenerResumen())
                    .thenThrow(new RuntimeException("Error al calcular métricas"));

            mockMvc.perform(get("/api/dashboard/resumen")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(dashboardService).obtenerResumen();
        }
    }
}
