package com.papusbarbershop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un elemento de mobiliario o equipo de la barbería.
 * 
 * Incluye información sobre categoría, estado, ubicación y valor.
 */
@Entity
@Table(name = "mobiliario_equipo")
public class MobiliarioEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotBlank(message = "La categoría es obligatoria")
    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria; // Mobiliario, Equipo, Herramienta, etc.

    @NotBlank(message = "El estado es obligatorio")
    @Column(name = "estado", nullable = false, length = 50)
    private String estado; // Bueno, Regular, Necesita Reparación, etc.

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.0", message = "El valor debe ser mayor o igual a 0")
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== CONSTRUCTORES ====================

    public MobiliarioEquipo() {
    }

    public MobiliarioEquipo(String nombre, String descripcion, String categoria, 
                           String estado, LocalDate fechaAdquisicion, 
                           BigDecimal valor, Integer cantidad, String ubicacion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.estado = estado;
        this.fechaAdquisicion = fechaAdquisicion;
        this.valor = valor;
        this.cantidad = cantidad;
        this.ubicacion = ubicacion;
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

