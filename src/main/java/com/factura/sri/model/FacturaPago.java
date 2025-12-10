package com.factura.sri.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "factura_pago")
public class FacturaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forma_pago_id", nullable = false)
    private FormaPago formaPago;

    @NotNull
    @Column(name = "total", nullable = false)
    private Double total; // Monto pagado con esta forma

    @Column(name = "plazo")
    private Double plazo;

    @Column(name = "unidad_tiempo", length = 20)
    private String unidadTiempo; // "DIAS", "MESES", "ANIOS"
}
