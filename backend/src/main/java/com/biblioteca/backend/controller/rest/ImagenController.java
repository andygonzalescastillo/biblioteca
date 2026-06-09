package com.biblioteca.backend.controller.rest;

import com.biblioteca.backend.dto.response.ImagenResponse;
import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.mapper.ImagenMapper;
import com.biblioteca.backend.service.ImagenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
@Tag(name = "1. Imágenes", description = "Endpoints para subir y eliminar archivos de imagen (portadas de libros, fotos de perfil). Los archivos se almacenan físicamente en disco.")
public class ImagenController {

    private final ImagenService imagenService;
    private final ImagenMapper imagenMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "11_imagenes_subir",
               summary = "Subir una imagen",
               description = "Sube un archivo de imagen al servidor. El archivo se guarda físicamente en la carpeta 'uploads/' con un nombre UUID único. La respuesta devuelve el ID y la URL de acceso para asociar la imagen a un Libro o Usuario.")
    public ResponseEntity<ImagenResponse> subirImagen(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        Imagen imagen = imagenService.guardarImagen(archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(imagenMapper.toResponse(imagen));
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "12_imagenes_eliminar",
               summary = "Eliminar una imagen",
               description = "Elimina el registro de la imagen en base de datos Y el archivo físico del disco. Asegúrate de desasociar la imagen del libro o usuario antes de llamar a este endpoint.")
    public ResponseEntity<Void> eliminarImagen(@PathVariable UUID id) throws IOException {
        imagenService.eliminarImagen(id);
        return ResponseEntity.noContent().build();
    }
}
