package com.papusbarbershop.dto;

import java.math.BigDecimal;

/**
 * DTO para la entidad Producto.
 */
public class ProductoDTO {

    private Long id;
    private String nombre;
    private Integer stock;
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private Integer comision;
    private String imagenUrl; // URL presignada de la imagen en S3
    private String descripcion; // Descripción del producto

    // ==================== CONSTRUCTORES ====================

    public ProductoDTO() {
    }

    public ProductoDTO(Long id, String nombre, Integer stock, BigDecimal precioCosto, BigDecimal precioVenta, Integer comision) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.precioCosto = precioCosto;
        this.precioVenta = precioVenta;
        this.comision = comision;
    }

    public ProductoDTO(Long id, String nombre, Integer stock, BigDecimal precioCosto, BigDecimal precioVenta, Integer comision, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.precioCosto = precioCosto;
        this.precioVenta = precioVenta;
        this.comision = comision;
        this.imagenUrl = imagenUrl;
    }

    public ProductoDTO(Long id, String nombre, Integer stock, BigDecimal precioCosto, BigDecimal precioVenta, Integer comision, String imagenUrl, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.precioCosto = precioCosto;
        this.precioVenta = precioVenta;
        this.comision = comision;
        this.imagenUrl = imagenUrl;
        this.descripcion = descripcion;
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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

