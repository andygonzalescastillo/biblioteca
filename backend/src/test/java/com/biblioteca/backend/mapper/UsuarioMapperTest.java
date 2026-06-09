package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.request.UsuarioRequest;
import com.biblioteca.backend.dto.response.UsuarioResponse;
import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.entity.Usuario;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UsuarioMapperImpl.class, ImagenMapperImpl.class})
class UsuarioMapperTest {

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Nested
    class ToResponse {

        @Test
        void conTodosLosCampos_MapearCorrectamente() {
            Instant fechaRegistro = Instant.now();
            Imagen foto = Imagen.builder()
                .id(UUID.randomUUID())
                .nombreArchivo("avatar.png")
                .urlAlmacenamiento("/uploads/avatar.png")
                .fechaCreacion(Instant.now())
                .build();
            Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("Ana García")
                .email("ana@example.com")
                .telefono("555-1234")
                .direccion("Calle Falsa 123")
                .fechaRegistro(fechaRegistro)
                .foto(foto)
                .estado(true)
                .build();

            UsuarioResponse response = usuarioMapper.toResponse(usuario);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("Ana García", response.nombre());
            assertEquals("ana@example.com", response.email());
            assertEquals("555-1234", response.telefono());
            assertEquals("Calle Falsa 123", response.direccion());
            assertEquals(fechaRegistro, response.fechaRegistro());
            assertTrue(response.estado());
            assertNotNull(response.foto());
            assertEquals("avatar.png", response.foto().nombreArchivo());
        }

        @Test
        void sinFoto_FotoEsNull() {
            Usuario usuario = Usuario.builder()
                .id(2L)
                .nombre("Carlos")
                .email("carlos@example.com")
                .estado(true)
                .build();

            UsuarioResponse response = usuarioMapper.toResponse(usuario);

            assertNotNull(response);
            assertNull(response.foto());
        }

        @Test
        void conEntidadNula_RetornaNull() {
            assertNull(usuarioMapper.toResponse(null));
        }
    }

    @Nested
    class ToEntity {

        @Test
        void conTodosLosCampos_MapearCorrectamente() {
            UsuarioRequest request = new UsuarioRequest(
                "María López", "maria@example.com", "555-9999", "Av. Principal 1", null, false
            );

            Usuario entity = usuarioMapper.toEntity(request);

            assertNotNull(entity);
            assertNull(entity.getId());             // ignorado
            assertNull(entity.getFechaRegistro());  // ignorado
            assertNull(entity.getFoto());           // ignorado
            assertEquals("María López", entity.getNombre());
            assertEquals("maria@example.com", entity.getEmail());
            assertEquals("555-9999", entity.getTelefono());
            assertEquals("Av. Principal 1", entity.getDireccion());
            assertFalse(entity.isEstado());
        }

        @Test
        void sinEstado_EstadoDefaultTrue() {
            UsuarioRequest request = new UsuarioRequest(
                "Nuevo", "nuevo@example.com", null, null, null, null
            );

            Usuario entity = usuarioMapper.toEntity(request);

            assertTrue(entity.isEstado());          // defaultValue = "true"
        }
    }
}
