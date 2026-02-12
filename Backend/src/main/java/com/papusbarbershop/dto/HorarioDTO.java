package com.papusbarbershop.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para representar un horario de barbero.
 */
public class HorarioDTO {
    private Long id;
    private Long barberoId;
    private String barberoNombre;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private Boolean activo;
    private LocalDate fecha;

    // ==================== CONSTRUCTORES ====================

    public HorarioDTO() {
    }

    public HorarioDTO(Long id, Long barberoId, String barberoNombre, LocalTime horaEntrada, LocalTime horaSalida, Boolean activo, LocalDate fecha) {
        this.id = id;
        this.barberoId = barberoId;
        this.barberoNombre = barberoNombre;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.activo = activo;
        this.fecha = fecha;
    }

    // ==================== GETTERS Y SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}

