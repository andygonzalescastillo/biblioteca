package com.biblioteca.backend.entity;

import org.hibernate.annotations.SQLDelete;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "libro")
@SQLDelete(sql = "UPDATE libro SET estado = false WHERE id = ?")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede superar los 255 caracteres")
    @Column(name = "titulo", nullable = false)
    String titulo;

    @NotBlank(message = "El ISBN es obligatorio")
    @Size(min = 10, max = 20, message = "El ISBN debe tener entre 10 y 20 caracteres")
    @Column(name = "isbn", nullable = false, length = 20)
    String isbn;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    boolean estado = true;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock", nullable = false)
    @Builder.Default
    Integer stock = 0;

    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    Categoria categoria;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "portada_imagen_id")
    Imagen portada;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "libro_autor",
        joinColumns = @JoinColumn(name = "libro_id"),
        inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    @Builder.Default
    Set<Autor> autores = new HashSet<>();
}
