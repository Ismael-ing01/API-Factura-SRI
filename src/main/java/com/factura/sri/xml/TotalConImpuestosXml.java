package com.factura.sri.xml; // Asegúrate que el paquete sea correcto

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class TotalConImpuestosXml { // <-- CLASE PUBLIC

    @XmlElement(name = "totalImpuesto")
    private List<TotalImpuestoXml> totalImpuesto;
}