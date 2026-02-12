package com.papusbarbershop.service;

import com.papusbarbershop.dto.VentaProductoCreateDTO;
import com.papusbarbershop.dto.VentaProductoDTO;
import com.papusbarbershop.entity.Barbero;
import com.papusbarbershop.entity.Producto;
import com.papusbarbershop.entity.VentaProducto;
import com.papusbarbershop.exception.RecursoNoEncontradoException;
import com.papusbarbershop.exception.ValidacionException;
import com.papusbarbershop.repository.VentaProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de ventas de productos.
 * 
 * Este servicio maneja la lógica de negocio para las ventas de productos,
 * incluyendo validación de stock y actualización automática del inventario.
 */
@Service
public class VentaProductoService {

    @Autowired
    private VentaProductoRepository ventaProductoRepository;

    @Autowired
    private BarberoService barberoService;

    @Autowired
    private ProductoService productoService;

    /**
     * Crea una nueva venta de producto.
     * 
     * Valida el stock disponible, actualiza el inventario y registra la venta.
     * 
     * @param ventaCreateDTO DTO con los datos de la venta
     * @return Venta creada
     * @throws ValidacionException si no hay stock suficiente
     */
    @Transactional
    public VentaProductoDTO create(VentaProductoCreateDTO ventaCreateDTO) {
        // Obtener barbero y producto
        Barbero barbero = barberoService.findEntityById(ventaCreateDTO.getBarberoId());
        Producto producto = productoService.findEntityById(ventaCreateDTO.getProductoId());

        // Validar stock disponible
        Integer stockAntes = producto.getStock();
        if (stockAntes < ventaCreateDTO.getCantidad()) {
            throw new ValidacionException("Stock insuficiente. Stock disponible: " + stockAntes + 
                    ", cantidad solicitada: " + ventaCreateDTO.getCantidad());
        }

        // Calcular importe
        BigDecimal precioUnitario = producto.getPrecioVenta();
        BigDecimal importe = precioUnitario.multiply(BigDecimal.valueOf(ventaCreateDTO.getCantidad()));

        // Actualizar stock del producto
        Integer stockDespues = stockAntes - ventaCreateDTO.getCantidad();
        producto.setStock(stockDespues);
        productoService.update(producto.getId(), convertProductoToDTO(producto));

        // Crear venta
        VentaProducto venta = new VentaProducto();
        venta.setFecha(ventaCreateDTO.getFecha());
        venta.setHora(ventaCreateDTO.getHora());
        venta.setBarbero(barbero);
        venta.setProducto(producto);
        venta.setCantidad(ventaCreateDTO.getCantidad());
        venta.setPrecioUnitario(precioUnitario);
        venta.setImporte(importe);
        venta.setStockAntes(stockAntes);
        venta.setStockDespues(stockDespues);
        venta.setMetodoPago(ventaCreateDTO.getMetodoPago());

        VentaProducto saved = ventaProductoRepository.save(venta);
        return convertToDTO(saved);
    }

    /**
     * Obtiene todas las ventas de productos.
     * 
     * @return Lista de ventas
     */
    public List<VentaProductoDTO> findAll() {
        return ventaProductoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las ventas de una fecha específica.
     * 
     * @param fecha Fecha a buscar
     * @return Lista de ventas
     */
    public List<VentaProductoDTO> findByFecha(LocalDate fecha) {
        return ventaProductoRepository.findByFecha(fecha).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una venta por su ID.
     * 
     * @param id ID de la venta
     * @return Venta encontrada
     * @throws RecursoNoEncontradoException si no se encuentra la venta
     */
    public VentaProductoDTO findById(Long id) {
        VentaProducto venta = ventaProductoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con ID: " + id));
        return convertToDTO(venta);
    }

    /**
     * Actualiza una venta existente.
     * 
     * Si cambia el producto o la cantidad, se ajusta el stock del producto.
     * 
     * @param id ID de la venta a actualizar
     * @param ventaCreateDTO DTO con los nuevos datos de la venta
     * @return Venta actualizada
     * @throws RecursoNoEncontradoException si no se encuentra la venta
     * @throws ValidacionException si no hay stock suficiente
     */
    @Transactional
    public VentaProductoDTO update(Long id, VentaProductoCreateDTO ventaCreateDTO) {
        VentaProducto venta = ventaProductoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con ID: " + id));

        // Obtener barbero y producto
        Barbero barbero = barberoService.findEntityById(ventaCreateDTO.getBarberoId());
        Producto producto = productoService.findEntityById(ventaCreateDTO.getProductoId());

        // Si cambió el producto o la cantidad, ajustar stock
        boolean productoCambio = !venta.getProducto().getId().equals(ventaCreateDTO.getProductoId());
        boolean cantidadCambio = !venta.getCantidad().equals(ventaCreateDTO.getCantidad());

        if (productoCambio || cantidadCambio) {
            // Restaurar stock del producto anterior
            Producto productoAnterior = venta.getProducto();
            Integer stockActualAnterior = productoAnterior.getStock();
            Integer stockRestaurado = stockActualAnterior + venta.getCantidad();
            productoAnterior.setStock(stockRestaurado);
            productoService.update(productoAnterior.getId(), convertProductoToDTO(productoAnterior));

            // Validar stock disponible del nuevo producto (o mismo producto con nueva cantidad)
            Integer stockActual = producto.getStock();
            Integer cantidadNecesaria = ventaCreateDTO.getCantidad();
            
            if (productoCambio) {
                // Si cambió el producto, validar stock del nuevo producto
                if (stockActual < cantidadNecesaria) {
                    throw new ValidacionException("Stock insuficiente. Stock disponible: " + stockActual + 
                            ", cantidad solicitada: " + cantidadNecesaria);
                }
            } else {
                // Si es el mismo producto pero cambió la cantidad, calcular diferencia
                Integer diferencia = cantidadNecesaria - venta.getCantidad();
                if (diferencia > 0 && stockActual < diferencia) {
                    throw new ValidacionException("Stock insuficiente. Stock disponible: " + stockActual + 
                            ", cantidad adicional necesaria: " + diferencia);
                }
            }

            // Obtener stock antes de la actualización
            Integer stockAntes = producto.getStock();
            
            // Actualizar stock del producto (nuevo o actualizado)
            Integer stockDespues = stockActual - cantidadNecesaria;
            producto.setStock(stockDespues);
            productoService.update(producto.getId(), convertProductoToDTO(producto));
            
            // Guardar stock antes y después para la venta
            venta.setStockAntes(stockAntes);
            venta.setStockDespues(stockDespues);
        }

        // Calcular importe
        BigDecimal precioUnitario = producto.getPrecioVenta();
        BigDecimal importe = precioUnitario.multiply(BigDecimal.valueOf(ventaCreateDTO.getCantidad()));

        // Actualizar venta
        venta.setFecha(ventaCreateDTO.getFecha());
        venta.setHora(ventaCreateDTO.getHora());
        venta.setBarbero(barbero);
        venta.setProducto(producto);
        venta.setCantidad(ventaCreateDTO.getCantidad());
        venta.setPrecioUnitario(precioUnitario);
        venta.setImporte(importe);
        venta.setMetodoPago(ventaCreateDTO.getMetodoPago());

        VentaProducto saved = ventaProductoRepository.save(venta);
        return convertToDTO(saved);
    }

    /**
     * Elimina una venta y restaura el stock del producto.
     * 
     * @param id ID de la venta a eliminar
     * @throws RecursoNoEncontradoException si no se encuentra la venta
     */
    @Transactional
    public void delete(Long id) {
        VentaProducto venta = ventaProductoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con ID: " + id));

        // Restaurar stock del producto
        Producto producto = venta.getProducto();
        Integer stockActual = producto.getStock();
        Integer stockRestaurado = stockActual + venta.getCantidad();
        producto.setStock(stockRestaurado);
        productoService.update(producto.getId(), convertProductoToDTO(producto));

        // Eliminar la venta
        ventaProductoRepository.deleteById(id);
    }

    /**
     * Convierte una entidad VentaProducto a DTO.
     * 
     * @param venta Entidad VentaProducto
     * @return DTO de VentaProducto
     */
    private VentaProductoDTO convertToDTO(VentaProducto venta) {
        VentaProductoDTO dto = new VentaProductoDTO();
        dto.setId(venta.getId());
        dto.setFecha(venta.getFecha());
        dto.setHora(venta.getHora());
        dto.setBarberoId(venta.getBarbero().getId());
        dto.setBarberoNombre(venta.getBarbero().getNombre());
        dto.setProductoId(venta.getProducto().getId());
        dto.setProductoNombre(venta.getProducto().getNombre());
        dto.setCantidad(venta.getCantidad());
        dto.setPrecioUnitario(venta.getPrecioUnitario());
        dto.setImporte(venta.getImporte());
        dto.setStockAntes(venta.getStockAntes());
        dto.setStockDespues(venta.getStockDespues());
        dto.setMetodoPago(venta.getMetodoPago());
        return dto;
    }

    /**
     * Convierte una entidad Producto a DTO para actualización.
     * 
     * @param producto Entidad Producto
     * @return DTO de Producto
     */
    private com.papusbarbershop.dto.ProductoCreateDTO convertProductoToDTO(Producto producto) {
        com.papusbarbershop.dto.ProductoCreateDTO dto = new com.papusbarbershop.dto.ProductoCreateDTO();
        dto.setNombre(producto.getNombre());
        dto.setStock(producto.getStock());
        dto.setPrecioCosto(producto.getPrecioCosto());
        dto.setPrecioVenta(producto.getPrecioVenta());
        return dto;
    }
}

