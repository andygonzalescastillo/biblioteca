package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.dto.request.CategoriaRequest;
import com.biblioteca.backend.dto.response.CategoriaResponse;
import com.biblioteca.backend.mapper.CategoriaMapper;
import com.biblioteca.backend.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "2. Categorías", description = "Endpoints para la gestión de las categorías literarias del catálogo")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaMapper categoriaMapper;

    @GetMapping
    @Operation(operationId = "21_categorias_listar",
               summary = "Listar categorías paginadas con filtros",
               description = "Retorna un listado paginado de categorías, permitiendo buscar por texto (nombre/descripción) y filtrar por estado (activo/inactivo).")
    public ResponseEntity<Page<CategoriaResponse>> obtenerTodas(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean estado,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(categoriaService.obtenerTodas(buscar, estado, pageable).map(categoriaMapper::toResponse));
    }

    @PostMapping
    @Operation(operationId = "22_categorias_crear",
               summary = "Crear una nueva categoría",
               description = "Registra una nueva categoría en la biblioteca. El nombre debe ser único.")
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                categoriaMapper.toResponse(categoriaService.crearCategoria(categoriaMapper.toEntity(request)))
        );
    }

    @GetMapping("/{id}")
    @Operation(operationId = "23_categorias_obtener",
               summary = "Obtener categoría por ID",
               description = "Busca y retorna los detalles de una categoría literaria específica basada en su ID único.")
    public ResponseEntity<CategoriaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaMapper.toResponse(categoriaService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "24_categorias_actualizar",
               summary = "Actualizar categoría",
               description = "Modifica los datos (nombre, descripción) de una categoría literaria existente.")
    public ResponseEntity<CategoriaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(
                categoriaMapper.toResponse(categoriaService.actualizar(id, categoriaMapper.toEntity(request), request.estado()))
        );
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "25_categorias_eliminar",
               summary = "Eliminar categoría",
               description = "Elimina lógicamente una categoría. No se puede eliminar si tiene libros activos asociados.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activar")
    @Operation(operationId = "26_categorias_activar",
               summary = "Activar categoría",
               description = "Reactiva lógicamente una categoría del sistema.")
    public ResponseEntity<CategoriaResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaMapper.toResponse(categoriaService.activar(id)));
    }
}
