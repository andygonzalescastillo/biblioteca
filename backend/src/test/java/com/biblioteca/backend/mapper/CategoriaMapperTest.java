package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.request.CategoriaRequest;
import com.biblioteca.backend.dto.response.CategoriaResponse;
import com.biblioteca.backend.entity.Categoria;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CategoriaMapperImpl.class)
class CategoriaMapperTest {

    @Autowired
    private CategoriaMapper categoriaMapper;

    @Nested
    class ToResponse {

        @Test
        void conTodosLosCampos_MapearCorrectamente() {
            Categoria categoria = Categoria.builder()
                .id(1L)
                .nombre("Ficción")
                .descripcion("Literatura de ficción")
                .estado(true)
                .build();

            CategoriaResponse response = categoriaMapper.toResponse(categoria);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("Ficción", response.nombre());
            assertEquals("Literatura de ficción", response.descripcion());
            assertTrue(response.estado());
        }

        @Test
        void conDescripcionNula_MapearCamposRestantes() {
            Categoria categoria = Categoria.builder()
                .id(2L)
                .nombre("Ciencia")
                .estado(false)
                .build();

            CategoriaResponse response = categoriaMapper.toResponse(categoria);

            assertNotNull(response);
            assertEquals(2L, response.id());
            assertNull(response.descripcion());
            assertFalse(response.estado());
        }

        @Test
        void conEntidadNula_RetornaNull() {
            assertNull(categoriaMapper.toResponse(null));
        }
    }

    @Nested
    class ToEntity {

        @Test
        void conTodosLosCampos_MapearCorrectamente() {
            CategoriaRequest request = new CategoriaRequest("Drama", "Libros dramáticos", false);

            Categoria entity = categoriaMapper.toEntity(request);

            assertNotNull(entity);
            assertNull(entity.getId());                     // ignorado por @Mapping
            assertTrue(entity.getLibros().isEmpty());       // ignorado: @Builder.Default → lista vacía
            assertEquals("Drama", entity.getNombre());
            assertEquals("Libros dramáticos", entity.getDescripcion());
            assertFalse(entity.isEstado());
        }

        @Test
        void sinEstado_EstadoDefaultTrue() {
            CategoriaRequest request = new CategoriaRequest("Historia", null, null);

            Categoria entity = categoriaMapper.toEntity(request);

            assertTrue(entity.isEstado());      // defaultValue = "true"
        }

        @Test
        void conEstadoExplicitoFalse_EstadoPermaneceFalse() {
            CategoriaRequest request = new CategoriaRequest("Inactiva", null, false);

            Categoria entity = categoriaMapper.toEntity(request);

            assertFalse(entity.isEstado());
        }
    }
}
