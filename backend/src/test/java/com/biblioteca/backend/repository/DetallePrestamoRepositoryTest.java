package com.biblioteca.backend.repository;

import com.biblioteca.backend.dto.response.DashboardRankingResponse;
import com.biblioteca.backend.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DetallePrestamoRepositoryTest {

    @Autowired private DetallePrestamoRepository detallePrestamoRepository;
    @Autowired private PrestamoRepository prestamoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LibroRepository libroRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    private Libro libroA;
    private Libro libroB;
    private Libro libroC;
    private Usuario usuario1;
    private Usuario usuario2;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.save(
            Categoria.builder().nombre("General").estado(true).build()
        );

        libroA = libroRepository.save(
            Libro.builder().titulo("El Quijote").isbn("978-1111111111")
                .stock(10).estado(true).categoria(categoria).build()
        );
        libroB = libroRepository.save(
            Libro.builder().titulo("Cien años de soledad").isbn("978-2222222222")
                .stock(10).estado(true).categoria(categoria).build()
        );
        libroC = libroRepository.save(
            Libro.builder().titulo("Ficciones").isbn("978-3333333333")
                .stock(10).estado(true).categoria(categoria).build()
        );

        usuario1 = usuarioRepository.save(
            Usuario.builder().nombre("Ana López").email("ana@test.com").estado(true).build()
        );
        usuario2 = usuarioRepository.save(
            Usuario.builder().nombre("Carlos Díaz").email("carlos@test.com").estado(true).build()
        );

        Instant ahora = Instant.now();

        // usuario1: libroA(3) + libroB(2) = 5 en total
        Prestamo prestamo1 = prestamoRepository.save(
            Prestamo.builder()
                .usuario(usuario1)
                .fechaPrestamo(ahora)
                .fechaDevolucionLimite(ahora.plus(7, ChronoUnit.DAYS))
                .estado(EstadoPrestamo.ACTIVO)
                .build()
        );
        detallePrestamoRepository.save(
            DetallePrestamo.builder().prestamo(prestamo1).libro(libroA).cantidad(3).build()
        );
        detallePrestamoRepository.save(
            DetallePrestamo.builder().prestamo(prestamo1).libro(libroB).cantidad(2).build()
        );

        // usuario2: libroA(1) + libroC(4) = 5 en total
        Prestamo prestamo2 = prestamoRepository.save(
            Prestamo.builder()
                .usuario(usuario2)
                .fechaPrestamo(ahora)
                .fechaDevolucionLimite(ahora.plus(7, ChronoUnit.DAYS))
                .estado(EstadoPrestamo.ACTIVO)
                .build()
        );
        detallePrestamoRepository.save(
            DetallePrestamo.builder().prestamo(prestamo2).libro(libroA).cantidad(1).build()
        );
        detallePrestamoRepository.save(
            DetallePrestamo.builder().prestamo(prestamo2).libro(libroC).cantidad(4).build()
        );
        // Totales por libro: libroA=4, libroC=4, libroB=2
    }

    @Nested
    class FindLibrosMasPrestados {

        @Test
        void retornaOrdenadoPorCantidadDesc() {
            Pageable pageable = PageRequest.of(0, 10);
            List<DashboardRankingResponse> resultado = detallePrestamoRepository.findLibrosMasPrestados(pageable);

            assertFalse(resultado.isEmpty());
            assertEquals(3, resultado.size());
            assertTrue(resultado.getFirst().total() >= resultado.getLast().total());
        }

        @Test
        void conLimite_RetornaSoloLosTop() {
            Pageable pageable = PageRequest.of(0, 2);
            List<DashboardRankingResponse> resultado = detallePrestamoRepository.findLibrosMasPrestados(pageable);

            assertEquals(2, resultado.size());
        }

        @Test
        void contenidoCorrecto_LibroATiene4() {
            Pageable pageable = PageRequest.of(0, 10);
            List<DashboardRankingResponse> resultado = detallePrestamoRepository.findLibrosMasPrestados(pageable);

            DashboardRankingResponse libroAResult = resultado.stream()
                .filter(r -> r.id().equals(libroA.getId()))
                .findFirst()
                .orElse(null);

            assertNotNull(libroAResult);
            assertEquals("El Quijote", libroAResult.nombre());
            assertEquals(4L, libroAResult.total());
        }

        @Test
        void libroMenosPrestado_EsLibroB() {
            Pageable pageable = PageRequest.of(0, 10);
            List<DashboardRankingResponse> resultado = detallePrestamoRepository.findLibrosMasPrestados(pageable);

            DashboardRankingResponse ultimo = resultado.getLast();
            assertEquals(libroB.getId(), ultimo.id());
            assertEquals(2L, ultimo.total());
        }

        @Test
        void sinDetalles_RetornaVacio() {
            detallePrestamoRepository.deleteAll();
            Pageable pageable = PageRequest.of(0, 10);
            List<DashboardRankingResponse> resultado = detallePrestamoRepository.findLibrosMasPrestados(pageable);

            assertTrue(resultado.isEmpty());
        }
    }

    @Nested
    class FindLectoresMasActivos {

        @Test
        void retornaOrdenadoPorCantidadDesc() {
            Pageable pageable = PageRequest.of(0, 10);
            List<DashboardRankingResponse> resultado = detallePrestamoRepository.findLectoresMasActivos(pageable);

            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());
        }

        @Test
        void contenidoCorrecto_Usuario1Tiene5() {
            Pageable pageable = PageRequest.of(0, 10);
            List<DashboardRankingResponse> resultado = detallePrestamoRepository.findLectoresMasActivos(pageable);

            DashboardRankingResponse usuario1Result = resultado.stream()
                .filter(r -> r.id().equals(usuario1.getId()))
                .findFirst()
                .orElse(null);

            assertNotNull(usuario1Result);
            assertEquals("Ana López", usuario1Result.nombre());
            assertEquals(5L, usuario1Result.total());
        }

        @Test
        void contenidoCorrecto_Usuario2Tiene5() {
            Pageable pageable = PageRequest.of(0, 10);
            List<DashboardRankingResponse> resultado = detallePrestamoRepository.findLectoresMasActivos(pageable);

            DashboardRankingResponse usuario2Result = resultado.stream()
                .filter(r -> r.id().equals(usuario2.getId()))
                .findFirst()
                .orElse(null);

            assertNotNull(usuario2Result);
            assertEquals("Carlos Díaz", usuario2Result.nombre());
            assertEquals(5L, usuario2Result.total());
        }

        @Test
        void conLimite_RetornaSoloTop1() {
            Pageable pageable = PageRequest.of(0, 1);
            List<DashboardRankingResponse> resultado = detallePrestamoRepository.findLectoresMasActivos(pageable);

            assertEquals(1, resultado.size());
        }

        @Test
        void sinDetalles_RetornaVacio() {
            detallePrestamoRepository.deleteAll();
            Pageable pageable = PageRequest.of(0, 10);
            List<DashboardRankingResponse> resultado = detallePrestamoRepository.findLectoresMasActivos(pageable);

            assertTrue(resultado.isEmpty());
        }
    }
}
