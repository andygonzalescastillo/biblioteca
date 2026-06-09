package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.dto.response.ConfiguracionPublicaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
@Tag(name = "8. Configuración", description = "Endpoints de configuración pública consumible por el frontend")
public class ConfiguracionController {

    private final AppProperties appProperties;

    @GetMapping("/publica")
    @Operation(operationId = "81_configuracion_publica",
               summary = "Obtener configuración pública",
               description = "Retorna reglas visibles de negocio que el frontend puede usar para límites, mensajes y controles.")
    public ResponseEntity<ConfiguracionPublicaResponse> obtenerConfiguracionPublica() {
        AppProperties.Prestamo prestamo = appProperties.prestamo();
        return ResponseEntity.ok(new ConfiguracionPublicaResponse(
                new ConfiguracionPublicaResponse.Prestamo(
                        prestamo.diasDefault(),
                        prestamo.diasMinimo(),
                        prestamo.diasMaximo(),
                        prestamo.cantidadReservaMaxima(),
                        prestamo.maxLibrosPrestadosConcurrentes()
                )
        ));
    }
}
