package com.papusbarbershop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * Entidad que representa un tipo de corte disponible en la barbería.
 * 
 * Contiene información sobre descripción, tiempo estimado y precio.
 */
@Entity
@Table(name = "tipos_corte")
public class TipoCorte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El tiempo estimado es obligatorio")
    @Min(value = 1, message = "El tiempo debe ser al menos 1 minuto")
    @Column(name = "tiempo_minutos", nullable = false)
    private Integer tiempoMinutos;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio debe ser mayor o igual a 0")
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "barbero_id", nullable = true)
    private Barbero barbero; // Barbero asignado para realizar este tipo de corte (opcional)

    // ==================== CONSTRUCTORES ====================

    public TipoCorte() {
    }

    public TipoCorte(String nombre, String descripcion, Integer tiempoMinutos, BigDecimal precio, Boolean activo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tiempoMinutos = tiempoMinutos;
        this.precio = precio;
        this.activo = activo;
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

    public Barbero getBarbero() {
        return barbero;
    }

    public void setBarbero(Barbero barbero) {
        this.barbero = barbero;
    }
}

