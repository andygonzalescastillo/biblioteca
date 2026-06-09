package com.biblioteca.backend.controller.mvc;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.response.UsuarioCupoPrestamoResponse;
import com.biblioteca.backend.mvc.portal.PortalConstantes;
import com.biblioteca.backend.service.UsuarioService;
import com.biblioteca.backend.service.portal.LectorAuthService;
import com.biblioteca.backend.service.portal.LectorCarritoService;
import com.biblioteca.backend.service.portal.LectorCatalogoService;
import com.biblioteca.backend.service.portal.LectorPrestamoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PortalController {

    private final LectorAuthService lectorAuthService;
    private final LectorCatalogoService lectorCatalogoService;
    private final LectorPrestamoService lectorPrestamoService;
    private final LectorCarritoService lectorCarritoService;
    private final UsuarioService usuarioService;
    private final AppProperties appProperties;

    @GetMapping("/portal")
    public String inicio(HttpSession session) {
        return session.getAttribute(PortalConstantes.SESSION_USUARIO_ID) == null
                ? "redirect:/portal/login" : "redirect:/portal/catalogo";
    }

    @GetMapping("/portal/catalogo")
    public String catalogo(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long autorId,
            @PageableDefault(size = 8, sort = "titulo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpSession session,
            Model model
    ) {
        agregarLector(model, session);
        model.addAttribute("librosPage", lectorCatalogoService.obtenerCatalogo(buscar, categoriaId, autorId, pageable));
        model.addAttribute("categorias", lectorCatalogoService.obtenerCategoriasActivas());
        model.addAttribute("autores", lectorCatalogoService.obtenerAutoresActivos());
        model.addAttribute("buscar", buscar);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("autorId", autorId);
        model.addAttribute("size", pageable.getPageSize());
        return "portal/catalogo";
    }

    @GetMapping("/portal/libros/{id}")
    public String detalleLibro(@PathVariable Long id, HttpSession session, Model model) {
        agregarLector(model, session);
        model.addAttribute("libro", lectorCatalogoService.obtenerLibroActivo(id));
        model.addAttribute("enCarrito", obtenerCarrito(session).contains(id));
        model.addAttribute("diasPrestamoDefault", appProperties.prestamo().diasDefault());
        model.addAttribute("diasPrestamoMinimo", appProperties.prestamo().diasMinimo());
        model.addAttribute("diasPrestamoMaximo", appProperties.prestamo().diasMaximo());
        model.addAttribute("cantidadReservaMaxima", appProperties.prestamo().cantidadReservaMaxima());
        model.addAttribute("maxLibrosPrestadosConcurrentes", appProperties.prestamo().maxLibrosPrestadosConcurrentes());
        return "portal/detalle-libro";
    }

    @GetMapping("/portal/mis-prestamos")
    public String misPrestamos(
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session,
            Model model
    ) {
        Long lectorId = (Long) session.getAttribute(PortalConstantes.SESSION_USUARIO_ID);
        agregarLector(model, session);
        model.addAttribute("prestamosPage", lectorPrestamoService.obtenerPrestamosDelLector(lectorId, pageable));
        model.addAttribute("size", pageable.getPageSize());
        return "portal/mis-prestamos";
    }

    private void agregarLector(Model model, HttpSession session) {
        Long lectorId = (Long) session.getAttribute(PortalConstantes.SESSION_USUARIO_ID);
        model.addAttribute("lector", lectorAuthService.obtenerLector(lectorId));
        List<Long> cartIds = obtenerCarrito(session);
        model.addAttribute("carritoCantidad", cartIds.size());
        model.addAttribute("librosCarrito", lectorCarritoService.obtenerLibrosCarrito(cartIds));
        model.addAttribute("carritoIds", cartIds);
        model.addAttribute("cantidadesPorLibro", calcularCantidades(cartIds));
        model.addAttribute("cantidadReservaMaxima", appProperties.prestamo().cantidadReservaMaxima());
        UsuarioCupoPrestamoResponse cupoPrestamo = usuarioService.obtenerCupoPrestamo(lectorId);
        model.addAttribute("cupoPrestamo", cupoPrestamo);
        model.addAttribute("maxLibrosPrestadosConcurrentes", cupoPrestamo.maximoPermitido());
        model.addAttribute("librosPrestadosIds", cupoPrestamo.librosPrestadosIds());
        model.addAttribute("librosEnPosesionCantidad", cupoPrestamo.librosEnPosesion());
        model.addAttribute("cupoDisponiblePrestamo", Math.max(0, cupoPrestamo.cupoDisponible() - cartIds.size()));
    }

    private Map<Long, Integer> calcularCantidades(List<Long> librosIds) {
        return librosIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(id -> 1)));
    }

    private List<Long> obtenerCarrito(HttpSession session) {
        Object carrito = session.getAttribute(PortalConstantes.SESSION_CARRITO_LIBROS);
        if (carrito instanceof List<?> librosIds) {
            return librosIds.stream()
                    .filter(Long.class::isInstance)
                    .map(Long.class::cast)
                    .toList();
        }
        return List.of();
    }
}
