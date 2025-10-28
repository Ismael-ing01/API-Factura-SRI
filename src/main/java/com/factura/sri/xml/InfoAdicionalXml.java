package com.factura.sri.xml;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class InfoAdicionalXml {
    @XmlElement(name = "campoAdicional")
    private List<CampoAdicionalXml> campoAdicional;
}

