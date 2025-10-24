package com.factura.sri.controller;

import com.factura.sri.dto.ClienteConDocumentosDto;
import com.factura.sri.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {


    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteConDocumentosDto>> listarClientes() {
        List<ClienteConDocumentosDto> clientes = clienteService.listarClientes();
        if (clientes.isEmpty()) {

            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clientes);
    }

    @PostMapping
    public ResponseEntity<ClienteConDocumentosDto> crearCliente( @Valid @RequestBody ClienteConDocumentosDto clienteDto) {

        if (clienteDto.getDocumentos() == null || clienteDto.getDocumentos().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ClienteConDocumentosDto clienteGuardado = clienteService.guardar(clienteDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteGuardado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteConDocumentosDto> obtenerPorId(@PathVariable Long id) {
        ClienteConDocumentosDto cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(cliente);
    }


    @PutMapping("/{id}") //
    public ResponseEntity<ClienteConDocumentosDto> actualizar
    (@PathVariable Long id, @Valid @RequestBody ClienteConDocumentosDto clienteDto) {
        ClienteConDocumentosDto clienteActualizado = clienteService.actualizar(id, clienteDto);
        return ResponseEntity.ok(clienteActualizado);
    }

    @DeleteMapping("/{id}") //
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
