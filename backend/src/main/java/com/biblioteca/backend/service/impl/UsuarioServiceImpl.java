package com.biblioteca.backend.service.impl;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.response.UsuarioCupoPrestamoResponse;
import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.entity.Usuario;
import com.biblioteca.backend.entity.EstadoPrestamo;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.ImagenRepository;
import com.biblioteca.backend.repository.UsuarioRepository;
import com.biblioteca.backend.repository.PrestamoRepository;
import com.biblioteca.backend.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ImagenRepository imagenRepository;
    private final PrestamoRepository prestamoRepository;
    private final AppProperties appProperties;

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> obtenerTodos(String buscar, Boolean estado, Pageable pageable) {
        return usuarioRepository.findByFilters(buscar, estado, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con el ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con el email: " + email));
    }

    @Override
    @Transactional
    public Usuario crearUsuario(Usuario usuario, UUID fotoId) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw BusinessException.conflict("EMAIL_ALREADY_EXISTS", "Ya existe un usuario registrado con el email: " + usuario.getEmail());
        }

        usuario.setFoto(buscarFotoOpcional(fotoId));

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario actualizar(Long id, Usuario usuarioDetails, UUID fotoId, Boolean estado) {
        Usuario usuario = obtenerPorId(id);

        if (!usuario.getEmail().equalsIgnoreCase(usuarioDetails.getEmail()) 
                && usuarioRepository.existsByEmail(usuarioDetails.getEmail())) {
            throw BusinessException.conflict("EMAIL_ALREADY_EXISTS", "Ya existe un usuario registrado con el email: " + usuarioDetails.getEmail());
        }

        usuario.setNombre(usuarioDetails.getNombre());
        usuario.setEmail(usuarioDetails.getEmail());
        usuario.setTelefono(usuarioDetails.getTelefono());
        usuario.setDireccion(usuarioDetails.getDireccion());

        usuario.setFoto(buscarFotoOpcional(fotoId));

        if (estado != null) {
            usuario.setEstado(estado);
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = obtenerPorId(id);
        if (prestamoRepository.existsByUsuarioIdAndEstado(id, EstadoPrestamo.ACTIVO)) {
            throw BusinessException.conflict("USER_HAS_ACTIVE_LOANS", "No se puede desactivar el usuario porque tiene préstamos activos asociados.");
        }
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario activar(Long id) {
        Usuario usuario = obtenerPorId(id);
        usuario.setEstado(true);
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioCupoPrestamoResponse obtenerCupoPrestamo(Long id) {
        obtenerPorId(id);
        long librosEnPosesion = prestamoRepository.countTotalLibrosEnPoseccion(id);
        long maximoPermitido = appProperties.prestamo().maxLibrosPrestadosConcurrentes();
        return new UsuarioCupoPrestamoResponse(
                id,
                maximoPermitido,
                librosEnPosesion,
                Math.max(0, maximoPermitido - librosEnPosesion),
                prestamoRepository.findLibrosIdsPrestadosActivos(id)
        );
    }

    private Imagen buscarFotoOpcional(UUID fotoId) {
        if (fotoId == null) {
            return null;
        }
        return imagenRepository.findById(fotoId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen de foto de perfil no encontrada con el ID: " + fotoId));
    }
}
