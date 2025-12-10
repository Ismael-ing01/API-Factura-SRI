package com.factura.sri.repository;

import com.factura.sri.model.FacturaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaPagoRepository extends JpaRepository<FacturaPago, Long> {
}
