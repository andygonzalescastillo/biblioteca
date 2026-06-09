package com.biblioteca.backend.service.portal;

import com.biblioteca.backend.entity.Autor;
import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.entity.Libro;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.service.AutorService;
import com.biblioteca.backend.service.CategoriaService;
import com.biblioteca.backend.service.LibroService;
import com.biblioteca.backend.service.portal.impl.LectorCatalogoServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LectorCatalogoServiceImplTest {

    @Mock
    private LibroService libroService;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private AutorService autorService;

    @InjectMocks
    private LectorCatalogoServiceImpl lectorCatalogoService;

    @Nested
    class ObtenerCatalogo {

        @Test
        void obtenerCatalogo_SinFiltros_DebeRetornarLibrosActivos() {
            Libro libro = Libro.builder().id(1L).titulo("El Quijote").estado(true).build();
            Page<Libro> pagina = new PageImpl<>(List.of(libro));
            Pageable pageable = PageRequest.of(0, 10);

            when(libroService.obtenerTodos(isNull(), eq(true), isNull(), isNull(), eq(pageable)))
                    .thenReturn(pagina);

            Page<Libro> resultado = lectorCatalogoService.obtenerCatalogo(null, null, null, pageable);

            assertEquals(1, resultado.getTotalElements());
            assertEquals("El Quijote", resultado.getContent().getFirst().getTitulo());
            verify(libroService).obtenerTodos(null, true, null, null, pageable);
        }

        @Test
        void obtenerCatalogo_ConBusquedaConEspacios_DebeNormalizarTexto() {
            Libro libro = Libro.builder().id(1L).titulo("Java").estado(true).build();
            Page<Libro> pagina = new PageImpl<>(List.of(libro));
            Pageable pageable = PageRequest.of(0, 10);

            when(libroService.obtenerTodos(eq("Java"), eq(true), isNull(), isNull(), eq(pageable)))
                    .thenReturn(pagina);

            lectorCatalogoService.obtenerCatalogo("  Java  ", null, null, pageable);

            verify(libroService).obtenerTodos("Java", true, null, null, pageable);
        }

        @Test
        void obtenerCatalogo_ConBusquedaEnBlanco_DebeNormalizarANull() {
            Page<Libro> pagina = new PageImpl<>(List.of());
            Pageable pageable = PageRequest.of(0, 10);

            when(libroService.obtenerTodos(isNull(), eq(true), isNull(), isNull(), eq(pageable)))
                    .thenReturn(pagina);

            lectorCatalogoService.obtenerCatalogo("   ", null, null, pageable);

            verify(libroService).obtenerTodos(null, true, null, null, pageable);
        }

        @Test
        void obtenerCatalogo_ConFiltros_DebePassarFiltrosAlServicio() {
            Page<Libro> pagina = new PageImpl<>(List.of());
            Pageable pageable = PageRequest.of(0, 10);

            when(libroService.obtenerTodos(eq("ciencia"), eq(true), eq(2L), eq(3L), eq(pageable)))
                    .thenReturn(pagina);

            lectorCatalogoService.obtenerCatalogo("ciencia", 2L, 3L, pageable);

            verify(libroService).obtenerTodos("ciencia", true, 2L, 3L, pageable);
        }
    }

    @Nested
    class ObtenerLibroActivo {

        @Test
        void obtenerLibroActivo_CuandoEstadoActivo_DebeRetornarLibro() {
            Libro libro = Libro.builder().id(10L).titulo("Clean Code").estado(true).build();
            when(libroService.obtenerPorId(10L)).thenReturn(libro);

            Libro resultado = lectorCatalogoService.obtenerLibroActivo(10L);

            assertEquals("Clean Code", resultado.getTitulo());
            verify(libroService).obtenerPorId(10L);
        }

        @Test
        void obtenerLibroActivo_CuandoEstadoInactivo_DebeArrojarBookInactive() {
            Libro libro = Libro.builder().id(11L).titulo("Libro Inactivo").estado(false).build();
            when(libroService.obtenerPorId(11L)).thenReturn(libro);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> lectorCatalogoService.obtenerLibroActivo(11L));

            assertEquals("BOOK_INACTIVE", ex.errorCode());
            verify(libroService).obtenerPorId(11L);
        }

        @Test
        void obtenerLibroActivo_CuandoNoExiste_DebePropagarEntityNotFoundException() {
            when(libroService.obtenerPorId(99L))
                    .thenThrow(new EntityNotFoundException("Libro no encontrado"));

            assertThrows(EntityNotFoundException.class,
                    () -> lectorCatalogoService.obtenerLibroActivo(99L));
        }
    }

    @Nested
    class ObtenerCategoriasActivas {

        @Test
        void obtenerCategoriasActivas_DebeRetornarSoloActivas() {
            Categoria c1 = Categoria.builder().id(1L).nombre("Ficción").estado(true).build();
            Categoria c2 = Categoria.builder().id(2L).nombre("Ciencia").estado(true).build();
            Page<Categoria> pagina = new PageImpl<>(List.of(c1, c2));

            when(categoriaService.obtenerTodas(isNull(), eq(true), any(Pageable.class)))
                    .thenReturn(pagina);

            List<Categoria> resultado = lectorCatalogoService.obtenerCategoriasActivas();

            assertEquals(2, resultado.size());
            assertEquals("Ficción", resultado.getFirst().getNombre());
            verify(categoriaService).obtenerTodas(isNull(), eq(true), any(Pageable.class));
        }
    }

    @Nested
    class ObtenerAutoresActivos {

        @Test
        void obtenerAutoresActivos_DebeRetornarSoloActivos() {
            Autor a1 = Autor.builder().id(1L).nombre("Borges").estado(true).build();
            Page<Autor> pagina = new PageImpl<>(List.of(a1));

            when(autorService.obtenerTodos(isNull(), eq(true), any(Pageable.class)))
                    .thenReturn(pagina);

            List<Autor> resultado = lectorCatalogoService.obtenerAutoresActivos();

            assertEquals(1, resultado.size());
            assertEquals("Borges", resultado.getFirst().getNombre());
            verify(autorService).obtenerTodos(isNull(), eq(true), any(Pageable.class));
        }
    }
}
