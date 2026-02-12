package com.papusbarbershop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;

/**
 * DTO para crear un nuevo producto.
 */
public class ProductoCreateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "El precio de costo es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio de costo debe ser mayor o igual a 0")
    private BigDecimal precioCosto;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio de venta debe ser mayor o igual a 0")
    private BigDecimal precioVenta;

    @Min(value = 1, message = "La comisión debe ser al menos 1")
    @Max(value = 100, message = "La comisión no puede ser mayor a 100")
    private Integer comision;

    private String descripcion; // Descripción del producto (opcional)

    // ==================== CONSTRUCTORES ====================

    public ProductoCreateDTO() {
    }

    // ==================== GETTERS Y SETTERS ====================

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public BigDecimal getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = precioCosto;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Integer getComision() {
        return comision;
    }

    public void setComision(Integer comision) {
        this.comision = comision;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

