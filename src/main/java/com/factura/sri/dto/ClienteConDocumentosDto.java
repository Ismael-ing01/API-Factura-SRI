package com.factura.sri.dto;

import com.factura.sri.model.Cliente;
import com.factura.sri.model.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ClienteConDocumentosDto {

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;
    private String direccion;
    private String telefono;
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    private String email;
    // Default to 'A' (Activo) if not provided, or handle in service
    private String estado = "A";

    private List<DocumentoClienteDto> documentos;

    // Helper para obtener entidad (opcional, pero util para refactorizar menos
    // service)
    public Cliente toClienteEntity() {
        Cliente c = new Cliente();
        c.setNombres(this.nombres);
        c.setApellidos(this.apellidos);
        c.setDireccion(this.direccion);
        c.setTelefono(this.telefono);
        c.setEmail(this.email);
        c.setEstado(this.estado);
        return c;
    }
}
