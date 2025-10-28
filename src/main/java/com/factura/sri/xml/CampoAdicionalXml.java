package com.factura.sri.xml; // Asegúrate que el paquete sea correcto

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class CampoAdicionalXml { // <-- CLASE PUBLIC

    @XmlAttribute
    private String nombre;

    @XmlValue // El texto dentro de la etiqueta <campoAdicional>
    private String value;
}