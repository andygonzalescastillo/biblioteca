package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.dto.request.CategoriaRequest;
import com.biblioteca.backend.dto.response.CategoriaResponse;
import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.mapper.CategoriaMapper;
import com.biblioteca.backend.service.CategoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import com.biblioteca.backend.config.AppProperties;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
@Import(CategoriaControllerTest.TestConfig.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private CategoriaMapper categoriaMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AppProperties appProperties() {
            return new AppProperties(
                "uploads",
                new AppProperties.Cors(List.of("http://localhost:3000")),
                new AppProperties.Prestamo(5, 1, 30, 3, 5), null);
        }
    }

    @Nested
    class ObtenerTodas {

        @Test
        void obtenerTodas_DebeRetornarPaginaCategorias() throws Exception {
            Categoria categoria = Categoria.builder().id(1L).nombre("Terror").descripcion("Novelas de terror").estado(true).build();
            Page<Categoria> pagina = new PageImpl<>(List.of(categoria));
            
            CategoriaResponse responseDto = new CategoriaResponse(1L, "Terror", "Novelas de terror", true);

            when(categoriaService.obtenerTodas(eq("Terror"), eq(true), any(Pageable.class))).thenReturn(pagina);
            when(categoriaMapper.toResponse(any(Categoria.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/categorias")
                    .param("buscar", "Terror")
                    .param("estado", "true")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].nombre").value("Terror"))
                    .andExpect(jsonPath("$.content[0].descripcion").value("Novelas de terror"))
                    .andExpect(jsonPath("$.content[0].estado").value(true));

            verify(categoriaService).obtenerTodas(eq("Terror"), eq(true), any(Pageable.class));
        }
    }

    @Nested
    class CrearCategoria {

        @Test
        void crearCategoria_ConDatosValidos_DebeCrearYRetornarCategoria() throws Exception {
            CategoriaRequest request = new CategoriaRequest("Historia", "Libros históricos", true);
            Categoria categoriaEntity = Categoria.builder().nombre("Historia").descripcion("Libros históricos").estado(true).build();
            Categoria categoriaGuardada = Categoria.builder().id(2L).nombre("Historia").descripcion("Libros históricos").estado(true).build();
            CategoriaResponse responseDto = new CategoriaResponse(2L, "Historia", "Libros históricos", true);

            when(categoriaMapper.toEntity(any(CategoriaRequest.class))).thenReturn(categoriaEntity);
            when(categoriaService.crearCategoria(any(Categoria.class))).thenReturn(categoriaGuardada);
            when(categoriaMapper.toResponse(any(Categoria.class))).thenReturn(responseDto);

            mockMvc.perform(post("/api/categorias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2L))
                    .andExpect(jsonPath("$.nombre").value("Historia"))
                    .andExpect(jsonPath("$.descripcion").value("Libros históricos"))
                    .andExpect(jsonPath("$.estado").value(true));

            verify(categoriaService).crearCategoria(any(Categoria.class));
        }

        @Test
        void crearCategoria_ConNombreVacio_DebeRetornarBadRequest() throws Exception {
            CategoriaRequest request = new CategoriaRequest("", "Sin nombre", true);

            mockMvc.perform(post("/api/categorias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.nombre").exists());

            verifyNoInteractions(categoriaService);
        }

        @Test
        void crearCategoria_ConNombreExcedeLimite_DebeRetornarBadRequest() throws Exception {
            String nombreLargo = "c".repeat(101);
            CategoriaRequest request = new CategoriaRequest(nombreLargo, "Descripción", true);

            mockMvc.perform(post("/api/categorias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.nombre").exists());

            verifyNoInteractions(categoriaService);
        }

        @Test
        void crearCategoria_ConDescripcionExcedeLimite_DebeRetornarBadRequest() throws Exception {
            String descLarga = "d".repeat(256);
            CategoriaRequest request = new CategoriaRequest("Historia", descLarga, true);

            mockMvc.perform(post("/api/categorias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.descripcion").exists());

            verifyNoInteractions(categoriaService);
        }
    }

    @Nested
    class ObtenerPorId {

        @Test
        void obtenerPorId_CuandoExiste_DebeRetornarCategoria() throws Exception {
            Categoria categoria = Categoria.builder().id(3L).nombre("Ciencia").descripcion("Libros científicos").estado(true).build();
            CategoriaResponse responseDto = new CategoriaResponse(3L, "Ciencia", "Libros científicos", true);

            when(categoriaService.obtenerPorId(3L)).thenReturn(categoria);
            when(categoriaMapper.toResponse(any(Categoria.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/categorias/3")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3L))
                    .andExpect(jsonPath("$.nombre").value("Ciencia"));

            verify(categoriaService).obtenerPorId(3L);
        }

        @Test
        void obtenerPorId_CuandoNoExiste_DebeRetornarNotFound() throws Exception {
            when(categoriaService.obtenerPorId(99L)).thenThrow(new EntityNotFoundException("Categoría no encontrada"));

            mockMvc.perform(get("/api/categorias/99")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));

            verify(categoriaService).obtenerPorId(99L);
        }
    }

    @Nested
    class Actualizar {

        @Test
        void actualizar_ConDatosValidos_DebeActualizarYRetornarCategoria() throws Exception {
            CategoriaRequest request = new CategoriaRequest("Fantasía Modificada", "Fantasía épica y más", true);
            Categoria categoriaEntity = Categoria.builder().nombre("Fantasía Modificada").descripcion("Fantasía épica y más").estado(true).build();
            Categoria categoriaActualizada = Categoria.builder().id(1L).nombre("Fantasía Modificada").descripcion("Fantasía épica y más").estado(true).build();
            CategoriaResponse responseDto = new CategoriaResponse(1L, "Fantasía Modificada", "Fantasía épica y más", true);

            when(categoriaMapper.toEntity(any(CategoriaRequest.class))).thenReturn(categoriaEntity);
            when(categoriaService.actualizar(eq(1L), any(Categoria.class), eq(true))).thenReturn(categoriaActualizada);
            when(categoriaMapper.toResponse(any(Categoria.class))).thenReturn(responseDto);

            mockMvc.perform(put("/api/categorias/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Fantasía Modificada"));

            verify(categoriaService).actualizar(eq(1L), any(Categoria.class), eq(true));
        }

        @Test
        void actualizar_ConNombreVacio_DebeRetornarBadRequest() throws Exception {
            CategoriaRequest request = new CategoriaRequest("", "Descripción", true);

            mockMvc.perform(put("/api/categorias/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.nombre").exists());

            verifyNoInteractions(categoriaService);
        }
    }

    @Nested
    class Eliminar {

        @Test
        void eliminar_DebeRetornarNoContent() throws Exception {
            doNothing().when(categoriaService).eliminar(1L);

            mockMvc.perform(delete("/api/categorias/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(categoriaService).eliminar(1L);
        }

        @Test
        void eliminar_CuandoTieneLibros_DebeRetornarBadRequest() throws Exception {
            doThrow(BusinessException.badRequest("CATEGORY_HAS_ACTIVE_BOOKS", "No se puede eliminar la categoría porque tiene libros activos"))
                    .when(categoriaService).eliminar(1L);

            mockMvc.perform(delete("/api/categorias/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("CATEGORY_HAS_ACTIVE_BOOKS"));

            verify(categoriaService).eliminar(1L);
        }
    }

    @Nested
    class Activar {

        @Test
        void activar_DebeRetornarCategoriaActivada() throws Exception {
            Categoria categoria = Categoria.builder().id(1L).nombre("Terror").descripcion("Novelas de terror").estado(true).build();
            CategoriaResponse responseDto = new CategoriaResponse(1L, "Terror", "Novelas de terror", true);

            when(categoriaService.activar(1L)).thenReturn(categoria);
            when(categoriaMapper.toResponse(any(Categoria.class))).thenReturn(responseDto);

            mockMvc.perform(put("/api/categorias/1/activar")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.estado").value(true));

            verify(categoriaService).activar(1L);
        }
    }
}
