package com.factura.sri.repository;

import com.factura.sri.model.FormaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormaPagoRepository extends JpaRepository<FormaPago, Long> {
}
