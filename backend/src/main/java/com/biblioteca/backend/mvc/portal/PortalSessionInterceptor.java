package com.biblioteca.backend.mvc.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PortalSessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var session = request.getSession(false);
        if (session != null && session.getAttribute(PortalConstantes.SESSION_USUARIO_ID) != null) {
            return true;
        }

        response.sendRedirect(request.getContextPath() + "/portal/login");
        return false;
    }
}
