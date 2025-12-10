package com.factura.sri.dto;

import lombok.Data;

@Data
public class FacturaPagoDTO {
    private Long formaPagoId;
    private Double total;
    private Double plazo;
    private String unidadTiempo;
}
