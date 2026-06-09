package com.biblioteca.backend.exception.mvc;

import com.biblioteca.backend.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@ControllerAdvice(basePackages = "com.biblioteca.backend.controller.mvc")
@Slf4j
public class PortalMvcExceptionHandler {


    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFound(EntityNotFoundException ex,
                                       RedirectAttributes redirectAttributes,
                                       Model model,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        
        
        if (HttpMethod.GET.matches(request.getMethod())) {
            log.warn("Entidad no encontrada en GET {}: {}", request.getRequestURI(), ex.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("status", 404);
            model.addAttribute("path", request.getRequestURI());
            return "error";
        }
        
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return redirectToReferer(request);
    }

    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class, IllegalStateException.class})
    public String handleBadRequest(RuntimeException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return redirectToReferer(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model, HttpServletRequest request) {
        log.error("Error no controlado en el portal MVC: ", ex);
        model.addAttribute("message", "Ha ocurrido un error inesperado.");
        model.addAttribute("status", 500);
        return "error";
    }

    private String redirectToReferer(HttpServletRequest request) {
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
}
