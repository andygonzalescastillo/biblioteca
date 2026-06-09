package com.biblioteca.backend.controller.mvc;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.response.UsuarioCupoPrestamoResponse;
import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.entity.Libro;
import com.biblioteca.backend.entity.Usuario;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.mvc.portal.PortalConstantes;
import com.biblioteca.backend.service.UsuarioService;
import com.biblioteca.backend.service.portal.LectorAuthService;
import com.biblioteca.backend.service.portal.LectorCarritoService;
import com.biblioteca.backend.service.portal.LectorPrestamoService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
@Import(ReservaControllerTest.TestConfig.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LectorAuthService lectorAuthService;

    @MockitoBean
    private LectorCarritoService lectorCarritoService;

    @MockitoBean
    private LectorPrestamoService lectorPrestamoService;

    @MockitoBean
    private UsuarioService usuarioService;

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
    class Carrito {

        @Test
        void carrito_DebeRetornarVistaYModeloCorrecto() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, new ArrayList<>(List.of(100L)));

            Usuario lector = Usuario.builder().id(10L).nombre("Lector Test").build();
            Categoria cat = Categoria.builder().id(1L).nombre("Novela").build();
            List<Libro> librosCarrito = List.of(Libro.builder().id(100L).titulo("Libro A").stock(5).categoria(cat).build());
            UsuarioCupoPrestamoResponse cupoPrestamo = new UsuarioCupoPrestamoResponse(10L, 5, 2, 3, List.of());

            when(lectorAuthService.obtenerLector(10L)).thenReturn(lector);
            when(lectorCarritoService.obtenerLibrosCarrito(anyList())).thenReturn(librosCarrito);
            when(usuarioService.obtenerCupoPrestamo(10L)).thenReturn(cupoPrestamo);

            mockMvc.perform(get("/portal/carrito").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("portal/carrito"))
                    .andExpect(model().attribute("lector", lector))
                    .andExpect(model().attribute("librosCarrito", librosCarrito))
                    .andExpect(model().attribute("carritoCantidad", 1))
                    .andExpect(model().attribute("diasPrestamoDefault", 7))
                    .andExpect(model().attribute("cupoDisponiblePrestamo", is(2L)))
                    .andExpect(model().attribute("cupoPrestamo", cupoPrestamo));
        }
    }

    @Nested
    class CarritoFragmento {

        @Test
        void carritoFragmento_DebeRetornarFragmentoCorrecto() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, new ArrayList<>(List.of(100L)));

            Categoria cat = Categoria.builder().id(1L).nombre("Novela").build();
            List<Libro> librosCarrito = List.of(Libro.builder().id(100L).titulo("Libro A").stock(5).categoria(cat).build());
            when(lectorCarritoService.obtenerLibrosCarrito(anyList())).thenReturn(librosCarrito);

            mockMvc.perform(get("/portal/carrito/fragmento").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("portal/fragmentos/carrito-drawer :: lista"))
                    .andExpect(model().attribute("librosCarrito", librosCarrito));
        }
    }

    @Nested
    class AgregarLibro {

        @Test
        void agregarLibroAjax_ConDatosValidos_DebeRetornarFragmentoEInsertarEnSesion() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, new ArrayList<>(List.of(100L)));

            Libro libro = Libro.builder().id(101L).stock(2).build();
            UsuarioCupoPrestamoResponse cup = new UsuarioCupoPrestamoResponse(10L, 5, 2, 3, List.of());

            when(lectorCarritoService.obtenerLibroAgregable(101L, 10L)).thenReturn(libro);
            when(usuarioService.obtenerCupoPrestamo(10L)).thenReturn(cup);

            mockMvc.perform(post("/portal/carrito/libros/101")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("portal/fragmentos/carrito-drawer :: lista"));

            List<?> list = (List<?>) session.getAttribute(PortalConstantes.SESSION_CARRITO_LIBROS);
            assertTrue(list.contains(101L));
        }

        @Test
        void agregarLibroAjax_CuandoSuperaCupo_DebeRetornarBadRequest() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, new ArrayList<>(List.of(100L, 102L)));

            Libro libro = Libro.builder().id(101L).stock(2).build();
            UsuarioCupoPrestamoResponse cup = new UsuarioCupoPrestamoResponse(10L, 5, 3, 2, List.of());

            when(lectorCarritoService.obtenerLibroAgregable(101L, 10L)).thenReturn(libro);
            when(usuarioService.obtenerCupoPrestamo(10L)).thenReturn(cup);

            mockMvc.perform(post("/portal/carrito/libros/101")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .session(session))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().exists("X-Error-Message"));
        }

        @Test
        void agregarLibro_Tradicional_DebeRedirigirConFlashAttribute() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, new ArrayList<>(List.of()));

            Libro libro = Libro.builder().id(100L).stock(5).build();
            UsuarioCupoPrestamoResponse cup = new UsuarioCupoPrestamoResponse(10L, 5, 2, 3, List.of());

            when(lectorCarritoService.obtenerLibroAgregable(100L, 10L)).thenReturn(libro);
            when(usuarioService.obtenerCupoPrestamo(10L)).thenReturn(cup);

            mockMvc.perform(post("/portal/carrito/libros/100")
                    .header("Referer", "/portal/catalogo")
                    .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/portal/catalogo"));

            List<?> list = (List<?>) session.getAttribute(PortalConstantes.SESSION_CARRITO_LIBROS);
            assertTrue(list.contains(100L));
        }
    }

    @Nested
    class ModificarCarrito {

        @Test
        void reducirLibroAjax_CuandoExiste_DebeRemoverUnidadYSesion() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, new ArrayList<>(List.of(100L, 100L)));

            mockMvc.perform(post("/portal/carrito/libros/100/reducir")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("portal/fragmentos/carrito-drawer :: lista"));

            List<?> list = (List<?>) session.getAttribute(PortalConstantes.SESSION_CARRITO_LIBROS);
            org.junit.jupiter.api.Assertions.assertEquals(1, list.size());
        }

        @Test
        void quitarLibroAjax_DebeRemoverTodasLasUnidades() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, new ArrayList<>(List.of(100L, 101L, 100L)));

            mockMvc.perform(post("/portal/carrito/libros/100/quitar")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .session(session))
                    .andExpect(status().isOk());

            List<?> list = (List<?>) session.getAttribute(PortalConstantes.SESSION_CARRITO_LIBROS);
            assertFalse(list.contains(100L));
        }
    }

    @Nested
    class ConfirmarPrestamo {

        @Test
        void confirmarPrestamo_ConDatosValidos_DebeCrearPrestamoYLimpiarCarrito() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, new ArrayList<>(List.of(100L, 101L)));

            doReturn(null).when(lectorPrestamoService)
                    .registrarPrestamoDesdeCarrito(eq(10L), anyList(), eq(7));

            mockMvc.perform(post("/portal/carrito/confirmar")
                    .param("diasPrestamo", "7")
                    .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/portal/mis-prestamos"));

            org.junit.jupiter.api.Assertions.assertNull(session.getAttribute(PortalConstantes.SESSION_CARRITO_LIBROS));
            verify(lectorPrestamoService).registrarPrestamoDesdeCarrito(eq(10L), eq(List.of(100L, 101L)), eq(7));
        }

        @Test
        void confirmarPrestamo_CuandoFalla_DebeRedirigirAlCarrito() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, new ArrayList<>(List.of(100L)));

            doThrow(BusinessException.badRequest("OUT_OF_STOCK", "No hay stock."))
                    .when(lectorPrestamoService).registrarPrestamoDesdeCarrito(anyLong(), anyList(), anyInt());

            mockMvc.perform(post("/portal/carrito/confirmar")
                    .param("diasPrestamo", "7")
                    .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/portal/carrito"));

            org.junit.jupiter.api.Assertions.assertNotNull(session.getAttribute(PortalConstantes.SESSION_CARRITO_LIBROS));
        }
    }
}
