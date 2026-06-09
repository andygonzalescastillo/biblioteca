package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.request.AutorRequest;
import com.biblioteca.backend.dto.response.AutorResponse;
import com.biblioteca.backend.entity.Autor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ImagenMapper.class})
public interface AutorMapper {

    AutorResponse toResponse(Autor autor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "libros", ignore = true)
    @Mapping(target = "foto", ignore = true)
    @Mapping(target = "estado", source = "estado", defaultValue = "true")
    Autor toEntity(AutorRequest request);
}
