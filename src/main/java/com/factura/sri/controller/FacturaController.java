package com.factura.sri.controller;

import com.factura.sri.dto.FacturaRequestDTO;
import com.factura.sri.dto.FacturaResponseDTO;
import com.factura.sri.service.FacturaService;
import jakarta.validation.Valid; // Importa Valid para validaciones de DTO
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    /**
     * Endpoint para CREAR una nueva factura.
     * Escucha peticiones POST en la URL /api/facturas.
     * Recibe los datos de la factura en el cuerpo de la petición (JSON).
     */
    @PostMapping
    public ResponseEntity<FacturaResponseDTO> crearFactura(@Valid @RequestBody FacturaRequestDTO facturaRequestDTO) {
        // 1. Llama al método del servicio para crear la factura
        FacturaResponseDTO nuevaFactura = facturaService.crearFactura(facturaRequestDTO);

        // 2. Devuelve una respuesta HTTP 201 (Created) con la factura creada en el cuerpo
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaFactura);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponseDTO> obtenerFacturaPorId(@PathVariable Long id) {
        // Llama al servicio para buscar la factura
        FacturaResponseDTO factura = facturaService.buscarFacturaPorId(id);
        // Devuelve la factura encontrada con estado 200 OK
        return ResponseEntity.ok(factura);
    }

    /**
     * Endpoint para LISTAR todas las facturas.
     * Escucha peticiones GET en /api/facturas
     */
    @GetMapping
    public ResponseEntity<List<FacturaResponseDTO>> listarFacturas() {
        // Llama al servicio para obtener la lista
        List<FacturaResponseDTO> facturas = facturaService.listarTodasLasFacturas();
        // Devuelve la lista con estado 200 OK
        return ResponseEntity.ok(facturas);
    }
}
