package com.factura.sri.service;

import com.factura.sri.model.TipoDocumento;
import com.factura.sri.repository.TipoDocumentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    public TipoDocumentoService(TipoDocumentoRepository tipoDocumentoRepository) {
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    public List<TipoDocumento> listarTodos() {
        return tipoDocumentoRepository.findAll();
    }

    public  TipoDocumento obtenerPorId(Long id){
        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TipoDocumento no encontrado con id: " + id));
    }

    public TipoDocumento guardar(TipoDocumento tipoDocumento){
        return tipoDocumentoRepository.save(tipoDocumento);
    }

    public TipoDocumento actualizar(Long id, TipoDocumento tipoDocumento){
       return tipoDocumentoRepository.findById(id)
               .map(td -> {
                   td.setCodigoTipoDocumento(tipoDocumento.getCodigoTipoDocumento());
                   td.setNombreTipoDocumento(tipoDocumento.getNombreTipoDocumento());
                   return tipoDocumentoRepository.save(td);
               })
               .orElseThrow(() -> new RuntimeException("TipoDocumento no encontrado con id: " + id));
    }


    public void eliminar(Long id){
         tipoDocumentoRepository.findById(id)
                 .orElseThrow(()-> new RuntimeException("TipoDocumento no encontrado con id: " + id));
            tipoDocumentoRepository.deleteById(id);

    }
}

