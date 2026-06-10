package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.request.LibroRequest;
import com.biblioteca.backend.dto.response.CategoriaResponse;
import com.biblioteca.backend.dto.response.LibroResponse;
import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.entity.Libro;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.mapper.LibroMapper;
import com.biblioteca.backend.service.LibroService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LibroController.class)
@Import(LibroControllerTest.TestConfig.class)
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LibroService libroService;

    @MockitoBean
    private LibroMapper libroMapper;

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
        void obtenerTodos_DebeRetornarPaginaLibros() throws Exception {
            Categoria categoria = Categoria.builder().id(1L).nombre("Novela").build();
            Libro libro = Libro.builder().id(1L).titulo("Cien años de soledad").isbn("978-0307474728").stock(5).categoria(categoria).estado(true).build();
            Page<Libro> pagina = new PageImpl<>(List.of(libro));
            
            CategoriaResponse catResponse = new CategoriaResponse(1L, "Novela", null, true);
            LibroResponse responseDto = new LibroResponse(1L, "Cien años de soledad", "978-0307474728", 5, catResponse, null, Collections.emptySet(), true);

            when(libroService.obtenerTodos(eq("Cien"), eq(true), eq(1L), eq(2L), any(Pageable.class))).thenReturn(pagina);
            when(libroMapper.toResponse(any(Libro.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/libros")
                    .param("buscar", "Cien")
                    .param("estado", "true")
                    .param("categoriaId", "1")
                    .param("autorId", "2")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].titulo").value("Cien años de soledad"))
                    .andExpect(jsonPath("$.content[0].isbn").value("978-0307474728"))
                    .andExpect(jsonPath("$.content[0].categoria.id").value(1L))
                    .andExpect(jsonPath("$.content[0].estado").value(true));

            verify(libroService).obtenerTodos(eq("Cien"), eq(true), eq(1L), eq(2L), any(Pageable.class));
        }
    }

    @Nested
    class CrearLibro {

        @Test
        void crearLibro_ConDatosValidos_DebeCrearYRetornarLibro() throws Exception {
            UUID portadaId = UUID.randomUUID();
            LibroRequest request = new LibroRequest("Rayuela", "978-8420471839", 3, 1L, Set.of(2L), portadaId, true);
            
            Libro libroEntity = Libro.builder().titulo("Rayuela").isbn("978-8420471839").stock(3).build();
            Libro libroGuardado = Libro.builder().id(2L).titulo("Rayuela").isbn("978-8420471839").stock(3).build();
            
            CategoriaResponse catResponse = new CategoriaResponse(1L, "Novela", null, true);
            LibroResponse responseDto = new LibroResponse(2L, "Rayuela", "978-8420471839", 3, catResponse, null, Collections.emptySet(), true);

            when(libroMapper.toEntity(any(LibroRequest.class))).thenReturn(libroEntity);
            when(libroService.crearLibro(any(Libro.class), eq(1L), eq(portadaId), eq(Set.of(2L)))).thenReturn(libroGuardado);
            when(libroMapper.toResponse(any(Libro.class))).thenReturn(responseDto);

            mockMvc.perform(post("/api/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2L))
                    .andExpect(jsonPath("$.titulo").value("Rayuela"))
                    .andExpect(jsonPath("$.isbn").value("978-8420471839"));

            verify(libroService).crearLibro(any(Libro.class), eq(1L), eq(portadaId), eq(Set.of(2L)));
        }

        @Test
        void crearLibro_ConTituloVacio_DebeRetornarBadRequest() throws Exception {
            LibroRequest request = new LibroRequest("", "978-8420471839", 3, 1L, Set.of(2L), null, true);

            mockMvc.perform(post("/api/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.titulo").exists());

            verifyNoInteractions(libroService);
        }

        @Test
        void crearLibro_ConTituloDemasiadoLargo_DebeRetornarBadRequest() throws Exception {
            String tituloLargo = "A".repeat(256);
            LibroRequest request = new LibroRequest(tituloLargo, "978-8420471839", 3, 1L, Set.of(2L), null, true);

            mockMvc.perform(post("/api/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.titulo").exists());

            verifyNoInteractions(libroService);
        }

        @Test
        void crearLibro_ConIsbnDemasiadoLargo_DebeRetornarBadRequest() throws Exception {
            String isbnLargo = "1".repeat(21);
            LibroRequest request = new LibroRequest("Rayuela", isbnLargo, 3, 1L, Set.of(2L), null, true);

            mockMvc.perform(post("/api/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.isbn").exists());

            verifyNoInteractions(libroService);
        }

        @Test
        void crearLibro_ConStockNegativo_DebeRetornarBadRequest() throws Exception {
            LibroRequest request = new LibroRequest("Rayuela", "978-8420471839", -1, 1L, Set.of(2L), null, true);

            mockMvc.perform(post("/api/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.stock").exists());

            verifyNoInteractions(libroService);
        }

        @Test
        void crearLibro_SinCategoria_DebeRetornarBadRequest() throws Exception {
            LibroRequest request = new LibroRequest("Rayuela", "978-8420471839", 3, null, Set.of(2L), null, true);

            mockMvc.perform(post("/api/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.categoriaId").exists());

            verifyNoInteractions(libroService);
        }

        @Test
        void crearLibro_SinAutores_DebeRetornarBadRequest() throws Exception {
            LibroRequest request = new LibroRequest("Rayuela", "978-8420471839", 3, 1L, Collections.emptySet(), null, true);

            mockMvc.perform(post("/api/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.autoresIds").exists());

            verifyNoInteractions(libroService);
        }
    }

    @Nested
    class ObtenerPorId {

        @Test
        void obtenerPorId_CuandoExiste_DebeRetornarLibro() throws Exception {
            Categoria categoria = Categoria.builder().id(1L).nombre("Novela").build();
            Libro libro = Libro.builder().id(3L).titulo("El túnel").isbn("978-8432248139").stock(4).categoria(categoria).estado(true).build();
            CategoriaResponse catResponse = new CategoriaResponse(1L, "Novela", null, true);
            LibroResponse responseDto = new LibroResponse(3L, "El túnel", "978-8432248139", 4, catResponse, null, Collections.emptySet(), true);

            when(libroService.obtenerPorId(3L)).thenReturn(libro);
            when(libroMapper.toResponse(any(Libro.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/libros/3")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3L))
                    .andExpect(jsonPath("$.titulo").value("El túnel"));

            verify(libroService).obtenerPorId(3L);
        }

        @Test
        void obtenerPorId_CuandoNoExiste_DebeRetornarNotFound() throws Exception {
            when(libroService.obtenerPorId(99L)).thenThrow(new EntityNotFoundException("Libro no encontrado"));

            mockMvc.perform(get("/api/libros/99")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));

            verify(libroService).obtenerPorId(99L);
        }
    }

    @Nested
    class ObtenerPorIsbn {

        @Test
        void obtenerPorIsbn_CuandoExiste_DebeRetornarLibro() throws Exception {
            Categoria categoria = Categoria.builder().id(1L).nombre("Novela").build();
            Libro libro = Libro.builder().id(3L).titulo("El túnel").isbn("9788432248139").stock(4).categoria(categoria).estado(true).build();
            CategoriaResponse catResponse = new CategoriaResponse(1L, "Novela", null, true);
            LibroResponse responseDto = new LibroResponse(3L, "El túnel", "9788432248139", 4, catResponse, null, Collections.emptySet(), true);

            when(libroService.obtenerPorIsbn("9788432248139")).thenReturn(libro);
            when(libroMapper.toResponse(any(Libro.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/libros/isbn/9788432248139")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3L))
                    .andExpect(jsonPath("$.isbn").value("9788432248139"));

            verify(libroService).obtenerPorIsbn("9788432248139");
        }
    }

    @Nested
    class Actualizar {

        @Test
        void actualizar_ConDatosValidos_DebeActualizarYRetornarLibro() throws Exception {
            UUID portadaId = UUID.randomUUID();
            LibroRequest request = new LibroRequest("Ficciones", "978-8420471839", 10, 1L, Set.of(2L), portadaId, true);
            
            Libro libroEntity = Libro.builder().titulo("Ficciones").isbn("978-8420471839").stock(10).build();
            Libro libroActualizado = Libro.builder().id(1L).titulo("Ficciones").isbn("978-8420471839").stock(10).build();
            
            CategoriaResponse catResponse = new CategoriaResponse(1L, "Novela", null, true);
            LibroResponse responseDto = new LibroResponse(1L, "Ficciones", "978-8420471839", 10, catResponse, null, Collections.emptySet(), true);

            when(libroMapper.toEntity(any(LibroRequest.class))).thenReturn(libroEntity);
            when(libroService.actualizar(eq(1L), any(Libro.class), eq(1L), eq(portadaId), eq(Set.of(2L)), eq(true))).thenReturn(libroActualizado);
            when(libroMapper.toResponse(any(Libro.class))).thenReturn(responseDto);

            mockMvc.perform(put("/api/libros/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.titulo").value("Ficciones"));

            verify(libroService).actualizar(eq(1L), any(Libro.class), eq(1L), eq(portadaId), eq(Set.of(2L)), eq(true));
        }

        @Test
        void actualizar_ConTituloVacio_DebeRetornarBadRequest() throws Exception {
            LibroRequest request = new LibroRequest("", "978-8420471839", 10, 1L, Set.of(2L), null, true);

            mockMvc.perform(put("/api/libros/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.titulo").exists());

            verifyNoInteractions(libroService);
        }
    }

    @Nested
    class Eliminar {

        @Test
        void eliminar_DebeRetornarNoContent() throws Exception {
            doNothing().when(libroService).eliminar(1L);

            mockMvc.perform(delete("/api/libros/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(libroService).eliminar(1L);
        }

        @Test
        void eliminar_CuandoTienePrestamos_DebeRetornarBadRequest() throws Exception {
            doThrow(BusinessException.badRequest("BOOK_HAS_ACTIVE_LOANS", "No se puede eliminar el libro porque tiene préstamos activos"))
                    .when(libroService).eliminar(1L);

            mockMvc.perform(delete("/api/libros/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("BOOK_HAS_ACTIVE_LOANS"));

            verify(libroService).eliminar(1L);
        }
    }

    @Nested
    class Activar {

        @Test
        void activar_DebeRetornarLibroActivado() throws Exception {
            Categoria categoria = Categoria.builder().id(1L).nombre("Novela").build();
            Libro libro = Libro.builder().id(1L).titulo("Ficciones").isbn("978-8420471839").stock(10).categoria(categoria).estado(true).build();
            CategoriaResponse catResponse = new CategoriaResponse(1L, "Novela", null, true);
            LibroResponse responseDto = new LibroResponse(1L, "Ficciones", "978-8420471839", 10, catResponse, null, Collections.emptySet(), true);

            when(libroService.activar(1L)).thenReturn(libro);
            when(libroMapper.toResponse(any(Libro.class))).thenReturn(responseDto);

            mockMvc.perform(put("/api/libros/1/activar")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.estado").value(true));

            verify(libroService).activar(1L);
        }
    }
}
