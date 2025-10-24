package com.factura.sri.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FacturaRequestDTO {
    @NotNull
    private Long clienteId; // ID del Cliente

    @NotEmpty
    private List<ItemRequestDTO> items; // Lista de items a incluir
}