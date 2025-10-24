package com.factura.sri.model;

import com.factura.sri.enums.TipoCodigoImpuestoIva; // Importa el Enum
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "item_factura")
public class ItemFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    private Integer cantidad;

    @NotNull
    @Positive
    @Column(name = "precio_unitario")
    private Double precioUnitario; // Precio al momento de la venta

    // --- Relación con Factura ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    // --- Relación con Producto ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // --- Campos Requeridos por SRI ---
    @NotNull
    @Enumerated(EnumType.STRING) // Guarda el nombre del Enum como String en la BD
    @Column(name = "codigo_impuesto_iva", length = 15)
    private TipoCodigoImpuestoIva codigoImpuestoIva; // Usa el Enum TipoCodigoImpuestoIva

    @NotNull
    @Column(name = "tarifa_iva")
    private Double tarifaIva; // El porcentaje (ej: 15.0, 0.0)

    // --- Totales de la Línea ---
    @NotNull
    @Column(name = "subtotal") // cantidad * precioUnitario
    private Double subtotal;

    @NotNull
    @Column(name = "valor_iva") // subtotal * (tarifaIva / 100)
    private Double valorIva;
}