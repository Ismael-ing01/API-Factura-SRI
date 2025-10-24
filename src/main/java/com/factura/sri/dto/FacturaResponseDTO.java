package com.factura.sri.dto;

import com.factura.sri.enums.EstadoSri; // Importa el Enum
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class FacturaResponseDTO {
    private Long id;
    private String numeroFactura;
    private LocalDateTime fechaEmision;
    private Long clienteId;
    private String clienteNombre; // Incluye el nombre del cliente para claridad
    private EstadoSri estadoSri; // Usa el Enum para el estado

    // Totales
    private Double subtotalSinImpuestos;
    private Double subtotalIva;
    private Double subtotalIva0;
    private Double valorIva;
    private Double total;

    private List<ItemResponseDTO> items; // Lista detallada de items
}