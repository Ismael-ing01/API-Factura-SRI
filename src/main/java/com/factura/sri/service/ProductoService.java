package com.factura.sri.service;

import com.factura.sri.dto.CategoriaDTO;
import com.factura.sri.dto.ProductoDTO;
import com.factura.sri.dto.ProductoResponseDTO;
import com.factura.sri.model.Categoria;
import com.factura.sri.model.Producto;
import com.factura.sri.repository.CategoriaRepository;
import com.factura.sri.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final com.factura.sri.repository.MovimientoProductoRepository kdxRepository;
    private final com.factura.sri.repository.MovimientoPrecioRepository precioRepository;
    private final com.factura.sri.repository.ProductoImagenRepository imagenRepository;

    public ProductoService(ProductoRepository pRepo, CategoriaRepository cRepo,
            com.factura.sri.repository.MovimientoProductoRepository kRepo,
            com.factura.sri.repository.MovimientoPrecioRepository prRepo,
            com.factura.sri.repository.ProductoImagenRepository imgRepo) {
        this.productoRepository = pRepo;
        this.categoriaRepository = cRepo;
        this.kdxRepository = kRepo;
        this.precioRepository = prRepo;
        this.imagenRepository = imgRepo;
    }

    /**
     * MÉTODO PARA CREAR (USA LA "COMANDA")
     * Recibe el DTO de entrada (ProductoDTO).
     */
    @Transactional
    public Producto guardarProducto(ProductoDTO productoDTO) {
        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Producto producto = new Producto();
        producto.setNombre(productoDTO.getNombre());
        producto.setStock(productoDTO.getStock());
        producto.setCategoria(categoria);
        producto.setTipoImpuestoIva(productoDTO.getTipoImpuestoIva());

        // Logic de Precios
        producto.setPrecioCompra(productoDTO.getPrecioCompra() != null ? productoDTO.getPrecioCompra() : 0.0);
        producto.setMargenUtilidad(productoDTO.getMargenUtilidad() != null ? productoDTO.getMargenUtilidad() : 0.0);

        // Si viene el precio venta manual, usalo, sino calcúlalo
        if (productoDTO.getPrecio() != null) {
            producto.setPrecio(productoDTO.getPrecio());
        } else {
            producto.calcularPrecioVenta(); // Usa costo y margen
        }

        Producto saved = productoRepository.save(producto);

        // Guardar Imagenes
        if (productoDTO.getImagenes() != null) {
            for (String url : productoDTO.getImagenes()) {
                com.factura.sri.model.ProductoImagen img = new com.factura.sri.model.ProductoImagen();
                img.setUrl(url);
                img.setProducto(saved);
                imagenRepository.save(img);
            }
        }

        // Registrar Inventario Inicial (Kardex)
        if (saved.getStock() > 0) {
            com.factura.sri.model.MovimientoProducto mp = new com.factura.sri.model.MovimientoProducto();
            mp.setFecha(java.time.LocalDateTime.now());
            mp.setTipoMovimiento("INVENTARIO_INICIAL");
            mp.setCantidad(saved.getStock());
            mp.setCostoUnitario(saved.getPrecioCompra());
            mp.setSaldoStock(saved.getStock());
            mp.setReferencia("Carga Inicial");
            mp.setProducto(saved);
            kdxRepository.save(mp);
        }

        // Registrar Precio Inicial
        com.factura.sri.model.MovimientoPrecio mpr = new com.factura.sri.model.MovimientoPrecio();
        mpr.setFecha(java.time.LocalDateTime.now());
        mpr.setPrecioAnterior(0.0);
        mpr.setPrecioNuevo(saved.getPrecio());
        mpr.setMargenAnterior(0.0);
        mpr.setMargenNuevo(saved.getMargenUtilidad());
        mpr.setProducto(saved);
        precioRepository.save(mpr);

        return saved;
    }

    /**
     * MÉTODO PARA LISTAR (DEVUELVE EL "PLATO SERVIDO")
     * Devuelve una lista del DTO de salida (ProductoResponseDTO).
     */
    @Transactional(readOnly = true) // Transacción de solo lectura (es más rápido)
    public List<ProductoResponseDTO> buscarTodos() {
        // 1. Obtiene todas las entidades Producto de la DB.
        List<Producto> productos = productoRepository.findAll();

        // 2. Convierte cada Entidad al DTO de respuesta.
        return productos.stream()
                .map(this::convertirEntidadADTO) // Llama al método de abajo
                .collect(Collectors.toList());
    }

    /**
     * Busca un solo producto por ID y lo devuelve como un DTO de respuesta.
     */
    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorId(Long id) {
        // 1. Busca la entidad
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // 2. Convierte la entidad al DTO de "plato servido" y lo devuelve
        return convertirEntidadADTO(producto);
    }

    /**
     * Actualiza un producto. Recibe la "comanda" (ProductoDTO).
     * Devuelve el producto actualizado como un DTO de "plato servido".
     */
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoDTO productoDTO) {
        // 1. Busca el producto que ya existe
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // 2. Busca la nueva categoría (por si cambió)
        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                .orElseThrow(
                        () -> new RuntimeException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));

        // 3. Actualiza todos los campos del producto existente
        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setStock(productoDTO.getStock());
        productoExistente.setCategoria(categoria);
        productoExistente.setTipoImpuestoIva(productoDTO.getTipoImpuestoIva());

        // 4. Guarda la entidad actualizada
        Producto productoGuardado = productoRepository.save(productoExistente);

        // 5. Convierte la entidad guardada al DTO de respuesta
        return convertirEntidadADTO(productoGuardado);
    }

    /**
     * Elimina un producto por su ID.
     */
    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    /**
     * Este es el "traductor". Se ejecuta dentro de la transacción de
     * 'buscarTodos()',
     * por lo que la conexión a la DB sigue abierta.
     * ¡AQUÍ ES DONDE SE SOLUCIONA LA LazyInitializationException!
     */
    private ProductoResponseDTO convertirEntidadADTO(Producto producto) {
        // Como la conexión está abierta, podemos acceder a getCategoria() sin error.
        CategoriaDTO categoriaDTO = new CategoriaDTO();
        categoriaDTO.setId(producto.getCategoria().getId());
        categoriaDTO.setNombre(producto.getCategoria().getNombre());

        // Arma el "plato servido" (ProductoResponseDTO)
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setCategoria(categoriaDTO); // Asigna el DTO de categoría, no la entidad.
        dto.setTipoImpuestoIva(producto.getTipoImpuestoIva());

        return dto;
    }
}
