package com.biblioteca.backend.entity;

import org.hibernate.annotations.SQLDelete;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "autor")
@SQLDelete(sql = "UPDATE autor SET estado = false WHERE id = ?")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    String nombre;

    @Column(name = "biografia", columnDefinition = "TEXT")
    String biografia;

    @Column(name = "fecha_nacimiento")
    LocalDate fechaNacimiento;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "foto_imagen_id")
    Imagen foto;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    boolean estado = true;

    @ManyToMany(mappedBy = "autores")
    @JsonIgnore
    @Builder.Default
    Set<Libro> libros = new HashSet<>();
}
