package com.factura.sri.xml; // Asegúrate que el paquete sea correcto

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@XmlType(propOrder = {"formaPago", "total", "plazo", "unidadTiempo"})
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class PagoXml { // <-- CLASE PUBLIC
    private String formaPago;
    private String total;
    private String plazo;
    private String unidadTiempo;
}
