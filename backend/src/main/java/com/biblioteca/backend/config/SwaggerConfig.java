package com.biblioteca.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("📚 Sistema de Biblioteca - API Profesional")
                        .description("""
                                ### API REST para la gestión integral de una Biblioteca.

                                Este panel interactivo permite administrar el catálogo de libros, autores, \
                                categorías, usuarios y registrar préstamos y devoluciones con control automático de stock.

                                **Características clave:**
                                - 💾 Almacenamiento físico de imágenes en disco y mapeo en base de datos.
                                - 🔄 Control estricto y seguro de stock en préstamos y devoluciones.
                                - ⚙️ Mapeos de alto desempeño con MapStruct y DTO Records inmutables.
                                - 🛡️ Manejo centralizado y amigable de excepciones.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Andy Gonzales")
                                .email("andygonzales.2005@outlook.es")));
    }
}
