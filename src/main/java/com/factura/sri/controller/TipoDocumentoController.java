package com.factura.sri.controller;

import jakarta.validation.Valid;
import com.factura.sri.model.TipoDocumento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.factura.sri.service.TipoDocumentoService;

import java.util.List;

@RestController
@RequestMapping("/api/tipo_documentos")
public class TipoDocumentoController {

    @Autowired
    private TipoDocumentoService tipoDocumentoService;

    @GetMapping
    public List<TipoDocumento> listar() {
        return tipoDocumentoService.listarTodos();
    }

    @GetMapping("/{id}")
    public TipoDocumento obtenerPorId(@PathVariable Long id) {
        return tipoDocumentoService.obtenerPorId(id);
    }

    @PostMapping
    public TipoDocumento guardar(@Valid @RequestBody TipoDocumento tipoDocumento) {
        return tipoDocumentoService.guardar(tipoDocumento);
    }

    @PutMapping("/{id}")
    public TipoDocumento actualizar(@PathVariable Long id, @Valid @RequestBody TipoDocumento tipoDocumento) {
        return tipoDocumentoService.actualizar(id, tipoDocumento);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        tipoDocumentoService.eliminar(id);
    }
}
