package com.factura.sri.xml;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

// Define el orden de los elementos
@XmlType(propOrder = {"fechaEmision", "dirEstablecimiento", "contribuyenteEspecial", "obligadoContabilidad",
        "tipoIdentificacionComprador", "razonSocialComprador", "identificacionComprador", "direccionComprador",
        "totalSinImpuestos", "totalDescuento", "totalConImpuestos", "propina", "importeTotal", "moneda", "pagos"})
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class InfoFacturaXml {
    private String fechaEmision; // Formato dd/MM/yyyy
    private String dirEstablecimiento; // Opcional si es igual a dirMatriz
    private String contribuyenteEspecial; // Opcional
    private String obligadoContabilidad; // SI o NO, opcional
    private String tipoIdentificacionComprador; // Código 04, 05, 06, 07, 08
    private String razonSocialComprador;
    private String identificacionComprador;
    private String direccionComprador; // Opcional
    private String totalSinImpuestos; // Formato numérico con punto decimal, ej: "100.00"
    private String totalDescuento;    // Formato numérico
    private TotalConImpuestosXml totalConImpuestos;
    private String propina = "0.00"; // Generalmente 0.00
    private String importeTotal;      // Formato numérico
    private String moneda = "DOLAR"; // Generalmente DOLAR

    @XmlElementWrapper(name="pagos")
    @XmlElement(name="pago")
    private List<PagoXml> pagos; // Opcional, pero recomendado
}

