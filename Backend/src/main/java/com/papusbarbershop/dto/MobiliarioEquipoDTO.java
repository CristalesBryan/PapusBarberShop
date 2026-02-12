package com.papusbarbershop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para la entidad MobiliarioEquipo.
 */
public class MobiliarioEquipoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String estado;
    private LocalDate fechaAdquisicion;
    private BigDecimal valor;
    private Integer cantidad;
    private String ubicacion;
    private String numeroSerie;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== CONSTRUCTORES ====================

    public MobiliarioEquipoDTO() {
    }

    public MobiliarioEquipoDTO(Long id, String nombre, String descripcion, String categoria,
                               String estado, LocalDate fechaAdquisicion, BigDecimal valor,
                               Integer cantidad, String ubicacion, String numeroSerie, 
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.estado = estado;
        this.fechaAdquisicion = fechaAdquisicion;
        this.valor = valor;
        this.cantidad = cantidad;
        this.ubicacion = ubicacion;
        this.numeroSerie = numeroSerie;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaAdquisicion() {
        return fechaAdquisicion;
    }

    public void setFechaAdquisicion(LocalDate fechaAdquisicion) {
        this.fechaAdquisicion = fechaAdquisicion;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

