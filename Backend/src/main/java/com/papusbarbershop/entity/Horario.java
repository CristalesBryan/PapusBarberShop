package com.papusbarbershop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Entidad que representa el horario de trabajo de un barbero.
 * 
 * Cada barbero puede tener un horario de entrada y salida definido.
 */
@Entity
@Table(name = "horarios")
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "El barbero es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbero_id", nullable = false)
    private Barbero barbero;

    @NotNull(message = "La hora de entrada es obligatoria")
    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @NotNull(message = "La hora de salida es obligatoria")
    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ==================== CONSTRUCTORES ====================

    public Horario() {
    }

    public Horario(Barbero barbero, LocalTime horaEntrada, LocalTime horaSalida, Boolean activo) {
        this.barbero = barbero;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.activo = activo;
    }

    // ==================== GETTERS Y SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Barbero getBarbero() {
        return barbero;
    }

    public void setBarbero(Barbero barbero) {
        this.barbero = barbero;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

