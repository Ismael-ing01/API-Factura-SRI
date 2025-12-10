package com.factura.sri.repository;

import com.factura.sri.model.MovimientoPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoPrecioRepository extends JpaRepository<MovimientoPrecio, Long> {
    List<MovimientoPrecio> findByProductoIdOrderByFechaDesc(Long productoId);
}
