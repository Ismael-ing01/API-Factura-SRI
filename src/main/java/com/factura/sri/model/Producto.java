package com.factura.sri.model;

import com.factura.sri.enums.TipoImpuestoIvaProducto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    // --- Nuevos Campos ---
    @NotNull(message = "El precio de compra es obligatorio")
    @PositiveOrZero
    @Column(name = "precio_compra", nullable = false)
    private Double precioCompra;

    @NotNull(message = "El margen de utilidad es obligatorio")
    @PositiveOrZero
    @Column(name = "margen_utilidad", nullable = false)
    private Double margenUtilidad; // Porcentaje (ej: 30.0 para 30%)

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductoImagen> imagenes = new ArrayList<>();

    // --- Lógica de negocio ---
    /**
     * Calcula el precio de venta sugerido basado en compra y margen.
     * Ejemplo: Costo 10, Margen 30% -> Precio = 10 / (1 - 0.30) = 14.28
     * O si es markup directo: Costo 10 + 30% = 13.
     * Usaremos markup directo por defecto (Costo * (1 + margen/100))
     */
    public void calcularPrecioVenta() {
        if (this.precioCompra != null && this.margenUtilidad != null) {
            this.precio = this.precioCompra * (1 + (this.margenUtilidad / 100));
        }
    }

    @PrePersist
    @PreUpdate
    public void preSave() {
        calcularPrecioVenta();
    }
}
