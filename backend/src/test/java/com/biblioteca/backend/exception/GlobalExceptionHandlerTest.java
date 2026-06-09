package com.biblioteca.backend.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    /**
     * Subclase sin basePackages para que el advice aplique al TestController
     * que no está en com.biblioteca.backend.controller.rest.
     * Hereda todos los @ExceptionHandler del padre.
     */
    @RestControllerAdvice
    static class TestAdvice extends GlobalExceptionHandler {}

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    record ValidRequest(@NotBlank String nombre) {}

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(new TestAdvice())
            .build();
    }

    @RestController
    static class TestController {

        @GetMapping("/test/business-bad-request")
        public void throwBusinessBadRequest() {
            throw BusinessException.badRequest("AUTOR_NOT_FOUND", "El autor no existe");
        }

        @GetMapping("/test/business-conflict")
        public void throwBusinessConflict() {
            throw BusinessException.conflict("AUTOR_EXISTS", "El autor ya existe");
        }

        @GetMapping("/test/not-found")
        public void throwEntityNotFound() {
            throw new EntityNotFoundException("Entidad no encontrada");
        }

        @GetMapping("/test/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Argumento inválido");
        }

        @GetMapping("/test/illegal-state")
        public void throwIllegalState() {
            throw new IllegalStateException("Estado de negocio inválido");
        }

        @PostMapping("/test/validation")
        public void throwValidation(@Valid @RequestBody ValidRequest request) {}

        @GetMapping("/test/generic")
        public void throwGeneric() {
            throw new RuntimeException("Error inesperado");
        }
    }

    @Nested
    class BusinessExceptionTests {

        @Test
        void badRequest_DebeRetornar400ConErrorCode() throws Exception {
            mockMvc.perform(get("/test/business-bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("AUTOR_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("El autor no existe"))
                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        void conflict_DebeRetornar409ConErrorCode() throws Exception {
            mockMvc.perform(get("/test/business-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.errorCode").value("AUTOR_EXISTS"))
                .andExpect(jsonPath("$.message").value("El autor ya existe"))
                .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    class EntityNotFoundExceptionTests {

        @Test
        void debeRetornar404ConResourceNotFound() throws Exception {
            mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Entidad no encontrada"))
                .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    class IllegalArgumentExceptionTests {

        @Test
        void debeRetornar400ConInvalidArgument() throws Exception {
            mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Argumento inválido"))
                .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    class IllegalStateExceptionTests {

        @Test
        void debeRetornar400ConBusinessRuleViolation() throws Exception {
            mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Estado de negocio inválido"))
                .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    class ValidationExceptionTests {

        @Test
        void campoEnBlanco_DebeRetornar400ConValidationError() throws Exception {
            String body = objectMapper.writeValueAsString(new ValidRequest(""));

            mockMvc.perform(post("/test/validation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Error de validación en los campos enviados."))
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        void campoNulo_DebeRetornar400ConErrorPorCampo() throws Exception {
            mockMvc.perform(post("/test/validation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nombre\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.nombre").exists());
        }
    }

    @Nested
    class GenericExceptionTests {

        @Test
        void debeRetornar500ConMensajeFijo() throws Exception {
            mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Ha ocurrido un error inesperado en el servidor."))
                .andExpect(jsonPath("$.timestamp").exists());
        }
    }
}
