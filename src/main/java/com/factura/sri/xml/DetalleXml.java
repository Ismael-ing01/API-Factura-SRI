package com.factura.sri.xml;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

// Define el orden de los elementos
@XmlType(propOrder = {"codigoPrincipal", "codigoAuxiliar", "descripcion", "cantidad",
        "precioUnitario", "descuento", "precioTotalSinImpuesto", "impuestos"})
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class DetalleXml {
    private String codigoPrincipal; // Código del producto
    private String codigoAuxiliar; // Opcional
    private String descripcion;
    private String cantidad; // Hasta 6 decimales si usas versión 1.1.0
    private String precioUnitario; // Hasta 6 decimales si usas versión 1.1.0
    private String descuento; // Formato numérico
    private String precioTotalSinImpuesto; // Formato numérico (cantidad * precioUnitario - descuento)

    @XmlElementWrapper(name="impuestos")
    @XmlElement(name="impuesto")
    private List<ImpuestoDetalleXml> impuestos;

    // Aquí iría @XmlElementWrapper(name="detallesAdicionales") si los necesitas
}

