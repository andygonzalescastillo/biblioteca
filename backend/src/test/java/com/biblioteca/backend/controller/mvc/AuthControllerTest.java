package com.biblioteca.backend.controller.mvc;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.entity.Usuario;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.exception.GlobalExceptionHandler;
import com.biblioteca.backend.exception.mvc.PortalMvcExceptionHandler;
import com.biblioteca.backend.mvc.portal.PortalConstantes;
import com.biblioteca.backend.service.portal.LectorAuthService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class))
@Import({AuthControllerTest.TestConfig.class, PortalMvcExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LectorAuthService lectorAuthService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AppProperties appProperties() {
            return new AppProperties(
                "uploads",
                new AppProperties.Cors(List.of("http://localhost:3000")),
                new AppProperties.Prestamo(7, 1, 30, 3, 5)
            );
        }
    }

    @Nested
    class Login {

        @Test
        void login_SinSesion_DebeRetornarVistaLogin() throws Exception {
            mockMvc.perform(get("/portal/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("portal/login"))
                    .andExpect(model().attributeExists("diasPrestamoDefault"))
                    .andExpect(model().attributeExists("diasPrestamoMinimo"))
                    .andExpect(model().attributeExists("diasPrestamoMaximo"))
                    .andExpect(model().attributeExists("cantidadReservaMaxima"))
                    .andExpect(model().attributeExists("maxLibrosPrestadosConcurrentes"));
        }

        @Test
        void login_ConSesionActiva_DebeRedirigirAlCatalogo() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 1L);

            mockMvc.perform(get("/portal/login").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/portal/catalogo"));
        }

        @Test
        void login_DebeAgregarConfiguracionPrestamoAlModelo() throws Exception {
            mockMvc.perform(get("/portal/login"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("diasPrestamoDefault", 7))
                    .andExpect(model().attribute("diasPrestamoMinimo", 1))
                    .andExpect(model().attribute("diasPrestamoMaximo", 30))
                    .andExpect(model().attribute("cantidadReservaMaxima", 3))
                    .andExpect(model().attribute("maxLibrosPrestadosConcurrentes", 5));
        }
    }

    @Nested
    class IniciarSesion {

        @Test
        void iniciarSesion_ConEmailValido_DebeGuardarSesionYRedirigirAlCatalogo() throws Exception {
            Usuario usuario = Usuario.builder().id(10L).email("andy@gmail.com").estado(true).build();
            when(lectorAuthService.autenticarPorEmail("andy@gmail.com")).thenReturn(usuario);

            MockHttpSession session = new MockHttpSession();

            mockMvc.perform(post("/portal/login")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("email", "andy@gmail.com")
                    .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/portal/catalogo"));

            assertEquals(10L, session.getAttribute(PortalConstantes.SESSION_USUARIO_ID));

            verify(lectorAuthService).autenticarPorEmail("andy@gmail.com");
        }

        @Test
        void iniciarSesion_ConEmailInvalido_CatchIllegalArgument_DebeVolverALogin() throws Exception {
            when(lectorAuthService.autenticarPorEmail("noexiste@gmail.com"))
                    .thenThrow(new IllegalArgumentException("Email no registrado"));

            mockMvc.perform(post("/portal/login")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("email", "noexiste@gmail.com"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("portal/login"))
                    .andExpect(model().attributeExists("error"))
                    .andExpect(model().attribute("email", "noexiste@gmail.com"));

            verify(lectorAuthService).autenticarPorEmail("noexiste@gmail.com");
        }

        @Test
        void iniciarSesion_ConBusinessException_DebeRedirigirViaExceptionHandler() throws Exception {
            when(lectorAuthService.autenticarPorEmail("inactivo@gmail.com"))
                    .thenThrow(BusinessException.conflict("USER_INACTIVE",
                            "Tu cuenta está inactiva."));

            mockMvc.perform(post("/portal/login")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("email", "inactivo@gmail.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/portal/catalogo"));

            verify(lectorAuthService).autenticarPorEmail("inactivo@gmail.com");
        }
    }

    @Nested
    class CerrarSesion {

        @Test
        void cerrarSesion_DebeInvalidarSesionYRedirigirALogin() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 5L);

            mockMvc.perform(post("/portal/logout").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/portal/login"));

            assertTrue(session.isInvalid());
        }

        @Test
        void cerrarSesion_SinSesionActiva_DebeIgualRedirigirALogin() throws Exception {
            mockMvc.perform(post("/portal/logout"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/portal/login"));
        }
    }
}
