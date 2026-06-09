package com.biblioteca.backend.repository;

import com.biblioteca.backend.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PrestamoRepositoryTest {

    @Autowired private PrestamoRepository prestamoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LibroRepository libroRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private DetallePrestamoRepository detallePrestamoRepository;

    private Usuario usuario1;
    private Usuario usuario2;
    private Libro libro1;
    private Libro libro2;
    private Prestamo prestamoActivo;
    private Prestamo prestamoDevuelto;
    private Prestamo prestamoVencido;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.save(
            Categoria.builder().nombre("Ficción").estado(true).build()
        );

        usuario1 = usuarioRepository.save(
            Usuario.builder().nombre("Usuario Uno").email("uno@test.com").estado(true).build()
        );
        usuario2 = usuarioRepository.save(
            Usuario.builder().nombre("Usuario Dos").email("dos@test.com").estado(true).build()
        );

        libro1 = libroRepository.save(
            Libro.builder().titulo("Libro A").isbn("978-1111111111").stock(10).estado(true).categoria(categoria).build()
        );
        libro2 = libroRepository.save(
            Libro.builder().titulo("Libro B").isbn("978-2222222222").stock(10).estado(true).categoria(categoria).build()
        );

        Instant ahora = Instant.now();

        // prestamoActivo: usuario1, libro1(x2), vence en 7 días
        prestamoActivo = prestamoRepository.save(
            Prestamo.builder()
                .usuario(usuario1)
                .fechaPrestamo(ahora)
                .fechaDevolucionLimite(ahora.plus(7, ChronoUnit.DAYS))
                .estado(EstadoPrestamo.ACTIVO)
                .build()
        );
        detallePrestamoRepository.save(
            DetallePrestamo.builder().prestamo(prestamoActivo).libro(libro1).cantidad(2).build()
        );

        // prestamoDevuelto: usuario1, devuelto hace 21 días
        prestamoDevuelto = prestamoRepository.save(
            Prestamo.builder()
                .usuario(usuario1)
                .fechaPrestamo(ahora.minus(30, ChronoUnit.DAYS))
                .fechaDevolucionLimite(ahora.minus(20, ChronoUnit.DAYS))
                .fechaDevolucionReal(ahora.minus(21, ChronoUnit.DAYS))
                .estado(EstadoPrestamo.DEVUELTO)
                .build()
        );

        // prestamoVencido: usuario2, libro2(x1), venció hace 5 días (estado=ACTIVO)
        prestamoVencido = prestamoRepository.save(
            Prestamo.builder()
                .usuario(usuario2)
                .fechaPrestamo(ahora.minus(10, ChronoUnit.DAYS))
                .fechaDevolucionLimite(ahora.minus(5, ChronoUnit.DAYS))
                .estado(EstadoPrestamo.ACTIVO)
                .build()
        );
        detallePrestamoRepository.save(
            DetallePrestamo.builder().prestamo(prestamoVencido).libro(libro2).cantidad(1).build()
        );
    }

    @Nested
    class FindIdsByFilters {

        @Test
        void sinFiltros_RetornaTodos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = prestamoRepository.findIdsByFilters(null, null, pageable);

            assertEquals(3, resultado.getTotalElements());
        }

        @Test
        void filtroUsuario_RetornaSoloDeEseUsuario() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = prestamoRepository.findIdsByFilters(usuario1.getId(), null, pageable);

            assertEquals(2, resultado.getTotalElements());
            assertTrue(resultado.getContent().contains(prestamoActivo.getId()));
            assertTrue(resultado.getContent().contains(prestamoDevuelto.getId()));
        }

        @Test
        void filtroEstadoActivo_RetornaSoloActivos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = prestamoRepository.findIdsByFilters(null, EstadoPrestamo.ACTIVO, pageable);

            assertEquals(2, resultado.getTotalElements());
            assertFalse(resultado.getContent().contains(prestamoDevuelto.getId()));
        }

        @Test
        void filtroEstadoDevuelto_RetornaSoloDevueltos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = prestamoRepository.findIdsByFilters(null, EstadoPrestamo.DEVUELTO, pageable);

            assertEquals(1, resultado.getTotalElements());
            assertTrue(resultado.getContent().contains(prestamoDevuelto.getId()));
        }
    }

    @Nested
    class FindWithRelations {

        @Test
        void findAllByIdIn_ConIds_RetornaPrestamosConRelaciones() {
            List<Long> ids = List.of(prestamoActivo.getId(), prestamoDevuelto.getId());
            List<Prestamo> resultado = prestamoRepository.findAllByIdInWithRelations(ids);

            assertEquals(2, resultado.size());
            resultado.forEach(p -> assertNotNull(p.getUsuario()));
        }

        @Test
        void findById_IdValido_RetornaPrestamoConRelaciones() {
            Optional<Prestamo> resultado = prestamoRepository.findByIdWithRelations(prestamoActivo.getId());

            assertTrue(resultado.isPresent());
            assertNotNull(resultado.get().getUsuario());
        }

        @Test
        void findById_IdInexistente_RetornaVacio() {
            Optional<Prestamo> resultado = prestamoRepository.findByIdWithRelations(999L);

            assertFalse(resultado.isPresent());
        }
    }

    @Nested
    class ExistsChecks {

        @Test
        void existsByUsuarioIdAndEstado_Activo_RetornaTrue() {
            assertTrue(prestamoRepository.existsByUsuarioIdAndEstado(usuario1.getId(), EstadoPrestamo.ACTIVO));
        }

        @Test
        void existsByUsuarioIdAndEstado_Devuelto_RetornaFalseParaUsuario2() {
            assertFalse(prestamoRepository.existsByUsuarioIdAndEstado(usuario2.getId(), EstadoPrestamo.DEVUELTO));
        }

        @Test
        void existsByDetallesLibroIdAndEstado_LibroEnPrestamoActivo_RetornaTrue() {
            // libro1 está en prestamoActivo (ACTIVO)
            assertTrue(prestamoRepository.existsByDetallesLibroIdAndEstado(libro1.getId(), EstadoPrestamo.ACTIVO));
        }

        @Test
        void existsByDetallesLibroIdAndEstado_LibroSinPrestamoDevuelto_RetornaFalse() {
            // libro1 no tiene préstamos DEVUELTOS
            assertFalse(prestamoRepository.existsByDetallesLibroIdAndEstado(libro1.getId(), EstadoPrestamo.DEVUELTO));
        }
    }

    @Nested
    class Contadores {

        @Test
        void countByEstado_Activo_RetornaCantidadCorrecta() {
            assertEquals(2, prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO));
        }

        @Test
        void countByEstado_Devuelto_RetornaCantidadCorrecta() {
            assertEquals(1, prestamoRepository.countByEstado(EstadoPrestamo.DEVUELTO));
        }

        @Test
        void countPrestamosActivosVencidos_RetornaCantidadVencidos() {
            // prestamoVencido tiene fechaDevolucionLimite < ahora
            assertEquals(1, prestamoRepository.countPrestamosActivosVencidos(Instant.now()));
        }

        @Test
        void countPrestamosActivosPorVencer_RetornaPorVencerProximos10Dias() {
            Instant ahora = Instant.now();
            Instant limite = ahora.plus(10, ChronoUnit.DAYS);
            // prestamoActivo vence en 7 días → entra en el rango
            assertEquals(1, prestamoRepository.countPrestamosActivosPorVencer(ahora, limite));
        }

        @Test
        void countByEstadoAndFechaDevolucionRealBetween_RetornaPrestamosDevueltosEnRango() {
            Instant desde = Instant.now().minus(30, ChronoUnit.DAYS);
            Instant hasta = Instant.now();
            // prestamoDevuelto fue devuelto hace 21 días → entra en el rango
            assertEquals(1, prestamoRepository.countByEstadoAndFechaDevolucionRealBetween(
                EstadoPrestamo.DEVUELTO, desde, hasta
            ));
        }
    }

    @Nested
    class BusquedasEspeciales {

        @Test
        void tienePrestamosVencidos_UsuarioConPrestamoVencido_RetornaTrue() {
            assertTrue(prestamoRepository.tienePrestamosVencidos(usuario2.getId(), Instant.now()));
        }

        @Test
        void tienePrestamosVencidos_UsuarioSinPrestamoVencido_RetornaFalse() {
            assertFalse(prestamoRepository.tienePrestamosVencidos(usuario1.getId(), Instant.now()));
        }

        @Test
        void countTotalLibrosEnPoseccion_UsuarioConLibros_RetornaCantidad() {
            // usuario1 tiene prestamoActivo con libro1 x2
            assertEquals(2, prestamoRepository.countTotalLibrosEnPoseccion(usuario1.getId()));
        }

        @Test
        void countTotalLibrosEnPoseccion_UsuarioSinLibros_RetornaCero() {
            Usuario usuarioSinPrestamos = usuarioRepository.save(
                Usuario.builder().nombre("Sin Libros").email("sin@test.com").estado(true).build()
            );
            assertEquals(0, prestamoRepository.countTotalLibrosEnPoseccion(usuarioSinPrestamos.getId()));
        }

        @Test
        void tieneLibroPrestadoActivo_ConLibroPrestado_RetornaTrue() {
            assertTrue(prestamoRepository.tieneLibroPrestadoActivo(usuario1.getId(), libro1.getId()));
        }

        @Test
        void tieneLibroPrestadoActivo_SinLibroPrestado_RetornaFalse() {
            assertFalse(prestamoRepository.tieneLibroPrestadoActivo(usuario1.getId(), libro2.getId()));
        }

        @Test
        void findLibrosIdsPrestadosActivos_UsuarioConPrestamo_RetornaIds() {
            List<Long> ids = prestamoRepository.findLibrosIdsPrestadosActivos(usuario1.getId());
            assertTrue(ids.contains(libro1.getId()));
        }

        @Test
        void findLibrosIdsPrestadosActivos_UsuarioSinPrestamos_RetornaVacio() {
            Usuario usuarioSinPrestamos = usuarioRepository.save(
                Usuario.builder().nombre("Sin Prestamos").email("sp@test.com").estado(true).build()
            );
            assertTrue(prestamoRepository.findLibrosIdsPrestadosActivos(usuarioSinPrestamos.getId()).isEmpty());
        }

        @Test
        void findIdsPrestamosActivosVencidos_RetornaVencidos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = prestamoRepository.findIdsPrestamosActivosVencidos(Instant.now(), pageable);

            assertEquals(1, resultado.getTotalElements());
            assertTrue(resultado.getContent().contains(prestamoVencido.getId()));
        }

        @Test
        void findIdsPrestamosActivosPorVencer_RetornaPorVencerEnRango() {
            Instant ahora = Instant.now();
            Instant limite = ahora.plus(10, ChronoUnit.DAYS);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Long> resultado = prestamoRepository.findIdsPrestamosActivosPorVencer(ahora, limite, pageable);

            // prestamoActivo vence en 7 días → entra en el rango
            assertEquals(1, resultado.getTotalElements());
            assertTrue(resultado.getContent().contains(prestamoActivo.getId()));
        }
    }
}
