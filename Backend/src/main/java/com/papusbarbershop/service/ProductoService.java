package com.papusbarbershop.service;

import com.papusbarbershop.dto.ProductoCreateDTO;
import com.papusbarbershop.dto.ProductoDTO;
import com.papusbarbershop.entity.Producto;
import com.papusbarbershop.exception.RecursoNoEncontradoException;
import com.papusbarbershop.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de productos.
 */
@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private S3Service s3Service;

    @org.springframework.beans.factory.annotation.Value("${aws.s3.presigned-url-expiration:3600}")
    private long presignedUrlExpiration;

    // Cache temporal de URLs presignadas para evitar generar URLs repetidas en el mismo request
    // Estructura: Map<s3Key, {url, expiresAt}>
    private static class PresignedUrlCache {
        String url;
        long expiresAt;
    }
    private final Map<String, PresignedUrlCache> presignedUrlCache = new HashMap<>();

    /**
     * Crea un nuevo producto.
     * 
     * @param productoCreateDTO DTO con los datos del producto
     * @return Producto creado
     */
    @Transactional
    public ProductoDTO create(ProductoCreateDTO productoCreateDTO) {
        Producto producto = new Producto();
        producto.setNombre(productoCreateDTO.getNombre());
        producto.setStock(productoCreateDTO.getStock());
        producto.setPrecioCosto(productoCreateDTO.getPrecioCosto());
        producto.setPrecioVenta(productoCreateDTO.getPrecioVenta());
        producto.setComision(productoCreateDTO.getComision() != null ? productoCreateDTO.getComision() : 1);
        producto.setDescripcion(productoCreateDTO.getDescripcion());

        Producto saved = productoRepository.save(producto);
        return convertToDTOConImagen(saved);
    }

    /**
     * Actualiza un producto existente.
     * 
     * @param id ID del producto
     * @param productoCreateDTO DTO con los datos actualizados
     * @return Producto actualizado
     */
    @Transactional
    public ProductoDTO update(Long id, ProductoCreateDTO productoCreateDTO) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado"));

        producto.setNombre(productoCreateDTO.getNombre());
        producto.setStock(productoCreateDTO.getStock());
        producto.setPrecioCosto(productoCreateDTO.getPrecioCosto());
        producto.setPrecioVenta(productoCreateDTO.getPrecioVenta());
        producto.setComision(productoCreateDTO.getComision() != null ? productoCreateDTO.getComision() : 1);
        producto.setDescripcion(productoCreateDTO.getDescripcion());

        Producto saved = productoRepository.save(producto);
        return convertToDTOConImagen(saved);
    }

    /**
     * Obtiene todos los productos con URLs presignadas de imágenes.
     * 
     * @return Lista de productos con imagenUrl incluida
     */
    public List<ProductoDTO> findAll() {
        // Limpiar cache expirado antes de procesar
        limpiarCacheExpirado();
        
        return productoRepository.findAll().stream()
                .map(this::convertToDTOConImagen)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un producto por su ID con URL presignada de imagen.
     * 
     * @param id ID del producto
     * @return Producto encontrado con imagenUrl incluida
     * @throws RecursoNoEncontradoException si no se encuentra el producto
     */
    public ProductoDTO findById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado"));
        return convertToDTOConImagen(producto);
    }

    /**
     * Obtiene la entidad Producto por su ID.
     * 
     * @param id ID del producto
     * @return Entidad Producto
     * @throws RecursoNoEncontradoException si no se encuentra el producto
     */
    public Producto findEntityById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado"));
    }

    /**
     * Obtiene todas las entidades Producto (sin convertir a DTO).
     * 
     * @return Lista de entidades Producto
     */
    public List<Producto> findAllEntities() {
        return productoRepository.findAll();
    }

    /**
     * Elimina un producto por su ID.
     * 
     * @param id ID del producto a eliminar
     * @throws RecursoNoEncontradoException si no se encuentra el producto
     */
    @Transactional
    public void delete(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado"));
        productoRepository.delete(producto);
    }

    /**
     * Actualiza la clave S3 de la imagen de un producto.
     * 
     * @param productoId ID del producto
     * @param s3Key Clave del objeto en S3
     * @throws RecursoNoEncontradoException si no se encuentra el producto
     */
    @Transactional
    public void actualizarS3Key(Long productoId, String s3Key) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + productoId + " no encontrado"));
        producto.setS3Key(s3Key);
        productoRepository.save(producto);
    }

    /**
     * Convierte una entidad Producto a DTO sin imagen (para operaciones de creación/actualización).
     * 
     * @param producto Entidad Producto
     * @return DTO de Producto
     */
    private ProductoDTO convertToDTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getStock(),
                producto.getPrecioCosto(),
                producto.getPrecioVenta(),
                producto.getComision() != null ? producto.getComision() : 1
        );
        dto.setDescripcion(producto.getDescripcion());
        return dto;
    }

    /**
     * Convierte una entidad Producto a DTO con URL presignada de imagen.
     * 
     * @param producto Entidad Producto
     * @return DTO de Producto con imagenUrl
     */
    private ProductoDTO convertToDTOConImagen(Producto producto) {
        ProductoDTO dto = convertToDTO(producto);
        
        // Obtener s3Key directamente de la entidad Producto (desde la base de datos)
        String s3Key = producto.getS3Key();
        
        if (s3Key != null && !s3Key.isEmpty()) {
            // Verificar si ya tenemos una URL presignada válida en cache
            PresignedUrlCache cached = presignedUrlCache.get(s3Key);
            if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
                dto.setImagenUrl(cached.url);
            } else {
                // Generar nueva URL presignada
                try {
                    String presignedUrl = s3Service.generatePresignedDownloadUrl(s3Key, presignedUrlExpiration);
                    dto.setImagenUrl(presignedUrl);
                    
                    // Guardar en cache
                    PresignedUrlCache cacheEntry = new PresignedUrlCache();
                    cacheEntry.url = presignedUrl;
                    cacheEntry.expiresAt = System.currentTimeMillis() + (presignedUrlExpiration * 1000);
                    presignedUrlCache.put(s3Key, cacheEntry);
                } catch (Exception e) {
                    // Si falla la generación de URL, dejar imagenUrl como null
                    System.err.println("Error al generar URL presignada para producto " + producto.getId() + " con s3Key " + s3Key + ": " + e.getMessage());
                    e.printStackTrace();
                    dto.setImagenUrl(null);
                }
            }
        } else {
            // No hay s3Key para este producto
            dto.setImagenUrl(null);
        }
        
        return dto;
    }

    /**
     * Limpia las entradas expiradas del cache de URLs presignadas.
     */
    private void limpiarCacheExpirado() {
        long ahora = System.currentTimeMillis();
        presignedUrlCache.entrySet().removeIf(entry -> entry.getValue().expiresAt <= ahora);
    }
}

