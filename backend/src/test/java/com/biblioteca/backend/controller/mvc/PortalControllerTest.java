package com.biblioteca.backend.controller.mvc;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.response.*;
import com.biblioteca.backend.entity.*;
import com.biblioteca.backend.mvc.portal.PortalConstantes;
import com.biblioteca.backend.service.UsuarioService;
import com.biblioteca.backend.service.portal.LectorAuthService;
import com.biblioteca.backend.service.portal.LectorCarritoService;
import com.biblioteca.backend.service.portal.LectorCatalogoService;
import com.biblioteca.backend.service.portal.LectorPrestamoService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortalController.class)
@Import(PortalControllerTest.TestConfig.class)
class PortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LectorAuthService lectorAuthService;

    @MockitoBean
    private LectorCatalogoService lectorCatalogoService;

    @MockitoBean
    private LectorPrestamoService lectorPrestamoService;

    @MockitoBean
    private LectorCarritoService lectorCarritoService;

    @MockitoBean
    private UsuarioService usuarioService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AppProperties appProperties() {
            return new AppProperties(
                "uploads",
                new AppProperties.Cors(List.of("http://localhost:3000")),
                new AppProperties.Prestamo(7, 1, 30, 3, 5), null);
        }
    }

    @Nested
    class Inicio {

        @Test
        void inicio_SinSesion_DebeRedirigirALogin() throws Exception {
            mockMvc.perform(get("/portal"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/portal/login"));
        }

        @Test
        void inicio_ConSesion_DebeRedirigirAlCatalogo() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 1L);

            mockMvc.perform(get("/portal").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/portal/catalogo"));
        }
    }

    @Nested
    class Catalogo {

        @Test
        void catalogo_DebeRetornarVistaYModeloCorrecto() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, List.of(100L, 101L, 100L));

            Usuario lector = Usuario.builder().id(10L).nombre("Lector Test").build();
            Page<Libro> librosPage = new PageImpl<>(List.of());
            
            Categoria categoria = Categoria.builder().id(1L).nombre("Novela").estado(true).build();
            List<Categoria> categorias = List.of(categoria);
            
            Autor autor = Autor.builder().id(2L).nombre("Autor A").biografia("Biografía").estado(true).build();
            List<Autor> autores = List.of(autor);
            
            List<Libro> librosCarrito = List.of(
                    Libro.builder().id(100L).titulo("Libro A").isbn("ISBN-A").categoria(categoria).build(),
                    Libro.builder().id(101L).titulo("Libro B").isbn("ISBN-B").categoria(categoria).build()
            );

            UsuarioCupoPrestamoResponse cupoPrestamo = new UsuarioCupoPrestamoResponse(10L, 5, 2, 3, List.of(50L));

            when(lectorAuthService.obtenerLector(10L)).thenReturn(lector);
            when(lectorCatalogoService.obtenerCatalogo(any(), any(), any(), any(Pageable.class))).thenReturn(librosPage);
            when(lectorCatalogoService.obtenerCategoriasActivas()).thenReturn(categorias);
            when(lectorCatalogoService.obtenerAutoresActivos()).thenReturn(autores);
            when(lectorCarritoService.obtenerLibrosCarrito(anyList())).thenReturn(librosCarrito);
            when(usuarioService.obtenerCupoPrestamo(10L)).thenReturn(cupoPrestamo);

            mockMvc.perform(get("/portal/catalogo")
                    .param("buscar", "Quijote")
                    .param("categoriaId", "1")
                    .param("autorId", "2")
                    .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("portal/catalogo"))
                    .andExpect(model().attribute("buscar", "Quijote"))
                    .andExpect(model().attribute("categoriaId", 1L))
                    .andExpect(model().attribute("autorId", 2L))
                    .andExpect(model().attribute("categorias", categorias))
                    .andExpect(model().attribute("autores", autores))
                    .andExpect(model().attribute("librosPage", librosPage))
                    .andExpect(model().attribute("lector", lector))
                    .andExpect(model().attribute("carritoCantidad", is(3)))
                    .andExpect(model().attribute("librosCarrito", librosCarrito))
                    .andExpect(model().attribute("cupoPrestamo", cupoPrestamo))
                    .andExpect(model().attribute("maxLibrosPrestadosConcurrentes", is(5L)))
                    .andExpect(model().attribute("librosEnPosesionCantidad", is(2L)))
                    .andExpect(model().attribute("cupoDisponiblePrestamo", is(0L)));
        }
    }

    @Nested
    class DetalleLibro {

        @Test
        void detalleLibro_DebeRetornarVistaYModeloDetalle() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, List.of(100L));

            Usuario lector = Usuario.builder().id(10L).nombre("Lector Test").build();
            Categoria categoria = Categoria.builder().id(1L).nombre("Novela").estado(true).build();
            Libro libroDetalle = Libro.builder().id(100L).titulo("Libro A").isbn("ISBN-A").categoria(categoria).stock(5).build();
            
            UsuarioCupoPrestamoResponse cupoPrestamo = new UsuarioCupoPrestamoResponse(10L, 5, 2, 3, List.of());

            when(lectorAuthService.obtenerLector(10L)).thenReturn(lector);
            when(lectorCatalogoService.obtenerLibroActivo(100L)).thenReturn(libroDetalle);
            when(usuarioService.obtenerCupoPrestamo(10L)).thenReturn(cupoPrestamo);

            mockMvc.perform(get("/portal/libros/100").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("portal/detalle-libro"))
                    .andExpect(model().attribute("libro", libroDetalle))
                    .andExpect(model().attribute("enCarrito", true))
                    .andExpect(model().attribute("diasPrestamoDefault", 7))
                    .andExpect(model().attribute("diasPrestamoMinimo", 1))
                    .andExpect(model().attribute("diasPrestamoMaximo", 30))
                    .andExpect(model().attribute("cantidadReservaMaxima", 3));
        }
    }

    @Nested
    class MisPrestamos {

        @Test
        void misPrestamos_DebeRetornarVistaMisPrestamosConPaginacion() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, 10L);

            Usuario lector = Usuario.builder().id(10L).nombre("Lector Test").build();
            Page<Prestamo> prestamosPage = new PageImpl<>(List.of());
            UsuarioCupoPrestamoResponse cupoPrestamo = new UsuarioCupoPrestamoResponse(10L, 5, 2, 3, List.of());

            when(lectorAuthService.obtenerLector(10L)).thenReturn(lector);
            when(lectorPrestamoService.obtenerPrestamosDelLector(eq(10L), any(Pageable.class))).thenReturn(prestamosPage);
            when(usuarioService.obtenerCupoPrestamo(10L)).thenReturn(cupoPrestamo);

            mockMvc.perform(get("/portal/mis-prestamos").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("portal/mis-prestamos"))
                    .andExpect(model().attribute("prestamosPage", prestamosPage))
                    .andExpect(model().attribute("lector", lector));
        }
    }
}
