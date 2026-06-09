package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.dto.request.AutorRequest;
import com.biblioteca.backend.dto.response.AutorResponse;
import com.biblioteca.backend.mapper.AutorMapper;
import com.biblioteca.backend.service.AutorService;
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
@RequestMapping("/api/autores")
@RequiredArgsConstructor
@Tag(name = "3. Autores", description = "Endpoints para la gestión de los autores literarios")
public class AutorController {

    private final AutorService autorService;
    private final AutorMapper autorMapper;

    @GetMapping
    @Operation(operationId = "31_autores_listar",
               summary = "Listar autores paginados con filtros",
               description = "Retorna un listado paginado de autores, permitiendo buscar por texto (nombre/biografía) y filtrar por estado (activo/inactivo).")
    public ResponseEntity<Page<AutorResponse>> obtenerTodos(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean estado,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(autorService.obtenerTodos(buscar, estado, pageable).map(autorMapper::toResponse));
    }

    @PostMapping
    @Operation(operationId = "32_autores_crear",
               summary = "Crear un nuevo autor",
               description = "Registra un nuevo autor en la biblioteca con su nombre, biografía, fecha de nacimiento y foto.")
    public ResponseEntity<AutorResponse> crearAutor(@Valid @RequestBody AutorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                autorMapper.toResponse(autorService.crearAutor(autorMapper.toEntity(request), request.fotoId()))
        );
    }

    @GetMapping("/{id}")
    @Operation(operationId = "33_autores_obtener",
               summary = "Obtener autor por ID",
               description = "Busca y retorna los detalles de un autor específico basado en su ID único.")
    public ResponseEntity<AutorResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(autorMapper.toResponse(autorService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "34_autores_actualizar",
               summary = "Actualizar autor",
               description = "Modifica los datos (nombre, biografía, fecha de nacimiento, foto, estado) de un autor existente.")
    public ResponseEntity<AutorResponse> actualizar(@PathVariable Long id, @Valid @RequestBody AutorRequest request) {
        return ResponseEntity.ok(
                autorMapper.toResponse(autorService.actualizar(id, autorMapper.toEntity(request), request.fotoId(), request.estado()))
        );
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "35_autores_eliminar",
               summary = "Eliminar autor",
               description = "Elimina lógicamente un autor del sistema siempre que no tenga libros activos asociados.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        autorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activar")
    @Operation(operationId = "36_autores_activar",
               summary = "Activar autor",
               description = "Reactiva lógicamente un autor del sistema.")
    public ResponseEntity<AutorResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(autorMapper.toResponse(autorService.activar(id)));
    }
}
