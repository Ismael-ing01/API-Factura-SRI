package com.factura.sri.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "movimiento_precio")
public class MovimientoPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @NotNull
    @Column(name = "precio_anterior")
    private Double precioAnterior;

    @NotNull
    @Column(name = "precio_nuevo")
    private Double precioNuevo;

    @Column(name = "margen_anterior")
    private Double margenAnterior;

    @Column(name = "margen_nuevo")
    private Double margenNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Quien cambio el precio
}
