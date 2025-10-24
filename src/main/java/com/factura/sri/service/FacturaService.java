package com.factura.sri.service;

import com.factura.sri.dto.*; // Importa todos tus DTOs
import com.factura.sri.model.*; // Importa todas tus entidades
import com.factura.sri.enums.EstadoSri;
import com.factura.sri.enums.TipoCodigoImpuestoIva;
import com.factura.sri.repository.ClienteRepository;
import com.factura.sri.repository.FacturaRepository;
import com.factura.sri.repository.ItemFacturaRepository; // Aunque no lo usemos directamente aquí, es bueno tenerlo
import com.factura.sri.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ¡Muy importante!

import java.time.LocalDateTime; // Para la fecha
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong; // Para generar número de factura (temporal)

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

    // --- Lógica para Generar Número de Factura (Temporal - necesita mejora) ---
    // Esto es solo un ejemplo simple. En producción, necesitarías un sistema
    // más robusto para generar secuenciales únicos por punto de emisión.
    private final AtomicLong secuenciaActual = new AtomicLong(0); // Inicia en 0
    private String generarNumeroFactura() {
        long nuevaSecuencia = secuenciaActual.incrementAndGet(); // Incrementa y obtiene el nuevo valor
        // Formato: 001 (establecimiento) - 001 (punto emision) - 000000001 (secuencial)
        return String.format("001-001-%09d", nuevaSecuencia);
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
        factura.setEstadoSri(EstadoSri.GENERADA); // Estado inicial
        factura.setNumeroFactura(generarNumeroFactura()); // Genera el número secuencial

        // --- 3. Inicializar Acumuladores para Totales ---
        double subtotalSinImpuestos = 0.0;
        double subtotalIvaGeneral = 0.0; // Base imponible para IVA General
        double subtotalIva0 = 0.0;       // Base imponible para IVA 0%
        double valorTotalIva = 0.0;      // Suma del IVA de todas las líneas

        // --- 4. Procesar cada Ítem de la Solicitud ---
        List<ItemFactura> itemsFactura = new ArrayList<>();
        for (ItemRequestDTO itemDto : requestDTO.getItems()) {

            // --- 4.1 Validar Producto y Stock ---
            Producto producto = productoRepository.findById(itemDto.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + itemDto.getProductoId()));

            if (producto.getStock() < itemDto.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre()
                        + ". Stock actual: " + producto.getStock() + ", Solicitado: " + itemDto.getCantidad());
            }

            // --- 4.2 Crear la Entidad ItemFactura ---
            ItemFactura item = new ItemFactura();
            item.setProducto(producto);
            item.setCantidad(itemDto.getCantidad());
            // ¡Importante! Guardar el precio actual del producto en el item
            item.setPrecioUnitario(producto.getPrecio());

            // --- 4.3 Calcular Valores e IVA del Item ---
            double subtotalItem = item.getCantidad() * item.getPrecioUnitario();
            double ivaItem = 0.0;
            double tarifaIvaActual = 0.0; // Porcentaje a aplicar (ej: 15.0)

            // Determinar tipo de IVA según el producto
            switch (producto.getTipoImpuestoIva()) {
                case IVA_0:
                    item.setCodigoImpuestoIva(TipoCodigoImpuestoIva.IVA_0);
                    item.setTarifaIva(0.0);
                    subtotalIva0 += subtotalItem; // Acumula en base IVA 0%
                    break;
                case IVA_GENERAL:
                    item.setCodigoImpuestoIva(TipoCodigoImpuestoIva.IVA_GENERAL);
                    // --- ¡IMPORTANTE! ---
                    // Aquí deberías obtener la tarifa de IVA vigente (ej: 15%).
                    // Puede venir de una configuración, base de datos, etc.
                    // Por ahora, usaremos 15.0 como ejemplo fijo.
                    tarifaIvaActual = 15.0; // CAMBIAR ESTO por un valor dinámico/configurable
                    // ---------------------
                    item.setTarifaIva(tarifaIvaActual);
                    ivaItem = subtotalItem * (tarifaIvaActual / 100.0);
                    subtotalIvaGeneral += subtotalItem; // Acumula en base IVA General
                    break;
                // Podrías añadir NO_OBJETO si lo necesitas
            }

            item.setSubtotal(subtotalItem);
            item.setValorIva(ivaItem);

            // --- 4.4 Acumular Totales Generales ---
            subtotalSinImpuestos += subtotalItem;
            valorTotalIva += ivaItem;

            // --- 4.5 Actualizar Stock del Producto ---
            producto.setStock(producto.getStock() - itemDto.getCantidad());
            // No es necesario llamar a productoRepository.save() aquí,
            // @Transactional y JPA se encargarán al final.

            // --- 4.6 Añadir Item a la Factura ---
            // Usamos el método helper para asegurar la relación bidireccional
            factura.addItem(item); // Esto también hace item.setFactura(factura);
            itemsFactura.add(item); // También podemos guardar en una lista local si la necesitamos después
        }

        // --- 5. Establecer Totales Calculados en la Factura ---
        factura.setSubtotalSinImpuestos(subtotalSinImpuestos);
        factura.setSubtotalIva(subtotalIvaGeneral);
        factura.setSubtotalIva0(subtotalIva0);
        factura.setValorIva(valorTotalIva);
        factura.setTotal(subtotalSinImpuestos + valorTotalIva); // Cálculo final

        // --- 6. Guardar la Factura (y sus Items en cascada) ---
        Factura facturaGuardada = facturaRepository.save(factura);

        // --- 7. Convertir a DTO de Respuesta y Devolver ---
        return convertirFacturaA_DTO(facturaGuardada);
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

}