package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.request.AutorRequest;
import com.biblioteca.backend.dto.response.AutorResponse;
import com.biblioteca.backend.dto.response.ImagenResponse;
import com.biblioteca.backend.entity.Autor;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.mapper.AutorMapper;
import com.biblioteca.backend.service.AutorService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AutorController.class)
@Import(AutorControllerTest.TestConfig.class)
class AutorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private AutorService autorService;

    @MockitoBean
    private AutorMapper autorMapper;

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
    class ObtenerTodos {

        @Test
        void obtenerTodos_DebeRetornarPaginaAutores() throws Exception {
            Autor autor = Autor.builder().id(1L).nombre("Gabriel García Márquez").biografia("Colombiano").estado(true).build();
            Page<Autor> pagina = new PageImpl<>(List.of(autor));
            
            AutorResponse responseDto = new AutorResponse(1L, "Gabriel García Márquez", "Colombiano", LocalDate.of(1927, 3, 6), null, true);

            when(autorService.obtenerTodos(eq("Gabriel"), eq(true), any(Pageable.class))).thenReturn(pagina);
            when(autorMapper.toResponse(any(Autor.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/autores")
                    .param("buscar", "Gabriel")
                    .param("estado", "true")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].nombre").value("Gabriel García Márquez"))
                    .andExpect(jsonPath("$.content[0].biografia").value("Colombiano"))
                    .andExpect(jsonPath("$.content[0].estado").value(true));

            verify(autorService).obtenerTodos(eq("Gabriel"), eq(true), any(Pageable.class));
        }
    }

    @Nested
    class CrearAutor {

        @Test
        void crearAutor_ConDatosValidos_DebeCrearYRetornarAutor() throws Exception {
            UUID fotoId = UUID.randomUUID();
            AutorRequest request = new AutorRequest("Julio Cortázar", "Escritor argentino", LocalDate.of(1914, 8, 26), fotoId, true);
            Autor autorEntity = Autor.builder().nombre("Julio Cortázar").biografia("Escritor argentino").fechaNacimiento(LocalDate.of(1914, 8, 26)).build();
            Autor autorGuardado = Autor.builder().id(2L).nombre("Julio Cortázar").biografia("Escritor argentino").fechaNacimiento(LocalDate.of(1914, 8, 26)).build();
            
            ImagenResponse fotoResponse = new ImagenResponse(fotoId, "foto.jpg", "/uploads/foto.jpg", Instant.now());
            AutorResponse responseDto = new AutorResponse(2L, "Julio Cortázar", "Escritor argentino", LocalDate.of(1914, 8, 26), fotoResponse, true);

            when(autorMapper.toEntity(any(AutorRequest.class))).thenReturn(autorEntity);
            when(autorService.crearAutor(any(Autor.class), eq(fotoId))).thenReturn(autorGuardado);
            when(autorMapper.toResponse(any(Autor.class))).thenReturn(responseDto);

            mockMvc.perform(post("/api/autores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2L))
                    .andExpect(jsonPath("$.nombre").value("Julio Cortázar"))
                    .andExpect(jsonPath("$.foto.id").value(fotoId.toString()))
                    .andExpect(jsonPath("$.estado").value(true));

            verify(autorService).crearAutor(any(Autor.class), eq(fotoId));
        }

        @Test
        void crearAutor_ConNombreVacio_DebeRetornarBadRequest() throws Exception {
            AutorRequest request = new AutorRequest("", "Sin nombre", LocalDate.of(1990, 1, 1), null, true);

            mockMvc.perform(post("/api/autores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.nombre").exists());

            verifyNoInteractions(autorService);
        }

        @Test
        void crearAutor_ConNombreExcedeLimite_DebeRetornarBadRequest() throws Exception {
            String nombreLargo = "a".repeat(151);
            AutorRequest request = new AutorRequest(nombreLargo, "Biografía", LocalDate.of(1990, 1, 1), null, true);

            mockMvc.perform(post("/api/autores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.nombre").exists());

            verifyNoInteractions(autorService);
        }
    }

    @Nested
    class ObtenerPorId {

        @Test
        void obtenerPorId_CuandoExiste_DebeRetornarAutor() throws Exception {
            Autor autor = Autor.builder().id(3L).nombre("Jorge Luis Borges").estado(true).build();
            AutorResponse responseDto = new AutorResponse(3L, "Jorge Luis Borges", null, null, null, true);

            when(autorService.obtenerPorId(3L)).thenReturn(autor);
            when(autorMapper.toResponse(any(Autor.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/autores/3")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3L))
                    .andExpect(jsonPath("$.nombre").value("Jorge Luis Borges"));

            verify(autorService).obtenerPorId(3L);
        }

        @Test
        void obtenerPorId_CuandoNoExiste_DebeRetornarNotFound() throws Exception {
            when(autorService.obtenerPorId(99L)).thenThrow(new EntityNotFoundException("Autor no encontrado"));

            mockMvc.perform(get("/api/autores/99")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));

            verify(autorService).obtenerPorId(99L);
        }
    }

    @Nested
    class Actualizar {

        @Test
        void actualizar_ConDatosValidos_DebeActualizarYRetornarAutor() throws Exception {
            UUID fotoId = UUID.randomUUID();
            AutorRequest request = new AutorRequest("Isabel Allende", "Escritora chilena", LocalDate.of(1942, 8, 2), fotoId, true);
            Autor autorEntity = Autor.builder().nombre("Isabel Allende").biografia("Escritora chilena").fechaNacimiento(LocalDate.of(1942, 8, 2)).build();
            Autor autorActualizado = Autor.builder().id(1L).nombre("Isabel Allende").biografia("Escritora chilena").fechaNacimiento(LocalDate.of(1942, 8, 2)).build();
            AutorResponse responseDto = new AutorResponse(1L, "Isabel Allende", "Escritora chilena", LocalDate.of(1942, 8, 2), null, true);

            when(autorMapper.toEntity(any(AutorRequest.class))).thenReturn(autorEntity);
            when(autorService.actualizar(eq(1L), any(Autor.class), eq(fotoId), eq(true))).thenReturn(autorActualizado);
            when(autorMapper.toResponse(any(Autor.class))).thenReturn(responseDto);

            mockMvc.perform(put("/api/autores/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Isabel Allende"));

            verify(autorService).actualizar(eq(1L), any(Autor.class), eq(fotoId), eq(true));
        }

        @Test
        void actualizar_ConNombreVacio_DebeRetornarBadRequest() throws Exception {
            AutorRequest request = new AutorRequest("", "Biografía", LocalDate.of(1990, 1, 1), null, true);

            mockMvc.perform(put("/api/autores/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.nombre").exists());

            verifyNoInteractions(autorService);
        }
    }

    @Nested
    class Eliminar {

        @Test
        void eliminar_DebeRetornarNoContent() throws Exception {
            doNothing().when(autorService).eliminar(1L);

            mockMvc.perform(delete("/api/autores/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(autorService).eliminar(1L);
        }

        @Test
        void eliminar_CuandoTieneLibros_DebeRetornarBadRequest() throws Exception {
            doThrow(BusinessException.badRequest("AUTHOR_HAS_ACTIVE_BOOKS", "No se puede eliminar el autor porque tiene libros activos"))
                    .when(autorService).eliminar(1L);

            mockMvc.perform(delete("/api/autores/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("AUTHOR_HAS_ACTIVE_BOOKS"));

            verify(autorService).eliminar(1L);
        }
    }

    @Nested
    class Activar {

        @Test
        void activar_DebeRetornarAutorActivado() throws Exception {
            Autor autor = Autor.builder().id(1L).nombre("Gabriel García Márquez").estado(true).build();
            AutorResponse responseDto = new AutorResponse(1L, "Gabriel García Márquez", null, null, null, true);

            when(autorService.activar(1L)).thenReturn(autor);
            when(autorMapper.toResponse(any(Autor.class))).thenReturn(responseDto);

            mockMvc.perform(put("/api/autores/1/activar")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.estado").value(true));

            verify(autorService).activar(1L);
        }
    }
}
