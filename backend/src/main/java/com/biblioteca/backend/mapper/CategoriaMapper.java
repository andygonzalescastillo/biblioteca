package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.request.CategoriaRequest;
import com.biblioteca.backend.dto.response.CategoriaResponse;
import com.biblioteca.backend.entity.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    CategoriaResponse toResponse(Categoria categoria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "libros", ignore = true)
    @Mapping(target = "estado", source = "estado", defaultValue = "true")
    Categoria toEntity(CategoriaRequest request);
}
