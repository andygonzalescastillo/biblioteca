package com.biblioteca.backend.repository;

import com.biblioteca.backend.dto.response.DashboardLibroInventarioResponse;
import com.biblioteca.backend.dto.response.DashboardRankingResponse;
import com.biblioteca.backend.entity.Autor;
import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.entity.Libro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class LibroRepositoryTest {

    @Autowired private LibroRepository libroRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private AutorRepository autorRepository;

    private Categoria categoriaFiccion;
    private Categoria categoriaCiencia;
    private Autor autorGarcia;
    private Autor autorBorges;
    private Libro libro1;
    private Libro libro2;
    private Libro libro3;

    @BeforeEach
    void setUp() {
        categoriaFiccion = categoriaRepository.save(
            Categoria.builder().nombre("Ficción").descripcion("Literatura de ficción").estado(true).build()
        );
        categoriaCiencia = categoriaRepository.save(
            Categoria.builder().nombre("Ciencia").descripcion("Libros de ciencia").estado(true).build()
        );
        autorGarcia = autorRepository.save(
            Autor.builder().nombre("Gabriel García Márquez").estado(true).build()
        );
        autorBorges = autorRepository.save(
            Autor.builder().nombre("Jorge Luis Borges").estado(true).build()
        );

        // libro1: activo, ficción, stock=5, autor=García
        libro1 = libroRepository.save(
            Libro.builder()
                .titulo("Cien años de soledad")
                .isbn("978-0307474728")
                .stock(5)
                .estado(true)
                .categoria(categoriaFiccion)
                .autores(Set.of(autorGarcia))
                .build()
        );
        // libro2: activo, ficción, stock=3, autor=Borges
        libro2 = libroRepository.save(
            Libro.builder()
                .titulo("Ficciones")
                .isbn("978-0802130303")
                .stock(3)
                .estado(true)
                .categoria(categoriaFiccion)
                .autores(Set.of(autorBorges))
                .build()
        );
        // libro3: inactivo, ciencia, stock=2, sin autor
        libro3 = libroRepository.save(
            Libro.builder()
                .titulo("El origen de las especies")
                .isbn("978-0140432053")
                .stock(2)
                .estado(false)
                .categoria(categoriaCiencia)
                .build()
        );
    }

    @Nested
    class FindIdsByFilters {

        @Test
        void sinFiltros_RetornaTodos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = libroRepository.findIdsByFilters(null, null, null, null, pageable);
            assertEquals(3, resultado.getTotalElements());
        }

        @Test
        void buscarPorTitulo_RetornaCoincidencias() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = libroRepository.findIdsByFilters("Cien años", null, null, null, pageable);
            assertEquals(1, resultado.getTotalElements());
            assertTrue(resultado.getContent().contains(libro1.getId()));
        }

        @Test
        void buscarPorIsbn_RetornaCoincidencias() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = libroRepository.findIdsByFilters("978-0802130303", null, null, null, pageable);
            assertEquals(1, resultado.getTotalElements());
            assertTrue(resultado.getContent().contains(libro2.getId()));
        }

        @Test
        void filtroEstadoActivo_RetornaSoloActivos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = libroRepository.findIdsByFilters(null, true, null, null, pageable);
            assertEquals(2, resultado.getTotalElements());
            assertFalse(resultado.getContent().contains(libro3.getId()));
        }

        @Test
        void filtroCategoria_RetornaLibrosDeCategoria() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = libroRepository.findIdsByFilters(null, null, categoriaFiccion.getId(), null, pageable);
            assertEquals(2, resultado.getTotalElements());
            assertFalse(resultado.getContent().contains(libro3.getId()));
        }

        @Test
        void filtroAutor_RetornaLibrosDelAutor() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> resultado = libroRepository.findIdsByFilters(null, null, null, autorGarcia.getId(), pageable);
            assertEquals(1, resultado.getTotalElements());
            assertTrue(resultado.getContent().contains(libro1.getId()));
        }
    }

    @Nested
    class FindWithRelations {

        @Test
        void findAllByIdIn_ConIdsValidos_RetornaLibrosConRelaciones() {
            List<Long> ids = List.of(libro1.getId(), libro2.getId());
            List<Libro> resultado = libroRepository.findAllByIdInWithRelations(ids);
            assertEquals(2, resultado.size());
            resultado.forEach(libro -> assertNotNull(libro.getCategoria()));
        }

        @Test
        void findById_ConIdValido_RetornaLibroConRelaciones() {
            Optional<Libro> resultado = libroRepository.findByIdWithRelations(libro1.getId());
            assertTrue(resultado.isPresent());
            assertEquals("Cien años de soledad", resultado.get().getTitulo());
            assertNotNull(resultado.get().getCategoria());
        }

        @Test
        void findById_ConIdInexistente_RetornaVacio() {
            Optional<Libro> resultado = libroRepository.findByIdWithRelations(999L);
            assertFalse(resultado.isPresent());
        }

        @Test
        void findByIsbn_ConIsbnValido_RetornaLibro() {
            Optional<Libro> resultado = libroRepository.findByIsbnWithRelations("978-0307474728");
            assertTrue(resultado.isPresent());
            assertEquals("Cien años de soledad", resultado.get().getTitulo());
        }
    }

    @Nested
    class ExistsChecks {

        @Test
        void existsByIsbn_IsbnExistente_RetornaTrue() {
            assertTrue(libroRepository.existsByIsbn("978-0307474728"));
        }

        @Test
        void existsByIsbn_IsbnInexistente_RetornaFalse() {
            assertFalse(libroRepository.existsByIsbn("000-0000000000"));
        }

        @Test
        void existsByCategoriaId_ConLibrosAsociados_RetornaTrue() {
            assertTrue(libroRepository.existsByCategoriaId(categoriaFiccion.getId()));
        }

        @Test
        void existsByAutoresId_ConLibrosAsociados_RetornaTrue() {
            assertTrue(libroRepository.existsByAutoresId(autorGarcia.getId()));
        }

        @Test
        void existsByCategoriaIdAndEstadoTrue_ConLibrosActivos_RetornaTrue() {
            assertTrue(libroRepository.existsByCategoriaIdAndEstadoTrue(categoriaFiccion.getId()));
        }

        @Test
        void existsByCategoriaIdAndEstadoTrue_SinLibrosActivos_RetornaFalse() {
            // categoriaCiencia solo tiene libro3 que está inactivo
            assertFalse(libroRepository.existsByCategoriaIdAndEstadoTrue(categoriaCiencia.getId()));
        }

        @Test
        void existsByAutoresIdAndEstadoTrue_ConLibroActivo_RetornaTrue() {
            // autorGarcia está en libro1 que es activo
            assertTrue(libroRepository.existsByAutoresIdAndEstadoTrue(autorGarcia.getId()));
        }

        @Test
        void existsByAutoresIdAndEstadoTrue_SinLibrosActivos_RetornaFalse() {
            Autor autorSinLibros = autorRepository.save(
                Autor.builder().nombre("Autor Sin Libros").estado(true).build()
            );
            assertFalse(libroRepository.existsByAutoresIdAndEstadoTrue(autorSinLibros.getId()));
        }
    }

    @Nested
    class Contadores {

        @Test
        void sumStockActivo_RetornaSumaDeSoloActivos() {
            // libro1=5, libro2=3 activos; libro3=2 inactivo no cuenta
            assertEquals(8, libroRepository.sumStockActivo());
        }

        @Test
        void countByEstadoTrue_RetornaCantidadActivos() {
            assertEquals(2, libroRepository.countByEstadoTrue());
        }

        @Test
        void countByEstadoTrueAndStock_RetornaCantidadConStockExacto() {
            assertEquals(1, libroRepository.countByEstadoTrueAndStock(5)); // libro1
            assertEquals(1, libroRepository.countByEstadoTrueAndStock(3)); // libro2
            assertEquals(0, libroRepository.countByEstadoTrueAndStock(2)); // libro3 inactivo no cuenta
        }
    }

    @Nested
    class AlertasYRankings {

        @Test
        void findAlertasInventario_StockMaximo5_RetornaActivosBajoUmbral() {
            Pageable pageable = PageRequest.of(0, 10);
            List<DashboardLibroInventarioResponse> resultado = libroRepository.findAlertasInventario(5, pageable);
            // libro1(stock=5) y libro2(stock=3) son activos con stock<=5; libro3 inactivo excluido
            assertEquals(2, resultado.size());
            assertEquals("Ficciones", resultado.getFirst().titulo()); // stock=3, menor -> primero por ASC
        }

        @Test
        void findAlertasInventario_StockMaximo2_RetornaVacio() {
            Pageable pageable = PageRequest.of(0, 10);
            List<DashboardLibroInventarioResponse> resultado = libroRepository.findAlertasInventario(2, pageable);
            // libro3 tiene stock=2 pero es inactivo; libro1 y libro2 tienen stock mayor a 2
            assertTrue(resultado.isEmpty());
        }

        @Test
        void findCategoriasConMasLibros_RetornaCategoriasConLibrosActivos() {
            Pageable pageable = PageRequest.of(0, 5);
            List<DashboardRankingResponse> resultado = libroRepository.findCategoriasConMasLibros(pageable);
            // Solo categoriaFiccion tiene libros activos (libro1 y libro2); libro3 inactivo excluido
            assertEquals(1, resultado.size());
            assertEquals("Ficción", resultado.getFirst().nombre());
            assertEquals(2L, resultado.getFirst().total());
        }

        @Test
        void findAutoresConMasLibros_RetornaAutoresConLibrosActivos() {
            Pageable pageable = PageRequest.of(0, 5);
            List<DashboardRankingResponse> resultado = libroRepository.findAutoresConMasLibros(pageable);
            // autorGarcia(libro1) y autorBorges(libro2), ambos activos
            assertEquals(2, resultado.size());
        }
    }
}
