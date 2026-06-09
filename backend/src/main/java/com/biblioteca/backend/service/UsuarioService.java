package com.biblioteca.backend.service;

import com.biblioteca.backend.entity.Usuario;
import com.biblioteca.backend.dto.response.UsuarioCupoPrestamoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface UsuarioService {
    Page<Usuario> obtenerTodos(String buscar, Boolean estado, Pageable pageable);
    Usuario obtenerPorId(Long id);
    Usuario obtenerPorEmail(String email);
    Usuario crearUsuario(Usuario usuario, UUID fotoId);
    Usuario actualizar(Long id, Usuario usuarioDetails, UUID fotoId, Boolean estado);
    void eliminar(Long id);
    Usuario activar(Long id);
    UsuarioCupoPrestamoResponse obtenerCupoPrestamo(Long id);
}
