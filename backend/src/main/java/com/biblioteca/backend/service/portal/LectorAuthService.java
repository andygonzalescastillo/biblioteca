package com.biblioteca.backend.service.portal;

import com.biblioteca.backend.entity.Usuario;

public interface LectorAuthService {
    Usuario autenticarPorEmail(String email);
    Usuario obtenerLector(Long lectorId);
}
