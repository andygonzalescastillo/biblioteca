package com.biblioteca.backend.controller.mvc;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.mvc.portal.PortalConstantes;
import com.biblioteca.backend.service.portal.LectorAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final LectorAuthService lectorAuthService;
    private final AppProperties appProperties;

    @GetMapping("/portal/login")
    public String login(HttpSession session, Model model) {
        if (session.getAttribute(PortalConstantes.SESSION_USUARIO_ID) != null) {
            return "redirect:/portal/catalogo";
        }
        agregarConfiguracionPrestamo(model);
        return "portal/login";
    }

    @PostMapping("/portal/login")
    public String iniciarSesion(
            @RequestParam String email,
            HttpSession session,
            Model model
    ) {
        try {
            var usuario = lectorAuthService.autenticarPorEmail(email);
            session.setAttribute(PortalConstantes.SESSION_USUARIO_ID, usuario.getId());
            return "redirect:/portal/catalogo";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("email", email);
            agregarConfiguracionPrestamo(model);
            return "portal/login";
        }
    }

    @PostMapping("/portal/logout")
    public String cerrarSesion(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("mensaje", "Sesión cerrada correctamente.");
        return "redirect:/portal/login";
    }

    private void agregarConfiguracionPrestamo(Model model) {
        model.addAttribute("diasPrestamoDefault", appProperties.prestamo().diasDefault());
        model.addAttribute("diasPrestamoMinimo", appProperties.prestamo().diasMinimo());
        model.addAttribute("diasPrestamoMaximo", appProperties.prestamo().diasMaximo());
        model.addAttribute("cantidadReservaMaxima", appProperties.prestamo().cantidadReservaMaxima());
        model.addAttribute("maxLibrosPrestadosConcurrentes", appProperties.prestamo().maxLibrosPrestadosConcurrentes());
    }
}
