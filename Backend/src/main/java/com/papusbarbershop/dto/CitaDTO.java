package com.papusbarbershop.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para representar una cita.
 */
public class CitaDTO {
    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private Long barberoId;
    private String barberoNombre;
    private Long tipoCorteId;
    private String tipoCorteNombre;
    private String tipoCorteDescripcion;
    private Integer tipoCorteTiempoMinutos;
    private java.math.BigDecimal tipoCortePrecio;
    private String nombreCliente;
    private String correoCliente;
    private String telefonoCliente;
    private String comentarios;
    private String estado;

    public CitaDTO() {
    }

    // Getters y Setters
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

    public Long getTipoCorteId() {
        return tipoCorteId;
    }

    public void setTipoCorteId(Long tipoCorteId) {
        this.tipoCorteId = tipoCorteId;
    }

    public String getTipoCorteNombre() {
        return tipoCorteNombre;
    }

    public void setTipoCorteNombre(String tipoCorteNombre) {
        this.tipoCorteNombre = tipoCorteNombre;
    }

    public String getTipoCorteDescripcion() {
        return tipoCorteDescripcion;
    }

    public void setTipoCorteDescripcion(String tipoCorteDescripcion) {
        this.tipoCorteDescripcion = tipoCorteDescripcion;
    }

    public Integer getTipoCorteTiempoMinutos() {
        return tipoCorteTiempoMinutos;
    }

    public void setTipoCorteTiempoMinutos(Integer tipoCorteTiempoMinutos) {
        this.tipoCorteTiempoMinutos = tipoCorteTiempoMinutos;
    }

    public java.math.BigDecimal getTipoCortePrecio() {
        return tipoCortePrecio;
    }

    public void setTipoCortePrecio(java.math.BigDecimal tipoCortePrecio) {
        this.tipoCortePrecio = tipoCortePrecio;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

