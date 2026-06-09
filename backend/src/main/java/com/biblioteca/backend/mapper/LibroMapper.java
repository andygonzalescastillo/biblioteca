package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.request.LibroRequest;
import com.biblioteca.backend.dto.response.LibroResponse;
import com.biblioteca.backend.entity.Libro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class, AutorMapper.class, ImagenMapper.class})
public interface LibroMapper {

    LibroResponse toResponse(Libro libro);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "portada", ignore = true)
    @Mapping(target = "autores", ignore = true)
    @Mapping(target = "estado", source = "estado", defaultValue = "true")
    Libro toEntity(LibroRequest request);
}
