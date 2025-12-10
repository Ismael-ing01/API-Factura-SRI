package com.factura.sri.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "caja")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El punto de emisión es obligatorio")
    @Size(min = 3, max = 3, message = "El punto de emisión debe tener 3 dígitos (ej: 001)")
    @Column(name = "punto_emision", nullable = false, length = 3)
    private String puntoEmision; // 001, 002...

    @Column(name = "nombre", nullable = false)
    private String nombre; // Ej: "Caja Principal", "Caja 2"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    // Aquí se podría guardar el secuencial actual de facturas si se desea controlar
    // por caja
    @Column(name = "secuencial_factura_actual")
    private Integer secuencialFacturaActual = 0;
}
