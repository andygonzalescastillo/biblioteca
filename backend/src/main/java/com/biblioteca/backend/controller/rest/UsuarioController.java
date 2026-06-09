package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.dto.request.UsuarioRequest;
import com.biblioteca.backend.dto.response.UsuarioCupoPrestamoResponse;
import com.biblioteca.backend.dto.response.UsuarioResponse;
import com.biblioteca.backend.mapper.UsuarioMapper;
import com.biblioteca.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "5. Usuarios", description = "Endpoints para la gestión de los usuarios registrados en la biblioteca")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @GetMapping
    @Operation(operationId = "51_usuarios_listar",
               summary = "Listar usuarios paginados con filtros",
               description = "Retorna un listado paginado de usuarios, permitiendo buscar por texto (nombre/email) y filtrar por estado (activo/inactivo).")
    public ResponseEntity<Page<UsuarioResponse>> obtenerTodos(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean estado,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(usuarioService.obtenerTodos(buscar, estado, pageable).map(usuarioMapper::toResponse));
    }

    @PostMapping
    @Operation(operationId = "52_usuarios_crear",
               summary = "Crear un nuevo usuario",
               description = "Registra un nuevo usuario en la biblioteca. El email debe ser único. Opcionalmente acepta 'fotoId' (UUID de imagen previamente subida).")
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                usuarioMapper.toResponse(usuarioService.crearUsuario(usuarioMapper.toEntity(request), request.fotoId()))
        );
    }

    @GetMapping("/{id}")
    @Operation(operationId = "53_usuarios_obtener",
               summary = "Obtener usuario por ID",
               description = "Busca y retorna los detalles de un usuario específico basado en su ID.")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioMapper.toResponse(usuarioService.obtenerPorId(id)));
    }

    @GetMapping("/email/{email}")
    @Operation(operationId = "54_usuarios_obtener_email",
               summary = "Obtener usuario por email",
               description = "Busca y retorna los detalles de un usuario específico usando su dirección de correo electrónico.")
    public ResponseEntity<UsuarioResponse> obtenerPorEmail(@PathVariable String email) {
        return ResponseEntity.ok(usuarioMapper.toResponse(usuarioService.obtenerPorEmail(email)));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "55_usuarios_actualizar",
               summary = "Actualizar usuario",
               description = "Modifica los datos (nombre, email, teléfono, dirección, foto) de un usuario existente.")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request
    ) {
        return ResponseEntity.ok(
                usuarioMapper.toResponse(
                        usuarioService.actualizar(id, usuarioMapper.toEntity(request), request.fotoId(), request.estado())
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "56_usuarios_eliminar",
               summary = "Eliminar usuario",
               description = "Elimina lógicamente un usuario del sistema. No se puede eliminar si tiene préstamos activos asociados.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activar")
    @Operation(operationId = "57_usuarios_activar",
               summary = "Activar usuario",
               description = "Reactiva lógicamente un usuario del sistema.")
    public ResponseEntity<UsuarioResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioMapper.toResponse(usuarioService.activar(id)));
    }

    @GetMapping("/{id}/cupo-prestamo")
    @Operation(operationId = "58_usuarios_cupo_prestamo",
               summary = "Obtener cupo de préstamo del usuario",
               description = "Retorna el máximo permitido, libros actualmente en posesión y cupo disponible para nuevos préstamos.")
    public ResponseEntity<UsuarioCupoPrestamoResponse> obtenerCupoPrestamo(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerCupoPrestamo(id));
    }
}
