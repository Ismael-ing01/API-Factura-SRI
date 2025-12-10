package com.factura.sri.service;

import org.springframework.beans.factory.annotation.Value;
import com.factura.sri.dto.*;
import com.factura.sri.model.*;
import com.factura.sri.enums.EstadoSri;
import com.factura.sri.enums.TipoCodigoImpuestoIva;
import com.factura.sri.repository.ClienteRepository;
import com.factura.sri.repository.FacturaRepository;
import com.factura.sri.repository.ItemFacturaRepository;
import com.factura.sri.repository.ProductoRepository;
import com.factura.sri.util.GeneradorClaveAcceso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ¡Muy importante!

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacturaService {

    // --- Repositorios Necesarios ---
    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final ItemFacturaRepository itemFacturaRepository;
    private final GeneradorXmlFacturaService generadorXmlFacturaService;

    // Nuevos Repositorios
    private final com.factura.sri.repository.SucursalRepository sucursalRepository;
    private final com.factura.sri.repository.CajaRepository cajaRepository;
    private final com.factura.sri.repository.MovimientoProductoRepository kdxRepository;
    private final com.factura.sri.repository.FormaPagoRepository formaPagoRepository;

    // --- Constructor para Inyección de Dependencias ---
    public FacturaService(FacturaRepository facturaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            ItemFacturaRepository itemFacturaRepository,
            GeneradorXmlFacturaService generadorXmlFacturaService,
            com.factura.sri.repository.SucursalRepository sucursalRepo,
            com.factura.sri.repository.CajaRepository cajaRepo,
            com.factura.sri.repository.MovimientoProductoRepository kdxRepo,
            com.factura.sri.repository.FormaPagoRepository fpRepo) {
        this.facturaRepository = facturaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.itemFacturaRepository = itemFacturaRepository;
        this.generadorXmlFacturaService = generadorXmlFacturaService;
        this.sucursalRepository = sucursalRepo;
        this.cajaRepository = cajaRepo;
        this.kdxRepository = kdxRepo;
        this.formaPagoRepository = fpRepo;
    }

    // --- 2. LEER DATOS DEL EMISOR DESDE CONFIGURACIÓN ---
    // Puedes poner estos valores en tu archivo application.properties/yml
    @Value("${sri.ruc.emisor}") // ej: sri.ruc.emisor=17XXXXXXXXX001
    private String rucEmisor;
    @Value("${sri.ambiente}") // ej: sri.ambiente=1 (Pruebas) o 2 (Producción)
    private String tipoAmbiente;

    private String generarNumeroFactura(String establecimiento, String puntoEmision) {
        // 1. Busca el último secuencial usado en la BD para esta serie
        Long maxSecuencial = facturaRepository
                .findMaxSecuencialByEstablecimientoAndPuntoEmision(establecimiento, puntoEmision)
                .orElse(0L); // Si no encuentra ninguno, empieza en 0

        // 2. Calcula el siguiente secuencial
        long nuevoSecuencial = maxSecuencial + 1;

        // 3. Formatea el número completo
        return String.format("%s-%s-%09d", establecimiento, puntoEmision, nuevoSecuencial);
    }

    /**
     * Método Principal: Crea una nueva factura.
     * Recibe los datos de entrada (DTO), realiza cálculos, actualiza stock y
     * guarda.
     * 
     * @Transactional asegura que todas las operaciones (guardar factura, items,
     *                actualizar stock)
     *                ocurran como una sola unidad. Si algo falla, todo se deshace
     *                (rollback).
     */

    @Transactional
    public FacturaResponseDTO crearFactura(FacturaRequestDTO requestDTO) {

        // --- 1. Validar Cliente ---
        Cliente cliente = clienteRepository.findById(requestDTO.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + requestDTO.getClienteId()));

        // --- 1.1 Validar Sucursal y Caja ---
        com.factura.sri.model.Sucursal sucursal = sucursalRepository.findById(requestDTO.getSucursalId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        com.factura.sri.model.Caja caja = cajaRepository.findById(requestDTO.getCajaId())
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        // --- 2. Crear la Entidad Factura (Cabecera) ---
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setSucursal(sucursal);
        factura.setCaja(caja);
        factura.setEstadoSri(EstadoSri.GENERADA);

        String establecimiento = sucursal.getCodigo();
        String puntoEmision = caja.getPuntoEmision();
        factura.setFechaEmision(LocalDateTime.now());
        factura.setNumeroFactura(generarNumeroFactura(establecimiento, puntoEmision));

        // --- 3. GENERAR Y ASIGNAR CLAVE DE ACCESO --- <<< ¡NUEVA LÍNEA!
        // Llamamos a la clase estática para generar la clave
        String claveAccesoGenerada = GeneradorClaveAcceso.generarClaveAcceso(factura, rucEmisor, tipoAmbiente);
        factura.setClaveAcceso(claveAccesoGenerada); // Guardamos la clave en la entidad

        // --- 4. Inicializar Acumuladores para Totales --- (Sin cambios)
        double subtotalSinImpuestos = 0.0;
        double subtotalIvaGeneral = 0.0;
        double subtotalIva0 = 0.0;
        double valorTotalIva = 0.0;

        // --- 5. Procesar cada Ítem de la Solicitud --- (Sin cambios)
        List<ItemFactura> itemsFactura = new ArrayList<>();
        for (ItemRequestDTO itemDto : requestDTO.getItems()) {

            Producto producto = productoRepository.findById(itemDto.getProductoId())
                    .orElseThrow(
                            () -> new RuntimeException("Producto no encontrado con ID: " + itemDto.getProductoId()));

            if (producto.getStock() < itemDto.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre()
                        + ". Stock actual: " + producto.getStock() + ", Solicitado: " + itemDto.getCantidad());
            }

            ItemFactura item = new ItemFactura();
            item.setProducto(producto);
            item.setCantidad(itemDto.getCantidad());
            item.setPrecioUnitario(producto.getPrecio());

            double subtotalItem = item.getCantidad() * item.getPrecioUnitario();
            double ivaItem = 0.0;
            double tarifaIvaActual = 0.0;

            switch (producto.getTipoImpuestoIva()) {
                case IVA_0:
                    item.setCodigoImpuestoIva(TipoCodigoImpuestoIva.IVA_0);
                    item.setTarifaIva(0.0);
                    subtotalIva0 += subtotalItem;
                    break;
                case IVA_GENERAL:
                    item.setCodigoImpuestoIva(TipoCodigoImpuestoIva.IVA_GENERAL);
                    tarifaIvaActual = 15.0; // <<< RECUERDA: Obtener esto dinámicamente
                    item.setTarifaIva(tarifaIvaActual);
                    ivaItem = subtotalItem * (tarifaIvaActual / 100.0);
                    subtotalIvaGeneral += subtotalItem;
                    break;
            }

            item.setSubtotal(subtotalItem);
            item.setValorIva(ivaItem);

            subtotalSinImpuestos += subtotalItem;
            valorTotalIva += ivaItem;

            producto.setStock(producto.getStock() - itemDto.getCantidad());

            factura.addItem(item);
            itemsFactura.add(item);
        }

        // --- 6. Establecer Totales Calculados en la Factura --- (Sin cambios)
        factura.setSubtotalSinImpuestos(subtotalSinImpuestos);
        factura.setSubtotalIva(subtotalIvaGeneral);
        factura.setSubtotalIva0(subtotalIva0);
        factura.setValorIva(valorTotalIva);
        factura.setTotal(subtotalSinImpuestos + valorTotalIva);

        // --- 6.1 Procesar Pagos ---
        if (requestDTO.getPagos() != null && !requestDTO.getPagos().isEmpty()) {
            for (FacturaPagoDTO pagoDTO : requestDTO.getPagos()) {
                com.factura.sri.model.FormaPago fp = formaPagoRepository.findById(pagoDTO.getFormaPagoId())
                        .orElseThrow(() -> new RuntimeException("Forma de Pago no encontrada"));

                com.factura.sri.model.FacturaPago pago = new com.factura.sri.model.FacturaPago();
                pago.setFormaPago(fp);
                pago.setTotal(pagoDTO.getTotal());
                pago.setPlazo(pagoDTO.getPlazo());
                pago.setUnidadTiempo(pagoDTO.getUnidadTiempo());
                factura.addPago(pago);
            }
        }

        // --- 6.2 Procesar Info Adicional ---
        if (requestDTO.getInfoAdicional() != null) {
            for (FacturaCampoAdicionalDTO infoDTO : requestDTO.getInfoAdicional()) {
                com.factura.sri.model.FacturaCampoAdicional campo = new com.factura.sri.model.FacturaCampoAdicional();
                campo.setNombre(infoDTO.getNombre());
                campo.setValor(infoDTO.getValor());
                factura.addCampoAdicional(campo);
            }
        }

        // --- 7. Guardar la Factura (y sus Items en cascada) ---
        Factura facturaGuardada = facturaRepository.save(factura); // Ahora se guarda CON la claveAcceso

        // --- 7.1 Registrar KARDEX (Salida de Inventario) ---
        for (ItemFactura item : facturaGuardada.getItems()) {
            Producto prod = item.getProducto();

            com.factura.sri.model.MovimientoProducto mp = new com.factura.sri.model.MovimientoProducto();
            mp.setFecha(LocalDateTime.now());
            mp.setTipoMovimiento("VENTA");
            mp.setCantidad(-item.getCantidad()); // Salida es negativa
            mp.setCostoUnitario(prod.getPrecioCompra()); // Costo promedio o actual
            mp.setSaldoStock(prod.getStock()); // El stock ya fue descontado en el bucle anterior (L147)
            mp.setReferencia("Factura: " + facturaGuardada.getNumeroFactura());
            mp.setProducto(prod);
            kdxRepository.save(mp);
        }

        // --- 8. Convertir a DTO de Respuesta y Devolver ---
        return convertirFacturaA_DTO(facturaGuardada); // Asegúrate que este método incluya la clave si quieres verla
    }

    // --- Método Privado para Convertir Entidad a DTO de Respuesta ---
    private FacturaResponseDTO convertirFacturaA_DTO(Factura factura) {
        FacturaResponseDTO dto = new FacturaResponseDTO();
        dto.setId(factura.getId());
        dto.setNumeroFactura(factura.getNumeroFactura());
        dto.setFechaEmision(factura.getFechaEmision());
        dto.setEstadoSri(factura.getEstadoSri());

        // Datos del cliente (Accede mientras la sesión está abierta)
        dto.setClienteId(factura.getCliente().getId());
        dto.setClienteNombre(factura.getCliente().getNombres() + " " + factura.getCliente().getApellidos());

        // Totales
        dto.setSubtotalSinImpuestos(factura.getSubtotalSinImpuestos());
        dto.setSubtotalIva(factura.getSubtotalIva());
        dto.setSubtotalIva0(factura.getSubtotalIva0());
        dto.setValorIva(factura.getValorIva());
        dto.setTotal(factura.getTotal());

        // Convertir Items
        List<ItemResponseDTO> itemDTOs = new ArrayList<>();
        for (ItemFactura item : factura.getItems()) {
            ItemResponseDTO itemDto = new ItemResponseDTO();
            itemDto.setId(item.getId());
            itemDto.setCantidad(item.getCantidad());
            itemDto.setPrecioUnitario(item.getPrecioUnitario());
            itemDto.setSubtotal(item.getSubtotal());
            itemDto.setValorIva(item.getValorIva());
            // Datos del producto (Accede mientras la sesión está abierta)
            itemDto.setNombreProducto(item.getProducto().getNombre());
            itemDTOs.add(itemDto);
        }
        dto.setItems(itemDTOs);

        return dto;
    }

    // --- Otros Métodos (Buscar, Listar, etc. - se implementarán después) ---

    @Transactional(readOnly = true) // Optimiza la transacción para solo lectura
    public FacturaResponseDTO buscarFacturaPorId(Long id) {
        // 1. Busca la entidad Factura por ID
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + id));

        // 2. Llama al método helper para convertirla a DTO
        // ¡Importante! Esto carga los 'items' y datos relacionados
        // mientras la transacción está abierta, evitando LazyInitializationException.
        return convertirFacturaA_DTO(factura);
    }

    /**
     * Busca una entidad Factura por su ID.
     * Usado internamente o por otros servicios que necesiten la entidad completa.
     * ¡Importante! Asegura que la transacción esté activa para cargar relaciones
     * LAZY si es necesario.
     */
    @Transactional(readOnly = true) // Necesario para cargar posibles relaciones LAZY
    public Factura buscarEntidadFacturaPorId(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + id));
        // Forzar carga de items si son LAZY y se necesitan fuera (opcional, depende de
        // tu generador)
        // factura.getItems().size(); // Ejemplo para forzar carga
        return factura;
    }

    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> listarTodasLasFacturas() {
        // 1. Obtiene todas las entidades Factura
        List<Factura> facturas = facturaRepository.findAll();

        // 2. Convierte cada entidad a su DTO correspondiente
        return facturas.stream()
                .map(this::convertirFacturaA_DTO) // Reutiliza el método de conversión
                .collect(Collectors.toList());
    }

    /**
     * Busca una factura por ID y genera su representación XML.
     * Toda la operación ocurre dentro de una transacción para permitir
     * la carga de asociaciones LAZY necesarias para el XML.
     *
     * @param id El ID de la factura a buscar.
     * @return El String XML de la factura.
     * @throws RuntimeException Si la factura no se encuentra o hay error al generar
     *                          XML.
     */
    @Transactional(readOnly = true) // La transacción mantiene la sesión abierta
    public String generarXmlParaFactura(Long id) {
        // 1. Busca la entidad Factura
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + id));

        // 2. Llama al servicio generador MIENTRAS LA TRANSACCIÓN ESTÁ ACTIVA
        // Ahora, cuando el generador acceda a
        // factura.getCliente().getDocumentoClientes(),
        // Hibernate podrá ir a la base de datos porque la sesión sigue abierta.
        return generadorXmlFacturaService.generarXml(factura);
    }
}