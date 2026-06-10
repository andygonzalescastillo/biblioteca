package com.biblioteca.backend.service.impl;

import com.biblioteca.backend.entity.Imagen;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.ImagenRepository;
import com.biblioteca.backend.service.ImagenService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
@RequiredArgsConstructor
public class ImagenServiceImpl implements ImagenService {

    private static final Set<String> CONTENT_TYPES_PERMITDOS = Set.of(
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
    private final Cloudinary cloudinary;

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

        String nombreOriginal = archivo.getOriginalFilename();

        // Subir los bytes directamente a Cloudinary con transformaciones de tamaño y optimización aplicadas al subir
        Map<?, ?> uploadResult = cloudinary.uploader().upload(archivo.getBytes(), ObjectUtils.asMap(
                "folder", "biblioteca",
                "transformation", "w_400,h_600,c_limit,q_auto,f_auto"
        ));

        String urlAlmacenamiento = (String) uploadResult.get("secure_url");

        Imagen imagen = Imagen.builder()
                .nombreArchivo(nombreOriginal)
                .urlAlmacenamiento(urlAlmacenamiento)
                .build();

        return imagenRepository.save(imagen);
    }

    @Override
    public void eliminarImagen(UUID id) throws IOException {
        Imagen imagen = obtenerImagenPorId(id);
        
        String url = imagen.getUrlAlmacenamiento();
        String publicId = extraerPublicId(url);
        
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
        
        imagenRepository.delete(imagen);
    }

    private String extraerPublicId(String url) {
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx == -1) {
            return null;
        }
        
        String pathAfterUpload = url.substring(uploadIdx + 8);
        int firstSlashIdx = pathAfterUpload.indexOf('/');
        if (firstSlashIdx == -1) {
            return null;
        }
        
        String versionOrPath = pathAfterUpload.substring(0, firstSlashIdx);
        String publicIdWithExt;
        
        if (versionOrPath.matches("v\\d+")) {
            publicIdWithExt = pathAfterUpload.substring(firstSlashIdx + 1);
        } else {
            publicIdWithExt = pathAfterUpload;
        }
        
        int dotIdx = publicIdWithExt.lastIndexOf('.');
        if (dotIdx == -1) {
            return publicIdWithExt;
        }
        
        return publicIdWithExt.substring(0, dotIdx);
    }

    private void validarTipoImagen(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITDOS.contains(contentType.toLowerCase())) {
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

