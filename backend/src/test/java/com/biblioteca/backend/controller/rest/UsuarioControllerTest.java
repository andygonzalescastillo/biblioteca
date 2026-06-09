package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.request.UsuarioRequest;
import com.biblioteca.backend.dto.response.UsuarioCupoPrestamoResponse;
import com.biblioteca.backend.dto.response.UsuarioResponse;
import com.biblioteca.backend.entity.Usuario;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.mapper.UsuarioMapper;
import com.biblioteca.backend.service.UsuarioService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(UsuarioControllerTest.TestConfig.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioMapper usuarioMapper;

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

    @Nested
    class ObtenerTodos {

        @Test
        void obtenerTodos_DebeRetornarPaginaUsuarios() throws Exception {
            Usuario usuario = Usuario.builder().id(1L).nombre("Andy Gonzales").email("andy@gmail.com").estado(true).build();
            Page<Usuario> pagina = new PageImpl<>(List.of(usuario));
            UsuarioResponse responseDto = new UsuarioResponse(1L, "Andy Gonzales", "andy@gmail.com", "123456", "Calle 123", Instant.now(), null, true);

            when(usuarioService.obtenerTodos(eq("Andy"), eq(true), any(Pageable.class))).thenReturn(pagina);
            when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/usuarios")
                    .param("buscar", "Andy")
                    .param("estado", "true")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].nombre").value("Andy Gonzales"))
                    .andExpect(jsonPath("$.content[0].email").value("andy@gmail.com"))
                    .andExpect(jsonPath("$.content[0].estado").value(true));

            verify(usuarioService).obtenerTodos(eq("Andy"), eq(true), any(Pageable.class));
        }
    }

    @Nested
    class CrearUsuario {

        @Test
        void crearUsuario_ConDatosValidos_DebeCrearYRetornarUsuario() throws Exception {
            UUID fotoId = UUID.randomUUID();
            UsuarioRequest request = new UsuarioRequest("Juan Perez", "juan@gmail.com", "987654321", "Av. Peru 123", fotoId, true);
            
            Usuario usuarioEntity = Usuario.builder().nombre("Juan Perez").email("juan@gmail.com").telefono("987654321").direccion("Av. Peru 123").build();
            Usuario usuarioGuardado = Usuario.builder().id(2L).nombre("Juan Perez").email("juan@gmail.com").telefono("987654321").direccion("Av. Peru 123").build();
            UsuarioResponse responseDto = new UsuarioResponse(2L, "Juan Perez", "juan@gmail.com", "987654321", "Av. Peru 123", Instant.now(), null, true);

            when(usuarioMapper.toEntity(any(UsuarioRequest.class))).thenReturn(usuarioEntity);
            when(usuarioService.crearUsuario(any(Usuario.class), eq(fotoId))).thenReturn(usuarioGuardado);
            when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(responseDto);

            mockMvc.perform(post("/api/usuarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2L))
                    .andExpect(jsonPath("$.nombre").value("Juan Perez"))
                    .andExpect(jsonPath("$.email").value("juan@gmail.com"));

            verify(usuarioService).crearUsuario(any(Usuario.class), eq(fotoId));
        }

        @Test
        void crearUsuario_ConEmailInvalido_DebeRetornarBadRequest() throws Exception {
            UsuarioRequest request = new UsuarioRequest("Juan Perez", "juan-email-invalido", "987654321", "Av. Peru 123", null, true);

            mockMvc.perform(post("/api/usuarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.email").exists());

            verifyNoInteractions(usuarioService);
        }

        @Test
        void crearUsuario_ConNombreDemasiadoLargo_DebeRetornarBadRequest() throws Exception {
            String nombreLargo = "A".repeat(151);
            UsuarioRequest request = new UsuarioRequest(nombreLargo, "juan@gmail.com", "987654321", "Av. Peru 123", null, true);

            mockMvc.perform(post("/api/usuarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.nombre").exists());

            verifyNoInteractions(usuarioService);
        }

        @Test
        void crearUsuario_ConTelefonoDemasiadoLargo_DebeRetornarBadRequest() throws Exception {
            String telfLargo = "1".repeat(21);
            UsuarioRequest request = new UsuarioRequest("Juan Perez", "juan@gmail.com", telfLargo, "Av. Peru 123", null, true);

            mockMvc.perform(post("/api/usuarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.telefono").exists());

            verifyNoInteractions(usuarioService);
        }

        @Test
        void crearUsuario_ConDireccionDemasiadaLarga_DebeRetornarBadRequest() throws Exception {
            String dirLarga = "D".repeat(256);
            UsuarioRequest request = new UsuarioRequest("Juan Perez", "juan@gmail.com", "987654321", dirLarga, null, true);

            mockMvc.perform(post("/api/usuarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.direccion").exists());

            verifyNoInteractions(usuarioService);
        }
    }

    @Nested
    class ObtenerPorId {

        @Test
        void obtenerPorId_CuandoExiste_DebeRetornarUsuario() throws Exception {
            Usuario usuario = Usuario.builder().id(3L).nombre("Carlos Gomez").email("carlos@gmail.com").estado(true).build();
            UsuarioResponse responseDto = new UsuarioResponse(3L, "Carlos Gomez", "carlos@gmail.com", null, null, Instant.now(), null, true);

            when(usuarioService.obtenerPorId(3L)).thenReturn(usuario);
            when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/usuarios/3")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3L))
                    .andExpect(jsonPath("$.nombre").value("Carlos Gomez"));

            verify(usuarioService).obtenerPorId(3L);
        }

        @Test
        void obtenerPorId_CuandoNoExiste_DebeRetornarNotFound() throws Exception {
            when(usuarioService.obtenerPorId(99L)).thenThrow(new EntityNotFoundException("Usuario no encontrado"));

            mockMvc.perform(get("/api/usuarios/99")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));

            verify(usuarioService).obtenerPorId(99L);
        }
    }

    @Nested
    class ObtenerPorEmail {

        @Test
        void obtenerPorEmail_CuandoExiste_DebeRetornarUsuario() throws Exception {
            Usuario usuario = Usuario.builder().id(3L).nombre("Carlos Gomez").email("carlos@gmail.com").estado(true).build();
            UsuarioResponse responseDto = new UsuarioResponse(3L, "Carlos Gomez", "carlos@gmail.com", null, null, Instant.now(), null, true);

            when(usuarioService.obtenerPorEmail("carlos@gmail.com")).thenReturn(usuario);
            when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(responseDto);

            mockMvc.perform(get("/api/usuarios/email/carlos@gmail.com")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3L))
                    .andExpect(jsonPath("$.email").value("carlos@gmail.com"));

            verify(usuarioService).obtenerPorEmail("carlos@gmail.com");
        }
    }

    @Nested
    class Actualizar {

        @Test
        void actualizar_ConDatosValidos_DebeActualizarYRetornarUsuario() throws Exception {
            UUID fotoId = UUID.randomUUID();
            UsuarioRequest request = new UsuarioRequest("Juan Modificado", "juan@gmail.com", "987654321", "Av. Peru 123", fotoId, true);
            Usuario usuarioEntity = Usuario.builder().nombre("Juan Modificado").email("juan@gmail.com").telefono("987654321").direccion("Av. Peru 123").build();
            Usuario usuarioActualizado = Usuario.builder().id(1L).nombre("Juan Modificado").email("juan@gmail.com").telefono("987654321").direccion("Av. Peru 123").build();
            UsuarioResponse responseDto = new UsuarioResponse(1L, "Juan Modificado", "juan@gmail.com", "987654321", "Av. Peru 123", Instant.now(), null, true);

            when(usuarioMapper.toEntity(any(UsuarioRequest.class))).thenReturn(usuarioEntity);
            when(usuarioService.actualizar(eq(1L), any(Usuario.class), eq(fotoId), eq(true))).thenReturn(usuarioActualizado);
            when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(responseDto);

            mockMvc.perform(put("/api/usuarios/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Juan Modificado"));

            verify(usuarioService).actualizar(eq(1L), any(Usuario.class), eq(fotoId), eq(true));
        }

        @Test
        void actualizar_ConNombreVacio_DebeRetornarBadRequest() throws Exception {
            UsuarioRequest request = new UsuarioRequest("", "juan@gmail.com", "987654321", "Av. Peru 123", null, true);

            mockMvc.perform(put("/api/usuarios/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.nombre").exists());

            verifyNoInteractions(usuarioService);
        }
    }

    @Nested
    class Eliminar {

        @Test
        void eliminar_DebeRetornarNoContent() throws Exception {
            doNothing().when(usuarioService).eliminar(1L);

            mockMvc.perform(delete("/api/usuarios/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(usuarioService).eliminar(1L);
        }

        @Test
        void eliminar_CuandoTienePrestamos_DebeRetornarBadRequest() throws Exception {
            doThrow(BusinessException.badRequest("USER_HAS_ACTIVE_LOANS", "No se puede eliminar el usuario porque tiene préstamos activos"))
                    .when(usuarioService).eliminar(1L);

            mockMvc.perform(delete("/api/usuarios/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("USER_HAS_ACTIVE_LOANS"));

            verify(usuarioService).eliminar(1L);
        }
    }

    @Nested
    class Activar {

        @Test
        void activar_DebeRetornarUsuarioActivado() throws Exception {
            Usuario usuario = Usuario.builder().id(1L).nombre("Andy Gonzales").estado(true).build();
            UsuarioResponse responseDto = new UsuarioResponse(1L, "Andy Gonzales", "andy@gmail.com", null, null, Instant.now(), null, true);

            when(usuarioService.activar(1L)).thenReturn(usuario);
            when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(responseDto);

            mockMvc.perform(put("/api/usuarios/1/activar")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.estado").value(true));

            verify(usuarioService).activar(1L);
        }
    }

    @Nested
    class ObtenerCupoPrestamo {

        @Test
        void obtenerCupoPrestamo_DebeRetornarCupoDeUsuario() throws Exception {
            UsuarioCupoPrestamoResponse cupoResponse = new UsuarioCupoPrestamoResponse(1L, 5, 2, 3, List.of(10L, 11L));
            
            when(usuarioService.obtenerCupoPrestamo(1L)).thenReturn(cupoResponse);

            mockMvc.perform(get("/api/usuarios/1/cupo-prestamo")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.usuarioId").value(1L))
                    .andExpect(jsonPath("$.maximoPermitido").value(5))
                    .andExpect(jsonPath("$.librosEnPosesion").value(2))
                    .andExpect(jsonPath("$.cupoDisponible").value(3))
                    .andExpect(jsonPath("$.librosPrestadosIds[0]").value(10L));

            verify(usuarioService).obtenerCupoPrestamo(1L);
        }
    }
}
