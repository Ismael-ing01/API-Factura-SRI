package com.factura.sri.repository;

import com.factura.sri.model.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {
    List<Caja> findBySucursalId(Long sucursalId);
}
