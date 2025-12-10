package com.factura.sri.repository;

import com.factura.sri.model.FacturaCampoAdicional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaCampoAdicionalRepository extends JpaRepository<FacturaCampoAdicional, Long> {
}
