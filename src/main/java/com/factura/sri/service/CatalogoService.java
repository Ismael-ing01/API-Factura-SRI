package com.factura.sri.service;

import com.factura.sri.dto.FormaPagoDTO;
import com.factura.sri.dto.ImpuestoDTO;
import com.factura.sri.model.FormaPago;
import com.factura.sri.model.Impuesto;
import com.factura.sri.repository.FormaPagoRepository;
import com.factura.sri.repository.ImpuestoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogoService {

    private final ImpuestoRepository impuestoRepository;
    private final FormaPagoRepository formaPagoRepository;

    public CatalogoService(ImpuestoRepository impuestoRepo, FormaPagoRepository formaPagoRepo) {
        this.impuestoRepository = impuestoRepo;
        this.formaPagoRepository = formaPagoRepo;
    }

    public List<ImpuestoDTO> listarImpuestos() {
        return impuestoRepository.findAll().stream().map(i -> {
            ImpuestoDTO dto = new ImpuestoDTO();
            dto.setId(i.getId());
            dto.setCodigo(i.getCodigo());
            dto.setCodigoPorcentaje(i.getCodigoPorcentaje());
            dto.setPorcentaje(i.getPorcentaje());
            dto.setDescripcion(i.getDescripcion());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<FormaPagoDTO> listarFormasPago() {
        return formaPagoRepository.findAll().stream().map(fp -> {
            FormaPagoDTO dto = new FormaPagoDTO();
            dto.setId(fp.getId());
            dto.setCodigo(fp.getCodigo());
            dto.setDescripcion(fp.getDescripcion());
            return dto;
        }).collect(Collectors.toList());
    }

    // Métodos para llenar data inicial si se desea
    public void crearImpuesto(Impuesto i) {
        impuestoRepository.save(i);
    }

    public void crearFormaPago(FormaPago fp) {
        formaPagoRepository.save(fp);
    }
}
