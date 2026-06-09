package com.biblioteca.backend.exception.mvc;

import com.biblioteca.backend.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PortalMvcExceptionHandlerTest {

    /**
     * Subclase sin basePackages para que el advice aplique al TestController
     * que no está en com.biblioteca.backend.controller.mvc.
     */
    @ControllerAdvice
    static class TestAdvice extends PortalMvcExceptionHandler {}

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(new TestAdvice())
            .build();
    }

    @RestController
    static class TestController {

        @GetMapping("/test/mvc/not-found-get")
        public String throwEntityNotFoundGet() {
            throw new EntityNotFoundException("Libro no encontrado");
        }

        @PostMapping("/test/mvc/not-found-post")
        public String throwEntityNotFoundPost() {
            throw new EntityNotFoundException("Libro no encontrado");
        }

        @PostMapping("/test/mvc/business")
        public String throwBusiness() {
            throw BusinessException.badRequest("STOCK_INSUFICIENTE", "Stock insuficiente");
        }

        @PostMapping("/test/mvc/illegal-argument")
        public String throwIllegalArgument() {
            throw new IllegalArgumentException("Parámetro inválido");
        }

        @PostMapping("/test/mvc/illegal-state")
        public String throwIllegalState() {
            throw new IllegalStateException("Estado inválido de negocio");
        }

        @GetMapping("/test/mvc/generic")
        public String throwGeneric() {
            throw new RuntimeException("Error inesperado");
        }
    }


    @Nested
    class EntityNotFoundExceptionTests {

        @Test
        void get_SinReferer_DebeRenderizarVistaErrorCon404() throws Exception {
            mockMvc.perform(get("/test/mvc/not-found-get"))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("status", 404))
                .andExpect(model().attributeExists("path"))
                .andExpect(view().name("error"));
        }

        @Test
        void post_ConRefererDelPortal_DebeRedirigirAlReferer() throws Exception {
            mockMvc.perform(post("/test/mvc/not-found-post")
                    .header("Referer", "http://localhost/portal/reserva/carrito"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/reserva/carrito"))
                .andExpect(flash().attributeExists("error"));
        }

        @Test
        void post_SinReferer_DebeRedirigirAlCatalogo() throws Exception {
            mockMvc.perform(post("/test/mvc/not-found-post"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/catalogo"))
                .andExpect(flash().attributeExists("error"));
        }

        @Test
        void post_ConRefererFueraDelPortal_DebeRedirigirAlCatalogo() throws Exception {
            mockMvc.perform(post("/test/mvc/not-found-post")
                    .header("Referer", "http://localhost/admin/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/catalogo"))
                .andExpect(flash().attributeExists("error"));
        }
    }


    @Nested
    class BadRequestExceptionTests {

        @Test
        void businessException_ConRefererDelPortal_DebeRedirigirConFlash() throws Exception {
            mockMvc.perform(post("/test/mvc/business")
                    .header("Referer", "http://localhost/portal/reserva/carrito"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/reserva/carrito"))
                .andExpect(flash().attributeExists("error"));
        }

        @Test
        void businessException_SinReferer_DebeRedirigirAlCatalogo() throws Exception {
            mockMvc.perform(post("/test/mvc/business"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/catalogo"))
                .andExpect(flash().attributeExists("error"));
        }

        @Test
        void illegalArgumentException_SinReferer_DebeRedirigirAlCatalogo() throws Exception {
            mockMvc.perform(post("/test/mvc/illegal-argument"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/catalogo"))
                .andExpect(flash().attributeExists("error"));
        }

        @Test
        void illegalStateException_ConRefererDelPortal_DebeRedirigirConFlash() throws Exception {
            mockMvc.perform(post("/test/mvc/illegal-state")
                    .header("Referer", "http://localhost/portal/catalogo?q=java"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/catalogo?q=java"))
                .andExpect(flash().attributeExists("error"));
        }
    }


    @Nested
    class GenericExceptionTests {

        @Test
        void debeRenderizarVistaErrorCon500() throws Exception {
            mockMvc.perform(get("/test/mvc/generic"))
                .andExpect(status().isOk())          // el handler devuelve la vista "error" con status 200 por default en standalone
                .andExpect(model().attribute("status", 500))
                .andExpect(model().attributeExists("message"))
                .andExpect(view().name("error"));
        }
    }
}
