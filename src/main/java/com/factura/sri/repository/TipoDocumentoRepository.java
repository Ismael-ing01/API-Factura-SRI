package com.factura.sri.repository;


import com.factura.sri.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoDocumentoRepository  extends JpaRepository<TipoDocumento, Long> {


}
