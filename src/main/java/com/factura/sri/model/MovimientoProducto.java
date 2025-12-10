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
@Table(name = "movimiento_producto")
public class MovimientoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @NotNull
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento; // COMPRA, VENTA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO

    @NotNull
    @Column(name = "cantidad")
    private Integer cantidad; // Cantidad que entra o sale (positivo o negativo)

    @NotNull
    @Column(name = "costo_unitario")
    private Double costoUnitario; // Costo al momento del movimiento

    @NotNull
    @Column(name = "saldo_stock")
    private Integer saldoStock; // Stock resultante despues del movimiento

    @Column(name = "referencia")
    private String referencia; // Nro Factura, Nro Compra, Motivo ajuste

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Quien hizo el movimiento
}
