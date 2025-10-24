package com.factura.sri.model;

import com.factura.sri.enums.TipoImpuestoIvaProducto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El nombre del producto es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "El precio no puede ser nulo")
    @Positive(message = "El precio debe ser un valor positivo")
    @Column(name = "precio", nullable = false)
    private Double precio;

    @NotNull(message = "El stock no puede ser nulo")
    @PositiveOrZero(message = "El stock debe ser cero o un valor positivo")
    @Column(name = "stock", nullable = false)
    private Integer stock;

    // --- RELACIÓN CON CATEGORIA ---
    // Muchos productos pueden pertenecer a una categoría.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /**
     * Guarda el tipo de IVA que aplica a este producto.
     * Se guarda como String ("IVA_0" o "IVA_GENERAL") en la base de datos.
     */
    @NotNull(message = "Debe especificar el tipo de IVA del producto")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_impuesto_iva", nullable = false, length = 15)
    private TipoImpuestoIvaProducto tipoImpuestoIva;
}
