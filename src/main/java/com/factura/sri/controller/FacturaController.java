package com.factura.sri.controller;

import com.factura.sri.dto.FacturaRequestDTO;
import com.factura.sri.dto.FacturaResponseDTO;
import com.factura.sri.model.Factura;
import com.factura.sri.service.FacturaService;
import com.factura.sri.service.GeneradorXmlFacturaService;
import jakarta.validation.Valid; // Importa Valid para validaciones de DTO
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;
    private final GeneradorXmlFacturaService generadorXmlFacturaService;

    public FacturaController(FacturaService facturaService, GeneradorXmlFacturaService generadorXmlFacturaService) {
        this.facturaService = facturaService;
        this.generadorXmlFacturaService = generadorXmlFacturaService;
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


    /**
     * Endpoint para obtener el XML generado de una factura específica.
     */
    @GetMapping(value = "/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> obtenerFacturaXml(@PathVariable Long id) {
        // 1. Llama al NUEVO método del servicio que busca Y genera el XML
        String xmlFactura = facturaService.generarXmlParaFactura(id); // <-- CAMBIO AQUÍ

        // 2. Prepara las cabeceras (sin cambios)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        // headers.setContentDispositionFormData("attachment", factura.getClaveAcceso() + ".xml"); // Necesitarías la clave aquí si la quieres

        // 3. Devuelve el XML (sin cambios)
        return new ResponseEntity<>(xmlFactura, headers, HttpStatus.OK);
    }

}
