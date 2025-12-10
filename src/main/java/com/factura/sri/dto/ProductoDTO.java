package com.factura.sri.dto; // Puedes crear un nuevo paquete 'dto'

import com.factura.sri.enums.TipoImpuestoIvaProducto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductoDTO {

    private String nombre;
    private Double precio;
    private Integer stock;
    private Long categoriaId;

    // --- Nuevos campos ---
    private Double precioCompra;
    private Double margenUtilidad;
    private java.util.List<String> imagenes; // URLs

    @NotNull(message = "Debe especificar el tipo de IVA del producto") // <-- Validación
    private TipoImpuestoIvaProducto tipoImpuestoIva; // <-- NUEVO CAMPO
}
