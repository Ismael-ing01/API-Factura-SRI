package com.factura.sri.model;

import com.factura.sri.enums.EstadoSri; // Importa el Enum
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "factura")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Datos del Comprobante ---
    @Column(name = "numero_factura", unique = true, nullable = false, length = 17)
    private String numeroFactura; // Formato: 001-001-000001234

    @CreationTimestamp
    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private LocalDateTime fechaEmision;

    // --- Relación con Cliente ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // --- Relación con Items ---
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemFactura> items = new ArrayList<>();

    // --- Campos Requeridos por SRI ---
    @Column(name = "clave_acceso", length = 49, unique = true)
    private String claveAcceso; // Clave de acceso de 49 dígitos (inicialmente nulo)

    @Column(name = "autorizacion_sri", length = 49)
    private String autorizacionSri; // Número de autorización SRI (inicialmente nulo)

    @NotNull
    @Enumerated(EnumType.STRING) // Guarda el nombre del Enum como String en la BD
    @Column(name = "estado_sri", nullable = false, length = 15)
    private EstadoSri estadoSri; // Usa el Enum EstadoSri

    // --- Totales Desglosados (Requeridos por SRI) ---
    @NotNull
    @Column(name = "subtotal_sin_impuestos")
    private Double subtotalSinImpuestos;

    @NotNull
    @Column(name = "subtotal_iva") // Suma de bases imponibles que SÍ graban IVA (tarifa general)
    private Double subtotalIva;

    @NotNull
    @Column(name = "subtotal_iva_0") // Suma de bases imponibles con IVA 0%
    private Double subtotalIva0;

    @NotNull
    @Column(name = "valor_iva") // Monto total de IVA cobrado
    private Double valorIva;

    @NotNull
    @Column(name = "total") // Valor final: subtotalSinImpuestos + valorIva
    private Double total;

    // --- Método Útil ---
    public void addItem(ItemFactura item) {
        items.add(item);
        item.setFactura(this);
    }
}