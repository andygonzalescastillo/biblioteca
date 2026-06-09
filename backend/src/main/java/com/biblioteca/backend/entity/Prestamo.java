package com.biblioteca.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prestamo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull(message = "El lector/usuario es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    Usuario usuario;

    @NotNull(message = "La fecha de préstamo es obligatoria")
    @Column(name = "fecha_prestamo", nullable = false, updatable = false)
    Instant fechaPrestamo;

    @NotNull(message = "La fecha límite de devolución es obligatoria")
    @Column(name = "fecha_devolucion_limite", nullable = false)
    Instant fechaDevolucionLimite;

    @Column(name = "fecha_devolucion_real")
    Instant fechaDevolucionReal;

    @NotNull(message = "El estado del préstamo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    @OneToMany(mappedBy = "prestamo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    List<DetallePrestamo> detalles = new ArrayList<>();

    public int getCantidadTotal() {
        return detalles.stream()
                .map(DetallePrestamo::getCantidad)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    @PrePersist
    protected void onCreate() {
        if (this.fechaPrestamo == null) {
            this.fechaPrestamo = Instant.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPrestamo.ACTIVO;
        }
    }
}
