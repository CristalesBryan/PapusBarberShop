package com.papusbarbershop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para la entidad Servicio.
 */
public class ServicioDTO {

    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private Long barberoId;
    private String barberoNombre;
    private String tipoCorte;
    private String metodoPago;
    private BigDecimal precio;

    // ==================== CONSTRUCTORES ====================

    public ServicioDTO() {
    }

    public ServicioDTO(Long id, LocalDate fecha, LocalTime hora, Long barberoId, 
                      String barberoNombre, String tipoCorte, String metodoPago, BigDecimal precio) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.barberoId = barberoId;
        this.barberoNombre = barberoNombre;
        this.tipoCorte = tipoCorte;
        this.metodoPago = metodoPago;
        this.precio = precio;
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

    public String getTipoCorte() {
        return tipoCorte;
    }

    public void setTipoCorte(String tipoCorte) {
        this.tipoCorte = tipoCorte;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}

