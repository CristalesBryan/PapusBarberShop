package com.papusbarbershop.dto;

import java.math.BigDecimal;

/**
 * DTO para representar un tipo de corte.
 */
public class TipoCorteDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer tiempoMinutos;
    private BigDecimal precio;
    private Boolean activo;
    private Long barberoId;
    private String barberoNombre;

    public TipoCorteDTO() {
    }

    public TipoCorteDTO(Long id, String nombre, String descripcion, Integer tiempoMinutos, 
                        BigDecimal precio, Boolean activo, Long barberoId, String barberoNombre) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tiempoMinutos = tiempoMinutos;
        this.precio = precio;
        this.activo = activo;
        this.barberoId = barberoId;
        this.barberoNombre = barberoNombre;
    }

    // Getters y Setters
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

    public String getBarberoNombre() {
        return barberoNombre;
    }

    public void setBarberoNombre(String barberoNombre) {
        this.barberoNombre = barberoNombre;
    }
}

