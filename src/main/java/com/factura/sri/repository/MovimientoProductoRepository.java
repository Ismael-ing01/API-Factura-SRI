package com.factura.sri.repository;

import com.factura.sri.model.MovimientoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoProductoRepository extends JpaRepository<MovimientoProducto, Long> {
    List<MovimientoProducto> findByProductoIdOrderByFechaDesc(Long productoId);
}
