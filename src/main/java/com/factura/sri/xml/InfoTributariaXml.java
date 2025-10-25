package com.factura.sri.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

// Define el orden exacto de los elementos según el SRI
@XmlType(propOrder = {"ambiente", "tipoEmision", "razonSocial", "nombreComercial",
        "ruc", "claveAcceso", "codDoc", "estab", "ptoEmi", "secuencial", "dirMatriz"})
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class InfoTributariaXml {
    private String ambiente;
    private String tipoEmision;
    private String razonSocial;
    private String nombreComercial; // Opcional
    private String ruc;
    private String claveAcceso;
    private String codDoc; // "01" para factura
    private String estab;
    private String ptoEmi;
    private String secuencial;
    private String dirMatriz;
    // Podrían faltar campos si eres Agente de Retención o RIMPE, se añaden aquí
}