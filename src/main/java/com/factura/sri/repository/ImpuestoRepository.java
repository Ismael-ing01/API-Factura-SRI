package com.factura.sri.repository;

import com.factura.sri.model.Impuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImpuestoRepository extends JpaRepository<Impuesto, Long> {
}
