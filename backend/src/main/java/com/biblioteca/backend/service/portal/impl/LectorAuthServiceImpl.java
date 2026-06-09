package com.biblioteca.backend.service.portal.impl;

import com.biblioteca.backend.entity.Usuario;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.service.UsuarioService;
import com.biblioteca.backend.service.portal.LectorAuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LectorAuthServiceImpl implements LectorAuthService {

    private final UsuarioService usuarioService;

    @Override
    @Transactional(readOnly = true)
    public Usuario autenticarPorEmail(String email) {
        String emailNormalizado = Optional.ofNullable(email)
                .map(e -> e.trim().toLowerCase())
                .orElse("");

        if (emailNormalizado.isBlank()) {
            throw BusinessException.badRequest("EMAIL_REQUIRED", "Ingresa tu correo para continuar.");
        }

        try {
            Usuario usuario = usuarioService.obtenerPorEmail(emailNormalizado);
            if (!usuario.isEstado()) {
                throw BusinessException.conflict("USER_INACTIVE", "Tu cuenta está inactiva. Comunícate con la biblioteca.");
            }
            return usuario;
        } catch (EntityNotFoundException ex) {
            throw BusinessException.badRequest("READER_NOT_FOUND", "No encontramos un lector activo con ese correo.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerLector(Long lectorId) {
        return usuarioService.obtenerPorId(lectorId);
    }
}
