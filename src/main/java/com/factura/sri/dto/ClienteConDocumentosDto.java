package com.factura.sri.dto;

import com.factura.sri.model.Cliente;
import lombok.Data;

import java.util.List;

@Data
public class ClienteConDocumentosDto {

    private Cliente cliente;
    private List<DocumentoClienteDto> documentos;
}
