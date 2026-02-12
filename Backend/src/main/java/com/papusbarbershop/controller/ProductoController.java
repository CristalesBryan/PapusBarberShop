package com.papusbarbershop.controller;

import com.papusbarbershop.dto.ProductoCreateDTO;
import com.papusbarbershop.dto.ProductoDTO;
import com.papusbarbershop.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la gestión de productos.
 */
@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    /**
     * Crea un nuevo producto.
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * @param productoCreateDTO DTO con los datos del producto
     * @return Producto creado
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoDTO> createProducto(@Valid @RequestBody ProductoCreateDTO productoCreateDTO) {
        ProductoDTO producto = productoService.create(productoCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    /**
     * Actualiza un producto existente.
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * @param id ID del producto
     * @param productoCreateDTO DTO con los datos actualizados
     * @return Producto actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoDTO> updateProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoCreateDTO productoCreateDTO) {
        ProductoDTO producto = productoService.update(id, productoCreateDTO);
        return ResponseEntity.ok(producto);
    }

    /**
     * Obtiene todos los productos.
     * 
     * @return Lista de productos
     */
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> getAllProductos() {
        List<ProductoDTO> productos = productoService.findAll();
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtiene un producto por su ID.
     * 
     * @param id ID del producto
     * @return Producto encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> getProductoById(@PathVariable Long id) {
        ProductoDTO producto = productoService.findById(id);
        return ResponseEntity.ok(producto);
    }

    /**
     * Elimina un producto por su ID.
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * @param id ID del producto a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

