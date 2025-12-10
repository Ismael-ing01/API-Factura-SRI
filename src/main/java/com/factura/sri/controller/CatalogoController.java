package com.factura.sri.controller;

import com.factura.sri.dto.FormaPagoDTO;
import com.factura.sri.dto.ImpuestoDTO;
import com.factura.sri.service.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/impuestos")
    public ResponseEntity<List<ImpuestoDTO>> listarImpuestos() {
        return ResponseEntity.ok(catalogoService.listarImpuestos());
    }

    @GetMapping("/formas-pago")
    public ResponseEntity<List<FormaPagoDTO>> listarFormasPago() {
        return ResponseEntity.ok(catalogoService.listarFormasPago());
    }
}
