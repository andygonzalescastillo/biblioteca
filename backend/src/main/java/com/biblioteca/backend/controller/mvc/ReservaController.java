package com.biblioteca.backend.controller.mvc;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.entity.Libro;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.mvc.portal.PortalConstantes;
import com.biblioteca.backend.service.portal.LectorAuthService;
import com.biblioteca.backend.service.portal.LectorCarritoService;
import com.biblioteca.backend.service.portal.LectorPrestamoService;
import com.biblioteca.backend.service.UsuarioService;
import com.biblioteca.backend.dto.response.UsuarioCupoPrestamoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ReservaController {

    private static final String FRAGMENTO_CARRITO_DRAWER = "portal/fragmentos/carrito-drawer :: lista";

    private final LectorAuthService lectorAuthService;
    private final LectorCarritoService lectorCarritoService;
    private final LectorPrestamoService lectorPrestamoService;
    private final AppProperties appProperties;
    private final UsuarioService usuarioService;

    @GetMapping("/portal/carrito")
    public String carrito(HttpSession session, Model model) {
        Long lectorId = (Long) session.getAttribute(PortalConstantes.SESSION_USUARIO_ID);
        List<Long> librosIds = obtenerCarrito(session);

        model.addAttribute("lector", lectorAuthService.obtenerLector(lectorId));
        cargarModeloCarrito(model, librosIds);
        model.addAttribute("carritoIds", librosIds);
        model.addAttribute("carritoCantidad", librosIds.size());
        model.addAttribute("diasPrestamoDefault", appProperties.prestamo().diasDefault());
        model.addAttribute("diasPrestamoMinimo", appProperties.prestamo().diasMinimo());
        model.addAttribute("diasPrestamoMaximo", appProperties.prestamo().diasMaximo());
        model.addAttribute("maxLibrosPrestadosConcurrentes", appProperties.prestamo().maxLibrosPrestadosConcurrentes());

        UsuarioCupoPrestamoResponse cupoPrestamo = usuarioService.obtenerCupoPrestamo(lectorId);
        model.addAttribute("cupoDisponiblePrestamo", Math.max(0, cupoPrestamo.cupoDisponible() - librosIds.size()));
        model.addAttribute("cupoPrestamo", cupoPrestamo);
        model.addAttribute("librosEnPosesionCantidad", cupoPrestamo.librosEnPosesion());

        return "portal/carrito";
    }

    @GetMapping("/portal/carrito/fragmento")
    public String carritoFragmento(HttpSession session, Model model) {
        List<Long> librosIds = obtenerCarrito(session);
        cargarModeloCarrito(model, librosIds);
        return FRAGMENTO_CARRITO_DRAWER;
    }


    @PostMapping(value = "/portal/carrito/libros/{id}", headers = "X-Requested-With=XMLHttpRequest")
    public String agregarLibroAjax(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        try {
            Long lectorId = (Long) session.getAttribute(PortalConstantes.SESSION_USUARIO_ID);
            Libro libro = lectorCarritoService.obtenerLibroAgregable(id, lectorId);

            List<Long> librosIds = obtenerCarrito(session);
            UsuarioCupoPrestamoResponse cupoPrestamo = usuarioService.obtenerCupoPrestamo(lectorId);
            if (librosIds.size() >= cupoPrestamo.cupoDisponible()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setHeader("X-Error-Message", "No puedes agregar más libros. Has alcanzado el límite máximo de cupo de préstamos permitidos.");
                return null;
            }

            long cantidadActual = contarLibro(librosIds, id);

            if (cantidadActual >= appProperties.prestamo().cantidadReservaMaxima()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setHeader("X-Error-Message", "Has alcanzado la cantidad maxima permitida para este libro (" + appProperties.prestamo().cantidadReservaMaxima() + " unidades).");
                return null;
            }

            if (cantidadActual + 1 > libro.getStock()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setHeader("X-Error-Message", "No hay suficiente stock disponible para agregar otra unidad.");
                return null;
            }

            librosIds.add(id);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, librosIds);
            
            String appContext = request.getHeader("X-App-Context");
            if ("carrito".equals(appContext)) {
                cargarModeloCarritoCompleto(model, librosIds, lectorId);
                return "portal/carrito :: contenido";
            } else {
                cargarModeloCarrito(model, librosIds);
                return FRAGMENTO_CARRITO_DRAWER;
            }
        } catch (BusinessException | IllegalArgumentException | IllegalStateException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader("X-Error-Message", ex.getMessage());
            return null;
        }
    }

    @PostMapping(value = "/portal/carrito/libros/{id}/reducir", headers = "X-Requested-With=XMLHttpRequest")
    public String reducirLibroAjax(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        List<Long> librosIds = obtenerCarrito(session);
        if (librosIds.contains(id)) {
            librosIds.remove(id);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, librosIds);

            Long lectorId = (Long) session.getAttribute(PortalConstantes.SESSION_USUARIO_ID);
            String appContext = request.getHeader("X-App-Context");
            if ("carrito".equals(appContext)) {
                cargarModeloCarritoCompleto(model, librosIds, lectorId);
                return "portal/carrito :: contenido";
            } else {
                cargarModeloCarrito(model, librosIds);
                return FRAGMENTO_CARRITO_DRAWER;
            }
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setHeader("X-Error-Message", "El libro no esta en el carrito.");
        return null;
    }

    @PostMapping(value = "/portal/carrito/libros/{id}/quitar", headers = "X-Requested-With=XMLHttpRequest")
    public String quitarLibroAjax(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            HttpServletRequest request
    ) {
        List<Long> librosIds = obtenerCarrito(session);
        librosIds.removeIf(id::equals);
        session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, librosIds);

        Long lectorId = (Long) session.getAttribute(PortalConstantes.SESSION_USUARIO_ID);
        String appContext = request.getHeader("X-App-Context");
        if ("carrito".equals(appContext)) {
            cargarModeloCarritoCompleto(model, librosIds, lectorId);
            return "portal/carrito :: contenido";
        } else {
            cargarModeloCarrito(model, librosIds);
            return FRAGMENTO_CARRITO_DRAWER;
        }
    }

    @PostMapping("/portal/carrito/libros/{id}")
    public String agregarLibro(
            @PathVariable Long id,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Long lectorId = (Long) session.getAttribute(PortalConstantes.SESSION_USUARIO_ID);
            Libro libro = lectorCarritoService.obtenerLibroAgregable(id, lectorId);

            List<Long> librosIds = obtenerCarrito(session);
            UsuarioCupoPrestamoResponse cupoPrestamo = usuarioService.obtenerCupoPrestamo(lectorId);
            if (librosIds.size() >= cupoPrestamo.cupoDisponible()) {
                redirectAttributes.addFlashAttribute("error", "No puedes agregar más libros. Has alcanzado el límite máximo de cupo de préstamos permitidos.");
                return redirigirAlOrigen(request);
            }

            long cantidadActual = contarLibro(librosIds, id);

            if (cantidadActual >= appProperties.prestamo().cantidadReservaMaxima()) {
                redirectAttributes.addFlashAttribute("error", "Cantidad máxima alcanzada.");
            } else if (cantidadActual + 1 > libro.getStock()) {
                redirectAttributes.addFlashAttribute("error", "No hay suficiente stock disponible.");
            } else {
                librosIds.add(id);
                session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, librosIds);
                redirectAttributes.addFlashAttribute("mensaje", "Unidad agregada al carrito.");
            }
        } catch (BusinessException | IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return redirigirAlOrigen(request);
    }

    @PostMapping("/portal/carrito/libros/{id}/reducir")
    public String reducirLibro(@PathVariable Long id, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        List<Long> librosIds = obtenerCarrito(session);
        if (librosIds.contains(id)) {
            librosIds.remove(id);
            session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, librosIds);
            redirectAttributes.addFlashAttribute("mensaje", "Unidad reducida.");
        }
        return redirigirAlOrigen(request);
    }

    @PostMapping("/portal/carrito/libros/{id}/quitar")
    public String quitarLibro(@PathVariable Long id, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        List<Long> librosIds = obtenerCarrito(session);
        librosIds.removeIf(id::equals);
        session.setAttribute(PortalConstantes.SESSION_CARRITO_LIBROS, librosIds);
        redirectAttributes.addFlashAttribute("mensaje", "Libro quitado del carrito.");
        return redirigirAlOrigen(request);
    }

    @PostMapping("/portal/carrito/confirmar")
    public String confirmarPrestamo(
            @RequestParam(required = false) Integer diasPrestamo,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Long lectorId = (Long) session.getAttribute(PortalConstantes.SESSION_USUARIO_ID);
        List<Long> librosIds = obtenerCarrito(session);

        try {
            int dias = Optional.ofNullable(diasPrestamo).orElse(appProperties.prestamo().diasDefault());
            lectorPrestamoService.registrarPrestamoDesdeCarrito(lectorId, librosIds, dias);
            session.removeAttribute(PortalConstantes.SESSION_CARRITO_LIBROS);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Préstamo registrado con %d libro(s) por %d día(s).".formatted(librosIds.size(), dias));
            return "redirect:/portal/mis-prestamos";
        } catch (BusinessException | IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/portal/carrito";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Long> obtenerCarrito(HttpSession session) {
        Object carrito = session.getAttribute(PortalConstantes.SESSION_CARRITO_LIBROS);
        if (carrito instanceof List<?> librosIds) {
            return new ArrayList<>((List<Long>) librosIds);
        }
        return new ArrayList<>();
    }

    private String redirigirAlOrigen(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Referer"))
                .filter(ref -> !ref.isBlank())
                .flatMap(ref -> {
                    try {
                        var uri = UriComponentsBuilder.fromUriString(ref).build().toUri();
                        String path = uri.getPath();
                        String contextPath = request.getContextPath();
                        if (path == null || !path.startsWith(contextPath + "/portal")) {
                            return Optional.empty();
                        }
                        String query = uri.getRawQuery();
                        return Optional.of("redirect:" + path.substring(contextPath.length())
                                + (query == null ? "" : "?" + query));
                    } catch (IllegalArgumentException ex) {
                        return Optional.empty();
                    }
                })
                .orElse("redirect:/portal/catalogo");
    }

    private Map<Long, Integer> calcularCantidades(List<Long> librosIds) {
        return librosIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(id -> 1)));
    }

    private long contarLibro(List<Long> librosIds, Long libroId) {
        return librosIds.stream()
                .filter(libroId::equals)
                .count();
    }

    private void cargarModeloCarrito(Model model, List<Long> librosIds) {
        model.addAttribute("librosCarrito", lectorCarritoService.obtenerLibrosCarrito(librosIds));
        model.addAttribute("cantidadesPorLibro", calcularCantidades(librosIds));
        model.addAttribute("cantidadReservaMaxima", appProperties.prestamo().cantidadReservaMaxima());
    }

    private void cargarModeloCarritoCompleto(Model model, List<Long> librosIds, Long lectorId) {
        model.addAttribute("lector", lectorAuthService.obtenerLector(lectorId));
        cargarModeloCarrito(model, librosIds);
        model.addAttribute("carritoIds", librosIds);
        model.addAttribute("carritoCantidad", librosIds.size());
        model.addAttribute("diasPrestamoDefault", appProperties.prestamo().diasDefault());
        model.addAttribute("diasPrestamoMinimo", appProperties.prestamo().diasMinimo());
        model.addAttribute("diasPrestamoMaximo", appProperties.prestamo().diasMaximo());
        model.addAttribute("maxLibrosPrestadosConcurrentes", appProperties.prestamo().maxLibrosPrestadosConcurrentes());

        UsuarioCupoPrestamoResponse cupoPrestamo = usuarioService.obtenerCupoPrestamo(lectorId);
        model.addAttribute("cupoDisponiblePrestamo", Math.max(0, cupoPrestamo.cupoDisponible() - librosIds.size()));
        model.addAttribute("cupoPrestamo", cupoPrestamo);
        model.addAttribute("librosEnPosesionCantidad", cupoPrestamo.librosEnPosesion());
    }
}
