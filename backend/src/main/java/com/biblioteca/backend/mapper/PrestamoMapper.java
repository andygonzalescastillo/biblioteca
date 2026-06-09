package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.response.DetallePrestamoResponse;
import com.biblioteca.backend.dto.response.PrestamoResponse;
import com.biblioteca.backend.entity.DetallePrestamo;
import com.biblioteca.backend.entity.Prestamo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class, LibroMapper.class})
public interface PrestamoMapper {

    PrestamoResponse toResponse(Prestamo prestamo);

    DetallePrestamoResponse toDetalleResponse(DetallePrestamo detalle);
}
