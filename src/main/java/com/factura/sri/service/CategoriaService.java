package com.factura.sri.service;


import com.factura.sri.model.Categoria;
import com.factura.sri.repository.CategoriaRepository;
import com.factura.sri.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    // --- MÉTODOS EXISTENTES ---

    @Transactional // Es buena práctica anotar también el 'guardar'
    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional // Es buena práctica para 'buscar'
    public List<Categoria> buscarTodas() {
        return categoriaRepository.findAll();
    }

    // --- NUEVOS MÉTODOS ---

    /**
     * Busca una categoría por su ID.
     * Si no la encuentra, lanza una excepción.
     */
    @Transactional
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }

    /**
     * Actualiza una categoría existente.
     * Busca la categoría por ID, actualiza su nombre y la guarda.
     */
    @Transactional
    public Categoria actualizar(Long id, Categoria categoriaActualizada) {
        // 1. Busca la categoría existente
        Categoria categoriaExistente = buscarPorId(id);


        // 2. Actualiza los campos
        categoriaExistente.setNombre(categoriaActualizada.getNombre());

        // 3. Guarda los cambios
        return categoriaRepository.save(categoriaExistente);
    }

    /**
     * Elimina una categoría por su ID.
     * Primero verifica que exista.
     */
    @Transactional
    public void eliminar(Long id) {

        // 1. (Opcional pero recomendado) Verificar si la categoría existe
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada con ID: " + id);
        }

        // 2. ¡LA VALIDACIÓN CLAVE!
        // Verificamos si algún producto está usando esta categoría
        boolean enUso = productoRepository.existsByCategoriaId(id);

        if (enUso) {
            // 3. Si está en uso, lanzamos un error claro y no borramos nada.
            throw new RuntimeException("No se puede eliminar la categoría con ID: " + id + " porque está siendo utilizada por productos.");
        }

        // 4. Si no está en uso, la eliminamos.
        categoriaRepository.deleteById(id);
    }
}
