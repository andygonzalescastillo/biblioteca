package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.config.AppProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfiguracionController.class)
@Import(ConfiguracionControllerTest.TestConfig.class)
class ConfiguracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AppProperties appProperties() {
            return new AppProperties(
                "uploads",
                new AppProperties.Cors(List.of("http://localhost:3000")),
                new AppProperties.Prestamo(7, 1, 30, 3, 5), null);
        }
    }

    @Nested
    class ObtenerConfiguracionPublica {

        @Test
        void obtenerConfiguracionPublica_DebeRetornarConfiguracionCompleta() throws Exception {
            mockMvc.perform(get("/api/configuracion/publica")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.prestamo").exists())
                    .andExpect(jsonPath("$.prestamo.diasDefault").value(7))
                    .andExpect(jsonPath("$.prestamo.diasMinimo").value(1))
                    .andExpect(jsonPath("$.prestamo.diasMaximo").value(30))
                    .andExpect(jsonPath("$.prestamo.cantidadReservaMaxima").value(3))
                    .andExpect(jsonPath("$.prestamo.maxLibrosPrestadosConcurrentes").value(5));
        }

        @Test
        void obtenerConfiguracionPublica_DebeRetornarContentTypeJson() throws Exception {
            mockMvc.perform(get("/api/configuracion/publica"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        void obtenerConfiguracionPublica_ConValoresDiferentes_DebeReflejarProperties() throws Exception {
            mockMvc.perform(get("/api/configuracion/publica")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.prestamo.diasDefault").isNumber())
                    .andExpect(jsonPath("$.prestamo.diasMinimo").isNumber())
                    .andExpect(jsonPath("$.prestamo.diasMaximo").isNumber())
                    .andExpect(jsonPath("$.prestamo.cantidadReservaMaxima").isNumber())
                    .andExpect(jsonPath("$.prestamo.maxLibrosPrestadosConcurrentes").isNumber());
        }
    }
}
