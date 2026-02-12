package com.papusbarbershop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad que representa un servicio (corte) realizado en la barbería.
 * 
 * Cada servicio está asociado a un barbero y tiene información sobre
 * el tipo de corte, método de pago y precio.
 */
@Entity
@Table(name = "servicios")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @NotNull(message = "El barbero es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "barbero_id", nullable = false)
    private Barbero barbero;

    @NotBlank(message = "El tipo de corte es obligatorio")
    @Column(name = "tipo_corte", nullable = false, length = 100)
    private String tipoCorte;

    @NotBlank(message = "El método de pago es obligatorio")
    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio debe ser mayor o igual a 0")
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    // ==================== CONSTRUCTORES ====================

    public Servicio() {
    }

    public Servicio(LocalDate fecha, LocalTime hora, Barbero barbero, String tipoCorte, 
                   String metodoPago, BigDecimal precio) {
        this.fecha = fecha;
        this.hora = hora;
        this.barbero = barbero;
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

    public Barbero getBarbero() {
        return barbero;
    }

    public void setBarbero(Barbero barbero) {
        this.barbero = barbero;
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

