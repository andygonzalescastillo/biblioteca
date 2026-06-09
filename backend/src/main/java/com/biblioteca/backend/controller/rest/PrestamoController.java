package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.dto.request.PrestamoRequest;
import com.biblioteca.backend.dto.response.PrestamoResponse;
import com.biblioteca.backend.mapper.PrestamoMapper;
import com.biblioteca.backend.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.biblioteca.backend.entity.EstadoPrestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
@Tag(name = "6. Préstamos", description = "Endpoints para registrar préstamos de libros y gestionar devoluciones con control automático de stock")
public class PrestamoController {

    private final PrestamoService prestamoService;
    private final PrestamoMapper prestamoMapper;

    @GetMapping
    @Operation(operationId = "61_prestamos_listar",
               summary = "Listar préstamos paginados con filtros",
               description = "Retorna un listado paginado del historial de préstamos, permitiendo filtrar por usuario y por estado del préstamo.")
    public ResponseEntity<Page<PrestamoResponse>> obtenerTodos(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) EstadoPrestamo estado,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(prestamoService.obtenerTodos(usuarioId, estado, pageable).map(prestamoMapper::toResponse));
    }

    @PostMapping
    @Operation(operationId = "62_prestamos_registrar",
               summary = "Registrar un nuevo préstamo",
               description = "Crea un nuevo préstamo de libros para un usuario. **Automáticamente descuenta el stock** de cada libro prestado. Si el stock es insuficiente, se rechaza la operación. El campo 'diasLimite' establece la fecha máxima de devolución.")
    public ResponseEntity<PrestamoResponse> registrarPrestamo(@Valid @RequestBody PrestamoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                prestamoMapper.toResponse(
                        prestamoService.registrarPrestamo(toServiceRequest(request))
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(operationId = "63_prestamos_obtener",
               summary = "Obtener préstamo por ID",
               description = "Busca y retorna los detalles completos de un préstamo específico: usuario, libros prestados con cantidades, fechas y estado.")
    public ResponseEntity<PrestamoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoMapper.toResponse(prestamoService.obtenerPorId(id)));
    }


    @PutMapping("/{id}/devolucion")
    @Operation(operationId = "65_prestamos_devolver",
               summary = "Devolver un préstamo",
               description = "Marca un préstamo como DEVUELTO. **Automáticamente restaura el stock** de cada libro devuelto. Registra la fecha real de devolución. Si el préstamo ya fue devuelto, se rechaza la operación.")
    public ResponseEntity<PrestamoResponse> devolverPrestamo(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoMapper.toResponse(prestamoService.devolverPrestamo(id)));
    }

    private PrestamoService.PrestamoRequest toServiceRequest(PrestamoRequest request) {
        return new PrestamoService.PrestamoRequest(
                request.usuarioId(),
                request.diasLimite(),
                request.detalles().stream()
                        .map(detalle -> new PrestamoService.DetalleRequest(detalle.libroId(), detalle.cantidad()))
                        .toList()
        );
    }
}
