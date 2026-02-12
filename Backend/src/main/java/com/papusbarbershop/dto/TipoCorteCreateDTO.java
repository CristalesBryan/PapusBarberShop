package com.papusbarbershop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * DTO para crear o actualizar un tipo de corte.
 */
public class TipoCorteCreateDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    private String descripcion;
    
    @NotNull(message = "El tiempo estimado es obligatorio")
    @Min(value = 1, message = "El tiempo debe ser al menos 1 minuto")
    private Integer tiempoMinutos;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precio;
    
    private Boolean activo = true;
    
    private Long barberoId; // ID del barbero asignado (opcional)

    // ==================== CONSTRUCTORES ====================

    public TipoCorteCreateDTO() {
    }

    // ==================== GETTERS Y SETTERS ====================

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

    public Integer getTiempoMinutos() {
        return tiempoMinutos;
    }

    public void setTiempoMinutos(Integer tiempoMinutos) {
        this.tiempoMinutos = tiempoMinutos;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getBarberoId() {
        return barberoId;
    }

    public void setBarberoId(Long barberoId) {
        this.barberoId = barberoId;
    }
}

