package com.factura.sri.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequestDTO {
    @NotNull
    private Long productoId; // ID del Producto

    @NotNull
    @Positive
    private Integer cantidad; // Cantidad
}