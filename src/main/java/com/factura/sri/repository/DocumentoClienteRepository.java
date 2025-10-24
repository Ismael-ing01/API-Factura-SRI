package com.factura.sri.repository;

import com.factura.sri.model.DocumentoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoClienteRepository extends JpaRepository<DocumentoCliente, Long> {
    List<DocumentoCliente> findByClienteId(Long clienteId);
    @Query("SELECT d FROM DocumentoCliente d JOIN FETCH d.cliente")
    List<DocumentoCliente> findAllConCliente();

}
