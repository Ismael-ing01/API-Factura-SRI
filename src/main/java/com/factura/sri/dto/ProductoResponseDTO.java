package com.factura.sri.dto;

import com.factura.sri.enums.TipoImpuestoIvaProducto;
import lombok.Data;

@Data
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private CategoriaDTO categoria;
    private TipoImpuestoIvaProducto tipoImpuestoIva;
}
