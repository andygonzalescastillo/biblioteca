package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.response.ImagenResponse;
import com.biblioteca.backend.entity.Imagen;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImagenMapper {
    ImagenResponse toResponse(Imagen imagen);
}
