package com.biblioteca.backend.service;

import com.biblioteca.backend.entity.*;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.*;
import com.biblioteca.backend.service.impl.LibroServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroServiceImplTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ImagenRepository imagenRepository;

    @Mock
    private AutorRepository autorRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private LibroServiceImpl libroService;

    private Libro libroValido;
    private Categoria categoriaValida;
    private Imagen portadaValida;
    private Autor autorValido;

    @BeforeEach
    void setUp() {
        categoriaValida = Categoria.builder()
                .id(1L)
                .nombre("Novela")
                .estado(true)
                .build();

        portadaValida = Imagen.builder()
                .id(UUID.randomUUID())
                .nombreArchivo("portada.jpg")
                .urlAlmacenamiento("http://example.com/portada.jpg")
                .build();

        autorValido = Autor.builder()
                .id(1L)
                .nombre("Gabriel García Márquez")
                .build();

        libroValido = Libro.builder()
                .id(1L)
                .titulo("Cien años de soledad")
                .isbn("978-3-16-148410-0")
                .estado(true)
                .stock(10)
                .categoria(categoriaValida)
                .portada(portadaValida)
                .autores(new HashSet<>(Set.of(autorValido)))
                .build();
    }

    @Nested
    class ObtenerTodos {

        @Test
        void obtenerTodos_Exitoso() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> idsPage = new PageImpl<>(List.of(1L));
            when(libroRepository.findIdsByFilters(any(), any(), any(), any(), eq(pageable))).thenReturn(idsPage);
            when(libroRepository.findAllByIdInWithRelations(List.of(1L))).thenReturn(List.of(libroValido));

            Page<Libro> resultado = libroService.obtenerTodos("Cien", true, 1L, 1L, pageable);

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals("Cien años de soledad", resultado.getContent().getFirst().getTitulo());
        }

        @Test
        void obtenerTodos_Vacio() {
            Pageable pageable = PageRequest.of(0, 10);
            when(libroRepository.findIdsByFilters(any(), any(), any(), any(), eq(pageable))).thenReturn(Page.empty(pageable));

            Page<Libro> resultado = libroService.obtenerTodos("Cien", true, 1L, 1L, pageable);

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            verify(libroRepository, never()).findAllByIdInWithRelations(any());
        }
    }

    @Nested
    class ObtenerPorId {

        @Test
        void obtenerPorId_Exitoso() {
            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(libroValido));

            Libro resultado = libroService.obtenerPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
        }

        @Test
        void obtenerPorId_NoEncontrado_LanzaException() {
            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> libroService.obtenerPorId(1L));
        }
    }

    @Nested
    class ObtenerPorIsbn {

        @Test
        void obtenerPorIsbn_Exitoso() {
            when(libroRepository.findByIsbnWithRelations("978-3-16-148410-0")).thenReturn(Optional.of(libroValido));

            Libro resultado = libroService.obtenerPorIsbn("978-3-16-148410-0");

            assertNotNull(resultado);
            assertEquals("978-3-16-148410-0", resultado.getIsbn());
        }

        @Test
        void obtenerPorIsbn_NoEncontrado_LanzaException() {
            when(libroRepository.findByIsbnWithRelations("978-3-16-148410-0")).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> libroService.obtenerPorIsbn("978-3-16-148410-0"));
        }
    }

    @Nested
    class CrearLibro {

        @Test
        void crearLibro_Exitoso() {
            Libro nuevoLibro = Libro.builder()
                    .titulo("Crónica de una muerte anunciada")
                    .isbn("978-0-307-35041-3")
                    .stock(5)
                    .build();

            when(libroRepository.existsByIsbn(nuevoLibro.getIsbn())).thenReturn(false);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(imagenRepository.findById(portadaValida.getId())).thenReturn(Optional.of(portadaValida));
            when(autorRepository.findAllById(Set.of(1L))).thenReturn(List.of(autorValido));
            when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Libro resultado = libroService.crearLibro(nuevoLibro, 1L, portadaValida.getId(), Set.of(1L));

            assertNotNull(resultado);
            assertEquals("Crónica de una muerte anunciada", resultado.getTitulo());
            assertEquals(categoriaValida, resultado.getCategoria());
            assertEquals(portadaValida, resultado.getPortada());
            assertTrue(resultado.getAutores().contains(autorValido));
            verify(libroRepository).save(nuevoLibro);
        }

        @Test
        void crearLibro_ConPortadaNulaYAutoresVacios_Exitoso() {
            Libro nuevoLibro = Libro.builder()
                    .titulo("Crónica")
                    .isbn("978-0-307-35041-3")
                    .stock(5)
                    .build();

            when(libroRepository.existsByIsbn(nuevoLibro.getIsbn())).thenReturn(false);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Libro resultado = libroService.crearLibro(nuevoLibro, 1L, null, Collections.emptySet());

            assertNotNull(resultado);
            assertNull(resultado.getPortada());
            assertTrue(resultado.getAutores().isEmpty());
            verify(imagenRepository, never()).findById(any());
            verify(autorRepository, never()).findAllById(any());
        }

        @Test
        void crearLibro_Error_IsbnDuplicado_LanzaBusinessException() {
            when(libroRepository.existsByIsbn(libroValido.getIsbn())).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    libroService.crearLibro(libroValido, 1L, portadaValida.getId(), Set.of(1L)));
            assertEquals("ISBN_ALREADY_EXISTS", ex.errorCode());
            verify(libroRepository, never()).save(any(Libro.class));
        }

        @Test
        void crearLibro_Error_CategoriaNoEncontrada_LanzaException() {
            Libro nuevoLibro = Libro.builder().isbn("1234").build();
            when(libroRepository.existsByIsbn("1234")).thenReturn(false);
            when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () ->
                    libroService.crearLibro(nuevoLibro, 99L, null, null));
        }

        @Test
        void crearLibro_Error_PortadaNoEncontrada_LanzaException() {
            Libro nuevoLibro = Libro.builder().isbn("1234").build();
            UUID portadaIdInexistente = UUID.randomUUID();
            when(libroRepository.existsByIsbn("1234")).thenReturn(false);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(imagenRepository.findById(portadaIdInexistente)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () ->
                    libroService.crearLibro(nuevoLibro, 1L, portadaIdInexistente, null));
        }

        @Test
        void crearLibro_Error_AutoresNoEncontrados_LanzaException() {
            Libro nuevoLibro = Libro.builder().isbn("1234").build();
            when(libroRepository.existsByIsbn("1234")).thenReturn(false);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(autorRepository.findAllById(Set.of(99L))).thenReturn(Collections.emptyList());

            assertThrows(EntityNotFoundException.class, () ->
                    libroService.crearLibro(nuevoLibro, 1L, null, Set.of(99L)));
        }
    }

    @Nested
    class Actualizar {

        @Test
        void actualizar_Exitoso() {
            Libro detallesActualizados = Libro.builder()
                    .titulo("Cien años de soledad - Edición Especial")
                    .isbn("978-3-16-148410-0")
                    .stock(12)
                    .build();

            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(libroValido));
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(imagenRepository.findById(portadaValida.getId())).thenReturn(Optional.of(portadaValida));
            when(autorRepository.findAllById(Set.of(1L))).thenReturn(List.of(autorValido));
            when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Libro resultado = libroService.actualizar(1L, detallesActualizados, 1L, portadaValida.getId(), Set.of(1L), false);

            assertNotNull(resultado);
            assertEquals("Cien años de soledad - Edición Especial", resultado.getTitulo());
            assertEquals(12, resultado.getStock());
            assertFalse(resultado.isEstado());
            verify(libroRepository).save(libroValido);
        }

        @Test
        void actualizar_MismoIsbn_NoValidaDuplicado_Exitoso() {
            Libro detallesMismoIsbn = Libro.builder()
                    .titulo("Cien años de soledad - Edición Especial")
                    .isbn("978-3-16-148410-0") // Mismo ISBN
                    .stock(12)
                    .build();

            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(libroValido));
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Libro resultado = libroService.actualizar(1L, detallesMismoIsbn, 1L, null, null, null);

            assertNotNull(resultado);
            assertTrue(resultado.isEstado()); // No cambia estado ya que se pasó null
            verify(libroRepository, never()).existsByIsbn(anyString());
            verify(libroRepository).save(libroValido);
        }

        @Test
        void actualizar_Error_IsbnDuplicado_LanzaBusinessException() {
            Libro detallesActualizados = Libro.builder()
                    .titulo("Cien años de soledad")
                    .isbn("978-0-307-35041-3")
                    .stock(10)
                    .build();

            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(libroValido));
            when(libroRepository.existsByIsbn("978-0-307-35041-3")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    libroService.actualizar(1L, detallesActualizados, 1L, portadaValida.getId(), Set.of(1L), true));
            assertEquals("ISBN_ALREADY_EXISTS", ex.errorCode());
            verify(libroRepository, never()).save(any(Libro.class));
        }
    }

    @Nested
    class Eliminar {

        @Test
        void eliminar_SinPrestamosActivos_Exitoso() {
            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(libroValido));
            when(prestamoRepository.existsByDetallesLibroIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(false);
            when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

            libroService.eliminar(1L);

            assertFalse(libroValido.isEstado());
            verify(libroRepository).save(libroValido);
        }

        @Test
        void eliminar_ConPrestamosActivos_LanzaBusinessException() {
            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(libroValido));
            when(prestamoRepository.existsByDetallesLibroIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> libroService.eliminar(1L));
            assertEquals("BOOK_HAS_ACTIVE_LOANS", ex.errorCode());
            verify(libroRepository, never()).save(any(Libro.class));
        }
    }

    @Nested
    class Activar {

        @Test
        void activar_Exitoso() {
            libroValido.setEstado(false);
            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(libroValido));
            when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Libro resultado = libroService.activar(1L);

            assertTrue(resultado.isEstado());
            verify(libroRepository).save(libroValido);
        }
    }

    @Nested
    class ActualizarStock {

        @Test
        void actualizarStock_Exitoso_Incremento() {
            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(libroValido));
            when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Libro resultado = libroService.actualizarStock(1L, 5);

            assertEquals(15, resultado.getStock());
            verify(libroRepository).save(libroValido);
        }

        @Test
        void actualizarStock_Exitoso_Decremento() {
            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(libroValido));
            when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Libro resultado = libroService.actualizarStock(1L, -5);

            assertEquals(5, resultado.getStock());
            verify(libroRepository).save(libroValido);
        }

        @Test
        void actualizarStock_Error_StockInsuficiente_LanzaBusinessException() {
            when(libroRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(libroValido));

            BusinessException ex = assertThrows(BusinessException.class, () -> libroService.actualizarStock(1L, -15));
            assertEquals("INSUFFICIENT_STOCK", ex.errorCode());
            verify(libroRepository, never()).save(any(Libro.class));
        }
    }
}
