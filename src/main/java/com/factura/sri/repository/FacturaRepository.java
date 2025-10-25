package com.factura.sri.repository;

import com.factura.sri.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    /**
     * Busca el número de factura más alto para un establecimiento y punto de emisión dados.
     * Extrae la parte secuencial (los últimos 9 dígitos) y devuelve el máximo como número.
     * @param establecimiento El código del establecimiento (ej: "001")
     * @param puntoEmision El código del punto de emisión (ej: "001")
     * @return Un Optional<Long> con el máximo secuencial encontrado, o vacío si no hay facturas para esa serie.
     */
    @Query("SELECT MAX(CAST(SUBSTRING(f.numeroFactura, 9, 9) AS long)) " +
            "FROM Factura f " +
            "WHERE SUBSTRING(f.numeroFactura, 1, 3) = :establecimiento " +
            "AND SUBSTRING(f.numeroFactura, 5, 3) = :puntoEmision")
    Optional<Long> findMaxSecuencialByEstablecimientoAndPuntoEmision(
            @Param("establecimiento") String establecimiento,
            @Param("puntoEmision") String puntoEmision);
}
