package com.factura.sri.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemResponseDTO {
    private Long id;
    private Integer cantidad;
    private Double precioUnitario;
    private String nombreProducto; // Incluye el nombre del producto para claridad
    private Double subtotal;
    private Double valorIva;
}