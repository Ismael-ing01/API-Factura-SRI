package com.factura.sri.dto;

import lombok.Data;

@Data
public class SucursalDTO {
    private Long id;
    private String codigo;
    private String direccion;
    private String nombre;
    private Long empresaId;
}
