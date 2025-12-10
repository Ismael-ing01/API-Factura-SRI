package com.factura.sri.dto;

import lombok.Data;

@Data
public class CajaDTO {
    private Long id;
    private String puntoEmision;
    private String nombre;
    private Long sucursalId;
}
