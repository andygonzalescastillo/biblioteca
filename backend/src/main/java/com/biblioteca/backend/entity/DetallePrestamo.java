package com.biblioteca.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.Min;

@Entity
@Table(name = "detalle_prestamo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetallePrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestamo_id", nullable = false)
    @JsonIgnore
    Prestamo prestamo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "libro_id", nullable = false)
    Libro libro;

    @Column(name = "cantidad", nullable = false)
    @Builder.Default
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    Integer cantidad = 1;
}
