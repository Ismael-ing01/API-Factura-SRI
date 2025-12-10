package com.factura.sri.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "impuesto")
public class Impuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo; // Ej: "2" (IVA), "3" (ICE)

    @NotBlank
    @Column(name = "codigo_porcentaje", nullable = false, length = 10)
    private String codigoPorcentaje; // Ej: "0", "2", "4" (según tabla técnica SRI)

    @NotNull
    @Column(name = "porcentaje", nullable = false)
    private Double porcentaje; // Ej: 0.0, 12.0, 15.0

    @NotBlank
    @Column(name = "descripcion", nullable = false)
    private String descripcion; // Ej: "IVA 15%"

    @Column(name = "activo")
    private Boolean activo = true;
}
