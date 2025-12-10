package com.factura.sri.dto;

import lombok.Data;

@Data
public class EmpresaDTO {
    private Long id;
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String direccionMatriz;
    private String contribuyenteEspecial;
    private String obligadoContabilidad;
    private String logoUrl;
    private Integer ambienteSri;
}
