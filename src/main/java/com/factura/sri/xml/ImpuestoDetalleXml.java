package com.factura.sri.xml; // Asegúrate que el paquete sea correcto

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@XmlType(propOrder = {"codigo", "codigoPorcentaje", "tarifa", "baseImponible", "valor"})
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class ImpuestoDetalleXml { // <-- CLASE PUBLIC
    private String codigo;
    private String codigoPorcentaje;
    private String tarifa;
    private String baseImponible;
    private String valor;
}