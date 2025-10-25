package com.factura.sri.xml;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "factura") // Define el nombre del elemento raíz XML
@XmlAccessorType(XmlAccessType.FIELD) // Mapea los campos directamente
@XmlType(propOrder = {"infoTributaria", "infoFactura", "detalles", "infoAdicional"}) // Define el orden de los elementos hijos
public class FacturaXml {

    @XmlAttribute // Mapea 'id' como un atributo del tag <factura>
    private String id = "comprobante";

    @XmlAttribute // Mapea 'version' como un atributo del tag <factura>
    private String version = "1.1.0"; // O "1.0.0" según necesites

    private InfoTributariaXml infoTributaria;
    private InfoFacturaXml infoFactura;

    @XmlElementWrapper(name="detalles") // Envuelve la lista en <detalles>
    @XmlElement(name="detalle")         // Cada elemento de la lista será <detalle>
    private List<DetalleXml> detalles;

    // Opcional, si necesitas información adicional
    private InfoAdicionalXml infoAdicional;

}