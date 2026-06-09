package com.biblioteca.backend.entity;

import org.hibernate.annotations.SQLDelete;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria")
@SQLDelete(sql = "UPDATE categoria SET estado = false WHERE id = ?")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    String nombre;

    @Column(name = "descripcion")
    String descripcion;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    boolean estado = true;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    @Builder.Default
    List<Libro> libros = new ArrayList<>();
}
