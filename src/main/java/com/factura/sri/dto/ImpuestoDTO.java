package com.factura.sri.dto;

import lombok.Data;

@Data
public class ImpuestoDTO {
    private Long id;
    private String codigo;
    private String codigoPorcentaje;
    private Double porcentaje;
    private String descripcion;
}
