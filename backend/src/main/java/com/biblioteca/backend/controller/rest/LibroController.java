package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.dto.request.LibroRequest;
import com.biblioteca.backend.dto.response.LibroResponse;
import com.biblioteca.backend.mapper.LibroMapper;
import com.biblioteca.backend.service.LibroService;
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
@RequestMapping("/api/libros")
@RequiredArgsConstructor
@Tag(name = "4. Libros", description = "Endpoints para la gestión del catálogo de libros, incluyendo relaciones con categorías, autores e imágenes de portada")
public class LibroController {

    private final LibroService libroService;
    private final LibroMapper libroMapper;

    @GetMapping
    @Operation(operationId = "41_libros_listar",
               summary = "Listar libros paginados con filtros",
               description = "Retorna un listado paginado del catálogo de libros. Permite buscar por texto (título/ISBN), filtrar por estado (activo/inactivo), por categoría y por autor.")
    public ResponseEntity<Page<LibroResponse>> obtenerTodos(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean estado,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long autorId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(libroService.obtenerTodos(buscar, estado, categoriaId, autorId, pageable).map(libroMapper::toResponse));
    }

    @PostMapping
    @Operation(operationId = "42_libros_crear",
               summary = "Crear un nuevo libro",
               description = "Registra un nuevo libro en el catálogo. Requiere un 'categoriaId' obligatorio. Opcionalmente acepta 'portadaId' (UUID de imagen previamente subida) y 'autoresIds' (set de IDs de autores existentes).")
    public ResponseEntity<LibroResponse> crearLibro(@Valid @RequestBody LibroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                libroMapper.toResponse(
                        libroService.crearLibro(libroMapper.toEntity(request), request.categoriaId(), request.portadaId(), request.autoresIds())
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(operationId = "43_libros_obtener",
               summary = "Obtener libro por ID",
               description = "Busca y retorna los detalles completos de un libro específico por su ID.")
    public ResponseEntity<LibroResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(libroMapper.toResponse(libroService.obtenerPorId(id)));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(operationId = "44_libros_obtener_isbn",
               summary = "Obtener libro por ISBN",
               description = "Busca y retorna los detalles de un libro específico usando su código ISBN único (ej: '978-0307474728').")
    public ResponseEntity<LibroResponse> obtenerPorIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(libroMapper.toResponse(libroService.obtenerPorIsbn(isbn)));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "45_libros_actualizar",
               summary = "Actualizar libro",
               description = "Modifica los datos de un libro existente: título, ISBN, stock, categoría, portada y autores asociados.")
    public ResponseEntity<LibroResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LibroRequest request
    ) {
        return ResponseEntity.ok(
                libroMapper.toResponse(
                        libroService.actualizar(id, libroMapper.toEntity(request), request.categoriaId(), request.portadaId(), request.autoresIds(), request.estado())
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "46_libros_eliminar",
               summary = "Eliminar libro",
               description = "Elimina lógicamente un libro del catálogo. No se puede eliminar si tiene préstamos activos asociados.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activar")
    @Operation(operationId = "47_libros_activar",
               summary = "Activar libro",
               description = "Reactiva lógicamente un libro en el catálogo.")
    public ResponseEntity<LibroResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(libroMapper.toResponse(libroService.activar(id)));
    }
}
