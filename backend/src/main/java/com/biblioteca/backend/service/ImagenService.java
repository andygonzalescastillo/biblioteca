package com.biblioteca.backend.service;

import com.biblioteca.backend.entity.Imagen;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

public interface ImagenService {
    Imagen obtenerImagenPorId(UUID id);
    Imagen guardarImagen(MultipartFile archivo) throws IOException;
    void eliminarImagen(UUID id) throws IOException;
}
