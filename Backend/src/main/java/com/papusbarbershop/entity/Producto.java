package com.papusbarbershop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;

/**
 * Entidad que representa un producto del inventario.
 * 
 * Cada producto tiene información sobre stock, precio de costo
 * y precio de venta.
 */
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @NotNull(message = "El precio de costo es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio de costo debe ser mayor o igual a 0")
    @Column(name = "precio_costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCosto;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio de venta debe ser mayor o igual a 0")
    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Min(value = 1, message = "La comisión debe ser al menos 1")
    @Max(value = 100, message = "La comisión no puede ser mayor a 100")
    @Column(name = "comision", nullable = true)
    private Integer comision;

    @Column(name = "s3_key", nullable = true, length = 500)
    private String s3Key; // Clave del objeto en S3 para la imagen del producto

    @Column(name = "descripcion", nullable = true, columnDefinition = "TEXT")
    private String descripcion; // Descripción del producto

    // ==================== CONSTRUCTORES ====================

    public Producto() {
    }

    public Producto(String nombre, Integer stock, BigDecimal precioCosto, BigDecimal precioVenta, Integer comision) {
        this.nombre = nombre;
        this.stock = stock;
        this.precioCosto = precioCosto;
        this.precioVenta = precioVenta;
        this.comision = comision;
    }

    // ==================== GETTERS Y SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

