package com.papusbarbershop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para la entidad VentaProducto.
 */
public class VentaProductoDTO {

    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private Long barberoId;
    private String barberoNombre;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal importe;
    private Integer stockAntes;
    private Integer stockDespues;
    private String metodoPago;

    // ==================== CONSTRUCTORES ====================

    public VentaProductoDTO() {
    }

    // ==================== GETTERS Y SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public Long getBarberoId() {
        return barberoId;
    }

    public void setBarberoId(Long barberoId) {
        this.barberoId = barberoId;
    }

    public String getBarberoNombre() {
        return barberoNombre;
    }

    public void setBarberoNombre(String barberoNombre) {
        this.barberoNombre = barberoNombre;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public Integer getStockAntes() {
        return stockAntes;
    }

    public void setStockAntes(Integer stockAntes) {
        this.stockAntes = stockAntes;
    }

    public Integer getStockDespues() {
        return stockDespues;
    }

    public void setStockDespues(Integer stockDespues) {
        this.stockDespues = stockDespues;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}

