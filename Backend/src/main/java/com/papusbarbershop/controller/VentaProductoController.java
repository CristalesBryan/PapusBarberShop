package com.papusbarbershop.controller;

import com.papusbarbershop.dto.VentaProductoCreateDTO;
import com.papusbarbershop.dto.VentaProductoDTO;
import com.papusbarbershop.service.VentaProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para la gestión de ventas de productos.
 */
@RestController
@RequestMapping("/ventas-productos")
@CrossOrigin(origins = "*")
public class VentaProductoController {

    @Autowired
    private VentaProductoService ventaProductoService;

    /**
     * Crea una nueva venta de producto.
     * 
     * @param ventaCreateDTO DTO con los datos de la venta
     * @return Venta creada
     */
    @PostMapping
    public ResponseEntity<VentaProductoDTO> createVenta(@Valid @RequestBody VentaProductoCreateDTO ventaCreateDTO) {
        VentaProductoDTO venta = ventaProductoService.create(ventaCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    /**
     * Obtiene todas las ventas de productos.
     * 
     * @return Lista de ventas
     */
    @GetMapping
    public ResponseEntity<List<VentaProductoDTO>> getAllVentas() {
        List<VentaProductoDTO> ventas = ventaProductoService.findAll();
        return ResponseEntity.ok(ventas);
    }

    /**
     * Obtiene todas las ventas de una fecha específica.
     * 
     * @param fecha Fecha a buscar
     * @return Lista de ventas
     */
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<VentaProductoDTO>> getVentasByFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<VentaProductoDTO> ventas = ventaProductoService.findByFecha(fecha);
        return ResponseEntity.ok(ventas);
    }

    /**
     * Obtiene una venta por su ID.
     * 
     * @param id ID de la venta
     * @return Venta encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<VentaProductoDTO> getVentaById(@PathVariable Long id) {
        VentaProductoDTO venta = ventaProductoService.findById(id);
        return ResponseEntity.ok(venta);
    }

    /**
     * Actualiza una venta existente.
     * 
     * @param id ID de la venta a actualizar
     * @param ventaCreateDTO DTO con los nuevos datos de la venta
     * @return Venta actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<VentaProductoDTO> updateVenta(
            @PathVariable Long id,
            @Valid @RequestBody VentaProductoCreateDTO ventaCreateDTO) {
        VentaProductoDTO venta = ventaProductoService.update(id, ventaCreateDTO);
        return ResponseEntity.ok(venta);
    }

    /**
     * Elimina una venta.
     * 
     * @param id ID de la venta a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenta(@PathVariable Long id) {
        ventaProductoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

