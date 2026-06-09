package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.response.DetallePrestamoResponse;
import com.biblioteca.backend.dto.response.PrestamoResponse;
import com.biblioteca.backend.entity.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    PrestamoMapperImpl.class,
    UsuarioMapperImpl.class,
    LibroMapperImpl.class,
    CategoriaMapperImpl.class,
    AutorMapperImpl.class,
    ImagenMapperImpl.class
})
class PrestamoMapperTest {

    @Autowired
    private PrestamoMapper prestamoMapper;

    private Usuario buildUsuario() {
        return Usuario.builder()
            .id(1L).nombre("Ana López").email("ana@test.com").estado(true).build();
    }

    private Libro buildLibro() {
        Categoria categoria = Categoria.builder()
            .id(1L).nombre("Ficción").estado(true).build();
        return Libro.builder()
            .id(10L).titulo("El Quijote").isbn("978-000").stock(5)
            .categoria(categoria).autores(Set.of()).estado(true).build();
    }

    @Nested
    class ToResponse {

        @Test
        void conPrestamoActivo_MapearTodosLosCampos() {
            Instant ahora = Instant.now();
            Instant limite = ahora.plus(7, ChronoUnit.DAYS);
            Libro libro = buildLibro();
            DetallePrestamo detalle = DetallePrestamo.builder()
                .id(100L).libro(libro).cantidad(2).build();
            Prestamo prestamo = Prestamo.builder()
                .id(1L)
                .usuario(buildUsuario())
                .fechaPrestamo(ahora)
                .fechaDevolucionLimite(limite)
                .estado(EstadoPrestamo.ACTIVO)
                .detalles(List.of(detalle))
                .build();

            PrestamoResponse response = prestamoMapper.toResponse(prestamo);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals(EstadoPrestamo.ACTIVO, response.estado());
            assertEquals(ahora, response.fechaPrestamo());
            assertEquals(limite, response.fechaDevolucionLimite());
            assertNull(response.fechaDevolucionReal());
            assertNotNull(response.usuario());
            assertEquals("Ana López", response.usuario().nombre());
            assertEquals(1, response.detalles().size());
            assertEquals(10L, response.detalles().getFirst().libro().id());
            assertEquals(2, response.detalles().getFirst().cantidad());
        }

        @Test
        void conPrestamoDevuelto_FechaDevolucionRealMapeada() {
            Instant ahora = Instant.now();
            Instant devolucion = ahora.minus(1, ChronoUnit.DAYS);
            Prestamo prestamo = Prestamo.builder()
                .id(2L)
                .usuario(buildUsuario())
                .fechaPrestamo(ahora.minus(10, ChronoUnit.DAYS))
                .fechaDevolucionLimite(ahora.minus(3, ChronoUnit.DAYS))
                .fechaDevolucionReal(devolucion)
                .estado(EstadoPrestamo.DEVUELTO)
                .detalles(List.of())
                .build();

            PrestamoResponse response = prestamoMapper.toResponse(prestamo);

            assertEquals(EstadoPrestamo.DEVUELTO, response.estado());
            assertEquals(devolucion, response.fechaDevolucionReal());
            assertTrue(response.detalles().isEmpty());
        }

        @Test
        void conEntidadNula_RetornaNull() {
            assertNull(prestamoMapper.toResponse(null));
        }
    }

    @Nested
    class ToDetalleResponse {

        @Test
        void conDetalle_MapearLibroYCantidad() {
            DetallePrestamo detalle = DetallePrestamo.builder()
                .id(50L)
                .libro(buildLibro())
                .cantidad(3)
                .build();

            DetallePrestamoResponse response = prestamoMapper.toDetalleResponse(detalle);

            assertNotNull(response);
            assertEquals(50L, response.id());
            assertEquals(3, response.cantidad());
            assertNotNull(response.libro());
            assertEquals("El Quijote", response.libro().titulo());
            assertEquals("Ficción", response.libro().categoria().nombre());
        }

        @Test
        void conDetallNulo_RetornaNull() {
            assertNull(prestamoMapper.toDetalleResponse(null));
        }
    }
}
