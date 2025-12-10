package com.factura.sri.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "forma_pago")
public class FormaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo; // Ej: "01" (Sin utilización sistema financiero)

    @NotBlank
    @Column(name = "descripcion", nullable = false)
    private String descripcion; // Ej: "SIN UTILIZACION DEL SISTEMA FINANCIERO"
}
