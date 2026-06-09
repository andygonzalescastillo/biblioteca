package com.biblioteca.backend.mapper;

import com.biblioteca.backend.dto.request.UsuarioRequest;
import com.biblioteca.backend.dto.response.UsuarioResponse;
import com.biblioteca.backend.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ImagenMapper.class})
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "foto", ignore = true)
    @Mapping(target = "estado", source = "estado", defaultValue = "true")
    Usuario toEntity(UsuarioRequest request);
}
