package com.biblioteca.backend.entity;

import org.hibernate.annotations.SQLDelete;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;

@Entity
@Table(name = "usuario")
@SQLDelete(sql = "UPDATE usuario SET estado = false WHERE id = ?")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 150, message = "El email no puede superar los 150 caracteres")
    @Column(name = "email", nullable = false, length = 150)
    String email;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    @Column(name = "telefono", length = 20)
    String telefono;

    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    @Column(name = "direccion", length = 255)
    String direccion;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    boolean estado = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    Instant fechaRegistro;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "foto_imagen_id")
    Imagen foto;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = Instant.now();
    }
}
