package com.factura.sri.service;

import com.factura.sri.model.DocumentoCliente;
import com.factura.sri.model.Factura;
import com.factura.sri.model.ItemFactura;
import com.factura.sri.xml.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class GeneradorXmlFacturaService {

    // --- Formateadores ---
    // Formato de fecha requerido por el SRI en el XML: dd/MM/yyyy
    private static final DateTimeFormatter XML_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Locale US para asegurar el punto como separador decimal
    private static final Locale LOCALE_US = Locale.US;

    // --- Datos del Emisor (inyectados desde application.properties) ---
    // --- Datos del Emisor (inyectados desde application.properties) ---
    // @Value("${sri.ruc.emisor}") ... YA NO SE USAN, SE TOMAN DE LA ENTIDAD EMPRESA
    // Mantenemos solo por si acaso, pero la lógica ahora usa la Entidad.

    /**
     * Método principal para generar el XML de una factura.
     *
     * @param factura La entidad Factura (ya guardada y con todos los datos).
     * @return Un String que contiene el XML de la factura formateado.
     * @throws RuntimeException Si ocurre un error durante la generación del XML.
     */
    public String generarXml(Factura factura) {
        try {
            // 1. Crear el objeto raíz del XML
            FacturaXml facturaXml = new FacturaXml();
            // facturaXml.setVersion("1.1.0"); // O la versión que estés usando

            // 2. Poblar InfoTributaria
            facturaXml.setInfoTributaria(crearInfoTributaria(factura));

            // 3. Poblar InfoFactura
            facturaXml.setInfoFactura(crearInfoFactura(factura));

            // 4. Poblar Detalles
            facturaXml.setDetalles(crearDetalles(factura));

            // 5. (Opcional) Poblar InfoAdicional
            // facturaXml.setInfoAdicional(crearInfoAdicional(factura));

            // 6. Convertir el objeto Java a XML usando JAXB
            JAXBContext context = JAXBContext.newInstance(FacturaXml.class);
            Marshaller marshaller = context.createMarshaller();

            // Configurar JAXB para formato legible y sin la declaración XML estándar
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // Para que el XML sea indentado y
                                                                                    // legible
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // Para omitir la declaración <?xml ...?>
                                                                            // automática de JAXB

            // --- CORRECCIÓN AQUÍ ---
            // Usar la constante estándar de JAXB para la codificación
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            // ---------------------

            StringWriter sw = new StringWriter();
            // Añadir la declaración XML manualmente ya que usamos JAXB_FRAGMENT
            sw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            marshaller.marshal(facturaXml, sw); // Convierte el objeto Java a XML y lo escribe en sw

            return sw.toString(); // Devuelve el XML como un String

        } catch (JAXBException e) {
            // Manejar el error adecuadamente (log, excepción específica)
            throw new RuntimeException("Error al generar el XML de la factura: " + e.getMessage(), e);
        }
    }

    // --- Métodos Helper para poblar cada sección ---

    private InfoTributariaXml crearInfoTributaria(Factura factura) {
        InfoTributariaXml info = new InfoTributariaXml();

        com.factura.sri.model.Empresa empresa = factura.getSucursal().getEmpresa();
        com.factura.sri.model.Sucursal sucursal = factura.getSucursal();
        com.factura.sri.model.Caja caja = factura.getCaja();

        // info.setAmbiente(empresa.getAmbienteSri().toString()); // TOOD: Asegurar tipo
        // string
        info.setAmbiente(String.valueOf(empresa.getAmbienteSri()));
        info.setTipoEmision("1"); // Emisión Normal
        info.setRazonSocial(empresa.getRazonSocial());
        info.setNombreComercial(empresa.getNombreComercial());
        info.setRuc(empresa.getRuc());
        info.setClaveAcceso(factura.getClaveAcceso());
        info.setCodDoc("01"); // Factura

        info.setEstab(sucursal.getCodigo());
        info.setPtoEmi(caja.getPuntoEmision());

        // Extraer secuencial del numeroFactura (001-001-000000001)
        String[] partesNum = factura.getNumeroFactura().split("-");
        info.setSecuencial(partesNum[2]);

        info.setDirMatriz(empresa.getDireccionMatriz());

        return info;
    }

    private InfoFacturaXml crearInfoFactura(Factura factura) {
        InfoFacturaXml info = new InfoFacturaXml();

        com.factura.sri.model.Empresa empresa = factura.getSucursal().getEmpresa();

        info.setFechaEmision(factura.getFechaEmision().format(XML_DATE_FORMATTER));
        info.setDirEstablecimiento(factura.getSucursal().getDireccion());
        info.setContribuyenteEspecial(empresa.getContribuyenteEspecial());
        info.setObligadoContabilidad(empresa.getObligadoContabilidad());

        // Datos del Comprador
        DocumentoCliente docCliente = factura.getCliente().getDocumentoClientes().get(0);

        info.setTipoIdentificacionComprador(docCliente.getTipoDocumento().getCodigoSri());
        info.setRazonSocialComprador(factura.getCliente().getNombres() + " " + factura.getCliente().getApellidos());
        info.setIdentificacionComprador(docCliente.getNumeroDocumento());
        info.setDireccionComprador(factura.getCliente().getDireccion());

        // Totales (Formateados a String con 2 decimales y punto)
        info.setTotalSinImpuestos(String.format(LOCALE_US, "%.2f", factura.getSubtotalSinImpuestos()));
        info.setTotalDescuento("0.00");
        info.setImporteTotal(String.format(LOCALE_US, "%.2f", factura.getTotal()));
        info.setMoneda("DOLAR");
        info.setPropina("0.00");

        // Detalle de Impuestos en Totales
        List<TotalImpuestoXml> totalImpuestos = new ArrayList<>();
        // IVA 0%
        if (factura.getSubtotalIva0() > 0) {
            TotalImpuestoXml imp0 = new TotalImpuestoXml();
            imp0.setCodigo("2");
            imp0.setCodigoPorcentaje("0");
            imp0.setBaseImponible(String.format(LOCALE_US, "%.2f", factura.getSubtotalIva0()));
            imp0.setValor("0.00");
            totalImpuestos.add(imp0);
        }
        // IVA Tarifa General
        if (factura.getSubtotalIva() > 0) {
            TotalImpuestoXml impGen = new TotalImpuestoXml();
            impGen.setCodigo("2");
            String codigoPorcentajeIvaGeneral = "4"; // AJUSTAR SEGÚN TARIFA USADA
            impGen.setCodigoPorcentaje(codigoPorcentajeIvaGeneral);
            impGen.setBaseImponible(String.format(LOCALE_US, "%.2f", factura.getSubtotalIva()));
            impGen.setValor(String.format(LOCALE_US, "%.2f", factura.getValorIva()));
            totalImpuestos.add(impGen);
        }

        TotalConImpuestosXml totalConImpuestosXml = new TotalConImpuestosXml();
        totalConImpuestosXml.setTotalImpuesto(totalImpuestos);
        info.setTotalConImpuestos(totalConImpuestosXml);

        // Pagos
        List<PagoXml> pagosXml = new ArrayList<>();
        if (factura.getPagos() != null && !factura.getPagos().isEmpty()) {
            for (com.factura.sri.model.FacturaPago pago : factura.getPagos()) {
                PagoXml pXml = new PagoXml();
                pXml.setFormaPago(pago.getFormaPago().getCodigo()); // Ej: 01, 20
                pXml.setTotal(String.format(LOCALE_US, "%.2f", pago.getTotal()));
                if (pago.getPlazo() != null && pago.getPlazo() > 0) {
                    pXml.setPlazo(pago.getPlazo().toString());
                    pXml.setUnidadTiempo(pago.getUnidadTiempo());
                }
                pagosXml.add(pXml);
            }
        }
        info.setPagos(pagosXml);

        return info;
    }

    private List<DetalleXml> crearDetalles(Factura factura) {
        List<DetalleXml> detalles = new ArrayList<>();
        for (ItemFactura item : factura.getItems()) {
            DetalleXml detalle = new DetalleXml();
            // Asume que Producto tiene un campo 'codigo' o usa el ID
            detalle.setCodigoPrincipal(item.getProducto().getId().toString()); // O usa un código de producto si lo
                                                                               // tienes
            detalle.setDescripcion(item.getProducto().getNombre());
            // Formatear cantidad y precio unitario (hasta 6 decimales permitidos en v1.1.0)
            detalle.setCantidad(String.format(LOCALE_US, "%.6f", (double) item.getCantidad()));
            detalle.setPrecioUnitario(String.format(LOCALE_US, "%.6f", item.getPrecioUnitario()));
            detalle.setDescuento("0.00"); // Asumiendo sin descuentos por línea
            detalle.setPrecioTotalSinImpuesto(String.format(LOCALE_US, "%.2f", item.getSubtotal()));

            // Impuestos del detalle
            List<ImpuestoDetalleXml> impuestosDetalle = new ArrayList<>();
            ImpuestoDetalleXml impDet = new ImpuestoDetalleXml();
            impDet.setCodigo("2"); // Código IVA

            // Determinar códigoPorcentaje y tarifa basado en el item
            String codigoPorcentajeDetalle;
            switch (item.getCodigoImpuestoIva()) {
                case IVA_0:
                    codigoPorcentajeDetalle = "0"; // Código Tarifa 0%
                    break;
                case IVA_GENERAL:
                    // Necesitas el código correcto para la tarifa usada (12, 15, etc.)
                    // Asumiendo 15% (código 4) de nuevo
                    codigoPorcentajeDetalle = "4"; // AJUSTAR SEGÚN TARIFA USADA
                    break;
                default: // NO_OBJETO u otros
                    codigoPorcentajeDetalle = "6"; // Código No Objeto de Impuesto (ajustar si es necesario)
            }
            impDet.setCodigoPorcentaje(codigoPorcentajeDetalle);
            impDet.setTarifa(String.format(LOCALE_US, "%.2f", item.getTarifaIva())); // El porcentaje
            impDet.setBaseImponible(String.format(LOCALE_US, "%.2f", item.getSubtotal()));
            impDet.setValor(String.format(LOCALE_US, "%.2f", item.getValorIva()));
            impuestosDetalle.add(impDet);
            // Añadir lógica para ICE o IRBPNR si aplican al item aquí

            detalle.setImpuestos(impuestosDetalle);
            detalles.add(detalle);
        }
        return detalles;
    }

    // Método opcional para información adicional
    /*
     * private InfoAdicionalXml crearInfoAdicional(Factura factura) {
     * InfoAdicionalXml info = new InfoAdicionalXml();
     * List<CampoAdicionalXml> campos = new ArrayList<>();
     * 
     * CampoAdicionalXml email = new CampoAdicionalXml();
     * email.setNombre("Email");
     * email.setValue(factura.getCliente().getEmail()); // Asumiendo que Cliente
     * tiene email
     * campos.add(email);
     * 
     * // Añadir más campos si es necesario
     * 
     * if (!campos.isEmpty()) {
     * info.setCampoAdicional(campos);
     * return info;
     * }
     * return null; // No añadir la sección si no hay campos
     * }
     */
}