package com.papusbarbershop.dto;

import java.math.BigDecimal;

/**
 * DTO para la entidad Barbero.
 */
public class BarberoDTO {

    private Long id;
    private String nombre;
    private BigDecimal porcentajeServicio;
    private String correo;

    // ==================== CONSTRUCTORES ====================

    public BarberoDTO() {
    }

    public BarberoDTO(Long id, String nombre, BigDecimal porcentajeServicio) {
        this.id = id;
        this.nombre = nombre;
        this.porcentajeServicio = porcentajeServicio;
    }

    public BarberoDTO(Long id, String nombre, BigDecimal porcentajeServicio, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.porcentajeServicio = porcentajeServicio;
        this.correo = correo;
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

