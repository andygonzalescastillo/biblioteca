package com.biblioteca.backend.service.impl;

import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.ImagenRepository;
import com.biblioteca.backend.service.ImagenService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.coobird.thumbnailator.Thumbnails;

@Service
@RequiredArgsConstructor
public class ImagenServiceImpl implements ImagenService {

    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp"
    );

    private final ImagenRepository imagenRepository;

    @Value("${app.upload.dir:${user.dir}/uploads}")
    private String uploadDir;

    @Override
    public Imagen obtenerImagenPorId(UUID id) {
        return imagenRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada con el ID: " + id));
    }

    @Override
    public Imagen guardarImagen(MultipartFile archivo) throws IOException {
        if (archivo.isEmpty()) {
            throw BusinessException.badRequest("EMPTY_FILE", "No se puede guardar un archivo vacío.");
        }

        validarTipoImagen(archivo);

        Path directorio = Path.of(uploadDir);
        Files.createDirectories(directorio);

        String nombreOriginal = archivo.getOriginalFilename();
        
        
        String nuevoNombreArchivo = UUID.randomUUID() + ".jpg";
        Path rutaDestino = directorio.resolve(nuevoNombreArchivo);

        
        
        
        Thumbnails.of(archivo.getInputStream())
                .size(400, 600)
                .outputQuality(0.85)
                .outputFormat("jpg")
                .toFile(rutaDestino.toFile());

        String urlAcceso = "/uploads/" + nuevoNombreArchivo;
        
        Imagen imagen = Imagen.builder()
                .nombreArchivo(nombreOriginal)
                .urlAlmacenamiento(urlAcceso)
                .build();

        return imagenRepository.save(imagen);
    }

    @Override
    public void eliminarImagen(UUID id) throws IOException {
        Imagen imagen = obtenerImagenPorId(id);
        String nombreArchivo = imagen.getUrlAlmacenamiento().replace("/uploads/", "");
        Path rutaArchivo = Path.of(uploadDir).resolve(nombreArchivo);
        Files.deleteIfExists(rutaArchivo);
        imagenRepository.delete(imagen);
    }

    private void validarTipoImagen(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw BusinessException.badRequest("INVALID_IMAGE_TYPE", "Solo se permiten imágenes JPG, PNG o WEBP.");
        }

        String extension = Optional.ofNullable(archivo.getOriginalFilename())
                .filter(nombre -> nombre.contains("."))
                .map(nombre -> nombre.substring(nombre.lastIndexOf(".")).toLowerCase())
                .orElse("");

        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw BusinessException.badRequest("INVALID_IMAGE_EXTENSION", "La imagen debe tener extensión JPG, PNG o WEBP.");
        }
    }
}
