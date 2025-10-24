package com.factura.sri.repository;

import com.factura.sri.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCategoriaId(Long categoriaId);
}
