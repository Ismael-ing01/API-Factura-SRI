package com.factura.sri.controller;

import com.factura.sri.dto.CajaDTO;
import com.factura.sri.dto.EmpresaDTO;
import com.factura.sri.dto.SucursalDTO;
import com.factura.sri.model.Empresa;
import com.factura.sri.model.Sucursal;
import com.factura.sri.model.Caja;
import com.factura.sri.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizacion")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    // --- Empresa ---
    @PostMapping("/empresas")
    public ResponseEntity<Empresa> crearEmpresa(@RequestBody @Valid EmpresaDTO dto) {
        return ResponseEntity.ok(organizationService.guardarEmpresa(dto));
    }

    @GetMapping("/empresas")
    public ResponseEntity<List<Empresa>> listarEmpresas() {
        return ResponseEntity.ok(organizationService.listarEmpresas());
    }

    // --- Sucursal ---
    @PostMapping("/sucursales")
    public ResponseEntity<Sucursal> crearSucursal(@RequestBody @Valid SucursalDTO dto) {
        return ResponseEntity.ok(organizationService.guardarSucursal(dto));
    }

    @GetMapping("/empresas/{empresaId}/sucursales")
    public ResponseEntity<List<SucursalDTO>> listarSucursales(@PathVariable Long empresaId) {
        return ResponseEntity.ok(organizationService.listarSucursalesPorEmpresa(empresaId));
    }

    // --- Caja ---
    @PostMapping("/cajas")
    public ResponseEntity<Caja> crearCaja(@RequestBody @Valid CajaDTO dto) {
        return ResponseEntity.ok(organizationService.guardarCaja(dto));
    }

    @GetMapping("/sucursales/{sucursalId}/cajas")
    public ResponseEntity<List<CajaDTO>> listarCajas(@PathVariable Long sucursalId) {
        return ResponseEntity.ok(organizationService.listarCajasPorSucursal(sucursalId));
    }
}
