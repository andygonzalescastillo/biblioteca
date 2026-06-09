package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.request.PrestamoRequest;
import com.biblioteca.backend.dto.response.DetallePrestamoResponse;
import com.biblioteca.backend.dto.response.PrestamoResponse;
import com.biblioteca.backend.dto.response.UsuarioResponse;
import com.biblioteca.backend.entity.EstadoPrestamo;
import com.biblioteca.backend.entity.Prestamo;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.mapper.PrestamoMapper;
import com.biblioteca.backend.service.PrestamoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrestamoController.class)
@Import(PrestamoControllerTest.TestConfig.class)
class PrestamoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private PrestamoService prestamoService;

    @MockitoBean
    private PrestamoMapper prestamoMapper;

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

    private PrestamoResponse buildPrestamoResponse(Long id, EstadoPrestamo estado) {
        UsuarioResponse usuario = new UsuarioResponse(1L, "Andy Gonzales", "andy@gmail.com",
                "999000111", "Calle 123", Instant.now(), null, true);
        DetallePrestamoResponse detalle = new DetallePrestamoResponse(1L, null, 2);
        return new PrestamoResponse(
                id,
                usuario,
                Instant.now(),
                Instant.now().plusSeconds(86400 * 7),
                estado == EstadoPrestamo.DEVUELTO ? Instant.now() : null,
                estado,
                List.of(detalle)
        );
    }

    private Prestamo buildPrestamo(Long id) {
        Prestamo p = new Prestamo();
        p.setId(id);
        return p;
    }

    @Nested
    class ObtenerTodos {

        @Test
        void obtenerTodos_SinFiltros_DebeRetornarPaginaPrestamos() throws Exception {
            Prestamo prestamo = buildPrestamo(1L);
            Page<Prestamo> pagina = new PageImpl<>(List.of(prestamo));
            PrestamoResponse responseDto = buildPrestamoResponse(1L, EstadoPrestamo.ACTIVO);

            when(prestamoService.obtenerTodos(isNull(), isNull(), any(Pageable.class))).thenReturn(pagina);
            when(prestamoMapper.toResponse(any(Prestamo.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/prestamos")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].estado").value("ACTIVO"));

            verify(prestamoService).obtenerTodos(isNull(), isNull(), any(Pageable.class));
        }

        @Test
        void obtenerTodos_ConFiltroUsuarioYEstado_DebeRetornarResultadosFiltrados() throws Exception {
            Prestamo prestamo = buildPrestamo(2L);
            Page<Prestamo> pagina = new PageImpl<>(List.of(prestamo));
            PrestamoResponse responseDto = buildPrestamoResponse(2L, EstadoPrestamo.ACTIVO);

            when(prestamoService.obtenerTodos(eq(1L), eq(EstadoPrestamo.ACTIVO), any(Pageable.class))).thenReturn(pagina);
            when(prestamoMapper.toResponse(any(Prestamo.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/prestamos")
                    .param("usuarioId", "1")
                    .param("estado", "ACTIVO")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(2L))
                    .andExpect(jsonPath("$.content[0].estado").value("ACTIVO"));

            verify(prestamoService).obtenerTodos(eq(1L), eq(EstadoPrestamo.ACTIVO), any(Pageable.class));
        }
    }

    @Nested
    class RegistrarPrestamo {

        @Test
        void registrarPrestamo_ConDatosValidos_DebeCrearYRetornarPrestamo() throws Exception {
            PrestamoRequest.DetalleRequest detalle = new PrestamoRequest.DetalleRequest(10L, 2);
            PrestamoRequest request = new PrestamoRequest(1L, List.of(detalle), 7);

            Prestamo prestamoGuardado = buildPrestamo(5L);
            PrestamoResponse responseDto = buildPrestamoResponse(5L, EstadoPrestamo.ACTIVO);

            when(prestamoService.registrarPrestamo(any(PrestamoService.PrestamoRequest.class))).thenReturn(prestamoGuardado);
            when(prestamoMapper.toResponse(any(Prestamo.class))).thenReturn(responseDto);

            mockMvc.perform(post("/api/prestamos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(5L))
                    .andExpect(jsonPath("$.estado").value("ACTIVO"))
                    .andExpect(jsonPath("$.usuario.nombre").value("Andy Gonzales"));

            verify(prestamoService).registrarPrestamo(any(PrestamoService.PrestamoRequest.class));
        }

        @Test
        void registrarPrestamo_SinUsuarioId_DebeRetornarBadRequest() throws Exception {
            PrestamoRequest.DetalleRequest detalle = new PrestamoRequest.DetalleRequest(10L, 1);
            PrestamoRequest request = new PrestamoRequest(null, List.of(detalle), 7);

            mockMvc.perform(post("/api/prestamos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.usuarioId").exists());

            verifyNoInteractions(prestamoService);
        }

        @Test
        void registrarPrestamo_SinDetalles_DebeRetornarBadRequest() throws Exception {
            PrestamoRequest request = new PrestamoRequest(1L, List.of(), 7);

            mockMvc.perform(post("/api/prestamos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.detalles").exists());

            verifyNoInteractions(prestamoService);
        }

        @Test
        void registrarPrestamo_ConDiasLimiteNegativo_DebeRetornarBadRequest() throws Exception {
            PrestamoRequest.DetalleRequest detalle = new PrestamoRequest.DetalleRequest(10L, 1);
            PrestamoRequest request = new PrestamoRequest(1L, List.of(detalle), 0);

            mockMvc.perform(post("/api/prestamos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.diasLimite").exists());

            verifyNoInteractions(prestamoService);
        }

        @Test
        void registrarPrestamo_ConDetalleCantidadInvalida_DebeRetornarBadRequest() throws Exception {
            PrestamoRequest.DetalleRequest detalle = new PrestamoRequest.DetalleRequest(10L, 0);
            PrestamoRequest request = new PrestamoRequest(1L, List.of(detalle), 7);

            mockMvc.perform(post("/api/prestamos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

            verifyNoInteractions(prestamoService);
        }

        @Test
        void registrarPrestamo_CuandoStockInsuficiente_DebeRetornarBadRequest() throws Exception {
            PrestamoRequest.DetalleRequest detalle = new PrestamoRequest.DetalleRequest(10L, 100);
            PrestamoRequest request = new PrestamoRequest(1L, List.of(detalle), 7);

            when(prestamoService.registrarPrestamo(any(PrestamoService.PrestamoRequest.class)))
                    .thenThrow(BusinessException.badRequest("INSUFFICIENT_STOCK", "Stock insuficiente para el libro solicitado"));

            mockMvc.perform(post("/api/prestamos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"));

            verify(prestamoService).registrarPrestamo(any(PrestamoService.PrestamoRequest.class));
        }
    }

    @Nested
    class ObtenerPorId {

        @Test
        void obtenerPorId_CuandoExiste_DebeRetornarPrestamo() throws Exception {
            Prestamo prestamo = buildPrestamo(3L);
            PrestamoResponse responseDto = buildPrestamoResponse(3L, EstadoPrestamo.ACTIVO);

            when(prestamoService.obtenerPorId(3L)).thenReturn(prestamo);
            when(prestamoMapper.toResponse(any(Prestamo.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/prestamos/3")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3L))
                    .andExpect(jsonPath("$.estado").value("ACTIVO"));

            verify(prestamoService).obtenerPorId(3L);
        }

        @Test
        void obtenerPorId_CuandoNoExiste_DebeRetornarNotFound() throws Exception {
            when(prestamoService.obtenerPorId(99L)).thenThrow(new EntityNotFoundException("Préstamo no encontrado"));

            mockMvc.perform(get("/api/prestamos/99")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));

            verify(prestamoService).obtenerPorId(99L);
        }
    }

    @Nested
    class Devolucion {

        @Test
        void devolverPrestamo_CuandoEstaActivo_DebeRetornarPrestamoDevuelto() throws Exception {
            Prestamo prestamo = buildPrestamo(4L);
            PrestamoResponse responseDto = buildPrestamoResponse(4L, EstadoPrestamo.DEVUELTO);

            when(prestamoService.devolverPrestamo(4L)).thenReturn(prestamo);
            when(prestamoMapper.toResponse(any(Prestamo.class))).thenReturn(responseDto);

            mockMvc.perform(put("/api/prestamos/4/devolucion")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(4L))
                    .andExpect(jsonPath("$.estado").value("DEVUELTO"))
                    .andExpect(jsonPath("$.fechaDevolucionReal").isNotEmpty());

            verify(prestamoService).devolverPrestamo(4L);
        }

        @Test
        void devolverPrestamo_CuandoYaFueDevuelto_DebeRetornarBadRequest() throws Exception {
            when(prestamoService.devolverPrestamo(5L))
                    .thenThrow(BusinessException.badRequest("ALREADY_RETURNED", "El préstamo ya fue devuelto anteriormente"));

            mockMvc.perform(put("/api/prestamos/5/devolucion")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("ALREADY_RETURNED"));

            verify(prestamoService).devolverPrestamo(5L);
        }

        @Test
        void devolverPrestamo_CuandoNoExiste_DebeRetornarNotFound() throws Exception {
            when(prestamoService.devolverPrestamo(99L))
                    .thenThrow(new EntityNotFoundException("Préstamo no encontrado"));

            mockMvc.perform(put("/api/prestamos/99/devolucion")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));

            verify(prestamoService).devolverPrestamo(99L);
        }
    }
}
