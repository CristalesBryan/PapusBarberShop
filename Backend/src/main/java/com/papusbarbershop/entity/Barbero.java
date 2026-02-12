package com.papusbarbershop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * Entidad que representa un barbero de la barbería.
 * 
 * Cada barbero tiene un porcentaje de servicio que determina
 * cuánto gana por cada servicio realizado.
 */
@Entity
@Table(name = "barberos")
public class Barbero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "El porcentaje de servicio es obligatorio")
    @DecimalMin(value = "0.0", message = "El porcentaje debe ser mayor o igual a 0")
    @Column(name = "porcentaje_servicio", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeServicio;

    @Column(name = "correo", length = 100)
    private String correo;

    // ==================== CONSTRUCTORES ====================

    public Barbero() {
    }

    public Barbero(String nombre, BigDecimal porcentajeServicio) {
        this.nombre = nombre;
        this.porcentajeServicio = porcentajeServicio;
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

    public BigDecimal getPorcentajeServicio() {
        return porcentajeServicio;
    }

    public void setPorcentajeServicio(BigDecimal porcentajeServicio) {
        this.porcentajeServicio = porcentajeServicio;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}

