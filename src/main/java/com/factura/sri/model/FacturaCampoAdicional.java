package com.factura.sri.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "factura_campo_adicional")
public class FacturaCampoAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @NotBlank
    @Column(name = "nombre", nullable = false, length = 300)
    private String nombre; // Ej: "Email", "Observación"

    @NotBlank
    @Column(name = "valor", nullable = false, length = 300)
    private String valor; // Ej: "cliente@email.com"
}
