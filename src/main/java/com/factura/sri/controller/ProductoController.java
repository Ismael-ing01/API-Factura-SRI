package com.factura.sri.controller;


import com.factura.sri.dto.ProductoDTO;
import com.factura.sri.dto.ProductoResponseDTO;
import com.factura.sri.model.Producto;
import com.factura.sri.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Para crear un producto, necesitas enviar el cuerpo del producto y el ID de la categoría en la URL.
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody ProductoDTO productoDTO) {
        Producto nuevoProducto = productoService.guardarProducto(productoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarProductos() {
        List<ProductoResponseDTO> productos = productoService.buscarTodos();
        return ResponseEntity.ok(productos);
    }

    // --- NUEVOS MÉTODOS ---

    /**
     * Endpoint para buscar un producto por su ID.
     * Devuelve el "plato servido" (ProductoResponseDTO).
     * Ej: GET /api/productos/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        ProductoResponseDTO producto = productoService.buscarPorId(id);
        return ResponseEntity.ok(producto);
    }

    /**
     * Endpoint para actualizar un producto.
     * Recibe la "comanda" (ProductoDTO).
     * Devuelve el "plato servido" (ProductoResponseDTO).
     * Ej: PUT /api/productos/1
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(@PathVariable Long id, @RequestBody ProductoDTO productoDTO) {
        ProductoResponseDTO productoActualizado = productoService.actualizar(id, productoDTO);
        return ResponseEntity.ok(productoActualizado);
    }

    /**
     * Endpoint para eliminar un producto.
     * Ej: DELETE /api/productos/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
