package com.biblioteca.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "imagen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "nombre_archivo", nullable = false)
    String nombreArchivo;

    @Column(name = "url_almacenamiento", nullable = false, length = 512)
    String urlAlmacenamiento;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    Instant fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = Instant.now();
    }
}
