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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class FacturaService {

    // --- Repositorios Necesarios ---
    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    // Aunque ItemFactura se guarda en cascada, podríamos necesitarlo para búsquedas futuras.
    private final ItemFacturaRepository itemFacturaRepository;

    // --- Constructor para Inyección de Dependencias ---
    public FacturaService(FacturaRepository facturaRepository,
                          ClienteRepository clienteRepository,
                          ProductoRepository productoRepository,
                          ItemFacturaRepository itemFacturaRepository) {
        this.facturaRepository = facturaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.itemFacturaRepository = itemFacturaRepository;
    }

    // --- 2. LEER DATOS DEL EMISOR DESDE CONFIGURACIÓN ---
    // Puedes poner estos valores en tu archivo application.properties/yml
    @Value("${sri.ruc.emisor}") // ej: sri.ruc.emisor=17XXXXXXXXX001
    private String rucEmisor;
    @Value("${sri.ambiente}")   // ej: sri.ambiente=1 (Pruebas) o 2 (Producción)
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
     * Recibe los datos de entrada (DTO), realiza cálculos, actualiza stock y guarda.
     * @Transactional asegura que todas las operaciones (guardar factura, items, actualizar stock)
     * ocurran como una sola unidad. Si algo falla, todo se deshace (rollback).
     */

    @Transactional
    public FacturaResponseDTO crearFactura(FacturaRequestDTO requestDTO) {

        // --- 1. Validar Cliente ---
        Cliente cliente = clienteRepository.findById(requestDTO.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + requestDTO.getClienteId()));

        // --- 2. Crear la Entidad Factura (Cabecera) ---
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setEstadoSri(EstadoSri.GENERADA);

        String establecimiento = "001"; // Ejemplo
        String puntoEmision = "001";    // Ejemplo
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
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + itemDto.getProductoId()));

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

        // --- 7. Guardar la Factura (y sus Items en cascada) ---
        Factura facturaGuardada = facturaRepository.save(factura); // Ahora se guarda CON la claveAcceso

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


    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> listarTodasLasFacturas() {
        // 1. Obtiene todas las entidades Factura
        List<Factura> facturas = facturaRepository.findAll();

        // 2. Convierte cada entidad a su DTO correspondiente
        return facturas.stream()
                .map(this::convertirFacturaA_DTO) // Reutiliza el método de conversión
                .collect(Collectors.toList());
    }
}