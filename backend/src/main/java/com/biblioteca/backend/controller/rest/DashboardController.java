package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.dto.response.DashboardResumenResponse;
import com.biblioteca.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "7. Dashboard", description = "Endpoints de resumen para métricas, préstamos recientes y alertas de inventario")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumen")
    @Operation(operationId = "71_dashboard_resumen",
               summary = "Obtener resumen del dashboard",
               description = "Retorna métricas generales, préstamos recientes y libros con stock crítico para la pantalla principal.")
    public ResponseEntity<DashboardResumenResponse> obtenerResumen() {
        return ResponseEntity.ok(dashboardService.obtenerResumen());
    }
}
