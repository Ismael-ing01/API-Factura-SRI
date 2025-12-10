package com.factura.sri.service;

import com.factura.sri.dto.ClienteConDocumentosDto;
import com.factura.sri.dto.DocumentoClienteDto;
import com.factura.sri.model.Cliente;
import com.factura.sri.model.DocumentoCliente;
import com.factura.sri.model.TipoDocumento;
import com.factura.sri.repository.ClienteRepository;
import com.factura.sri.repository.DocumentoClienteRepository;
import com.factura.sri.repository.TipoDocumentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private DocumentoClienteRepository documentoClienteRepository;

    @Transactional
    public ClienteConDocumentosDto guardar(ClienteConDocumentosDto clienteDto) {

        List<DocumentoClienteDto> documentos = clienteDto.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            throw new RuntimeException("No tiene documentos asociados");
        }

        // Usar método helper o map manual
        Cliente cliente = clienteDto.toClienteEntity();
        if (cliente.getEstado() == null)
            cliente.setEstado("A"); // Default

        cliente = clienteRepository.save(cliente);

        for (DocumentoClienteDto documentoDto : documentos) {
            DocumentoCliente documentoCliente = new DocumentoCliente();
            documentoCliente.setCliente(cliente);
            // documentoDto.getNumeroDocumento(); // linea muerta
            // documentoDto.getIdTipoDocumento(); // linea muerta
            documentoCliente.setNumeroDocumento(documentoDto.getNumeroDocumento());
            TipoDocumento tipoDocumento = tipoDocumentoRepository.getById(documentoDto.getIdTipoDocumento());
            documentoCliente.setTipoDocumento(tipoDocumento);
            documentoClienteRepository.save(documentoCliente);
        }

        // No necesitamos devolver el ID en el DTO de entrada, pero si quieres:
        // clienteDto.setId(cliente.getId());
        return clienteDto;
    }

    public List<ClienteConDocumentosDto> listarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();

        return clientes.stream().map(this::convertirAClienteDto).collect(Collectors.toList());
    }

    public ClienteConDocumentosDto buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        return convertirAClienteDto(cliente);
    }

    private ClienteConDocumentosDto convertirAClienteDto(Cliente cliente) {
        List<DocumentoClienteDto> documentos = documentoClienteRepository
                .findByClienteId(cliente.getId())
                .stream()
                .map(doc -> {
                    DocumentoClienteDto docDto = new DocumentoClienteDto();
                    docDto.setIdTipoDocumento(doc.getTipoDocumento().getIdTipoDocumento());
                    docDto.setNumeroDocumento(doc.getNumeroDocumento());
                    return docDto;
                })
                .collect(Collectors.toList());

        ClienteConDocumentosDto dto = new ClienteConDocumentosDto();
        dto.setNombres(cliente.getNombres());
        dto.setApellidos(cliente.getApellidos());
        dto.setDireccion(cliente.getDireccion());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        dto.setEstado(cliente.getEstado());
        dto.setDocumentos(documentos);

        return dto;
    }

    @Transactional
    public ClienteConDocumentosDto actualizar(Long id, ClienteConDocumentosDto clienteDto) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        // Actualizar datos del cliente usando DTO plano
        clienteExistente.setNombres(clienteDto.getNombres());
        clienteExistente.setApellidos(clienteDto.getApellidos());
        clienteExistente.setDireccion(clienteDto.getDireccion());
        clienteExistente.setTelefono(clienteDto.getTelefono());
        clienteExistente.setEmail(clienteDto.getEmail());
        if (clienteDto.getEstado() != null) {
            clienteExistente.setEstado(clienteDto.getEstado());
        }
        clienteRepository.save(clienteExistente);

        // Eliminar documentos anteriores
        documentoClienteRepository.deleteAll(
                documentoClienteRepository.findByClienteId(id));

        // Guardar documentos nuevos
        for (DocumentoClienteDto docDto : clienteDto.getDocumentos()) {
            DocumentoCliente documento = new DocumentoCliente();
            documento.setCliente(clienteExistente);
            documento.setNumeroDocumento(docDto.getNumeroDocumento());

            TipoDocumento tipo = tipoDocumentoRepository.getById(docDto.getIdTipoDocumento());
            documento.setTipoDocumento(tipo);
            documentoClienteRepository.save(documento);
        }

        return clienteDto;
    }

    @Transactional
    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }

        documentoClienteRepository.deleteAll(
                documentoClienteRepository.findByClienteId(id));

        clienteRepository.deleteById(id);
    }

}
