package com.biblioteca.backend.service;

import com.biblioteca.backend.entity.Categoria;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.CategoriaRepository;
import com.biblioteca.backend.repository.LibroRepository;
import com.biblioteca.backend.service.impl.CategoriaServiceImpl;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoriaValida;

    @BeforeEach
    void setUp() {
        categoriaValida = Categoria.builder()
                .id(1L)
                .nombre("Drama")
                .descripcion("Libros dramáticos")
                .estado(true)
                .build();
    }

    @Nested
    class ObtenerTodas {

        @Test
        void obtenerTodas_Exitoso() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Categoria> page = new PageImpl<>(List.of(categoriaValida));
            when(categoriaRepository.findByFilters("Drama", true, pageable)).thenReturn(page);

            Page<Categoria> resultado = categoriaService.obtenerTodas("Drama", true, pageable);

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals("Drama", resultado.getContent().getFirst().getNombre());
        }
    }

    @Nested
    class ObtenerPorId {

        @Test
        void obtenerPorId_Exitoso() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));

            Categoria resultado = categoriaService.obtenerPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
        }

        @Test
        void obtenerPorId_NoEncontrado_LanzaException() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> categoriaService.obtenerPorId(1L));
        }
    }

    @Nested
    class CrearCategoria {

        @Test
        void crearCategoria_Exitoso() {
            Categoria nueva = Categoria.builder()
                    .nombre("Ciencia Ficción")
                    .descripcion("Futuristas")
                    .build();

            when(categoriaRepository.existsByNombre("Ciencia Ficción")).thenReturn(false);
            when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Categoria resultado = categoriaService.crearCategoria(nueva);

            assertNotNull(resultado);
            assertEquals("Ciencia Ficción", resultado.getNombre());
            verify(categoriaRepository).save(nueva);
        }

        @Test
        void crearCategoria_Error_NombreDuplicado_LanzaBusinessException() {
            when(categoriaRepository.existsByNombre("Drama")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    categoriaService.crearCategoria(categoriaValida));
            assertEquals("CATEGORY_ALREADY_EXISTS", ex.errorCode());
            verify(categoriaRepository, never()).save(any(Categoria.class));
        }
    }

    @Nested
    class Actualizar {

        @Test
        void actualizar_ConNuevoNombreExitoso() {
            Categoria detallesActualizados = Categoria.builder()
                    .nombre("Drama Actualizado")
                    .descripcion("Descripción nueva")
                    .build();

            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(categoriaRepository.existsByNombre("Drama Actualizado")).thenReturn(false);
            when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Categoria resultado = categoriaService.actualizar(1L, detallesActualizados, true);

            assertNotNull(resultado);
            assertEquals("Drama Actualizado", resultado.getNombre());
            assertEquals("Descripción nueva", resultado.getDescripcion());
            assertTrue(resultado.isEstado());
            verify(categoriaRepository).save(categoriaValida);
        }

        @Test
        void actualizar_MismoNombre_NoValidaDuplicado_Exitoso() {
            Categoria detallesMismoNombre = Categoria.builder()
                    .nombre("Drama") // Mismo nombre
                    .descripcion("Nueva descripción")
                    .build();

            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Categoria resultado = categoriaService.actualizar(1L, detallesMismoNombre, null);

            assertNotNull(resultado);
            assertEquals("Drama", resultado.getNombre());
            assertEquals("Nueva descripción", resultado.getDescripcion());
            assertTrue(resultado.isEstado()); // No cambia estado ya que se pasó null
            verify(categoriaRepository, never()).existsByNombre(anyString());
            verify(categoriaRepository).save(categoriaValida);
        }

        @Test
        void actualizar_Error_NombreDuplicado_LanzaBusinessException() {
            Categoria detallesActualizados = Categoria.builder()
                    .nombre("Terror")
                    .build();

            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(categoriaRepository.existsByNombre("Terror")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    categoriaService.actualizar(1L, detallesActualizados, true));
            assertEquals("CATEGORY_ALREADY_EXISTS", ex.errorCode());
            verify(categoriaRepository, never()).save(any(Categoria.class));
        }
    }

    @Nested
    class Eliminar {

        @Test
        void eliminar_SinLibrosActivos_Exitoso() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(libroRepository.existsByCategoriaIdAndEstadoTrue(1L)).thenReturn(false);
            when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

            categoriaService.eliminar(1L);

            assertFalse(categoriaValida.isEstado());
            verify(categoriaRepository).save(categoriaValida);
        }

        @Test
        void eliminar_ConLibrosActivos_LanzaBusinessException() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(libroRepository.existsByCategoriaIdAndEstadoTrue(1L)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> categoriaService.eliminar(1L));
            assertEquals("CATEGORY_HAS_ACTIVE_BOOKS", ex.errorCode());
            verify(categoriaRepository, never()).save(any(Categoria.class));
        }
    }

    @Nested
    class Activar {

        @Test
        void activar_Exitoso() {
            categoriaValida.setEstado(false);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaValida));
            when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Categoria resultado = categoriaService.activar(1L);

            assertTrue(resultado.isEstado());
            verify(categoriaRepository).save(categoriaValida);
        }
    }
}
