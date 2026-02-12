package com.papusbarbershop.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.time.LocalDate;

/**
 * DTO para crear un nuevo horario.
 */
public class HorarioCreateDTO {
    
    @NotNull(message = "El barbero es obligatorio")
    private Long barberoId;
    
    @NotNull(message = "La hora de entrada es obligatoria")
    private LocalTime horaEntrada;
    
    @NotNull(message = "La hora de salida es obligatoria")
    private LocalTime horaSalida;
    
    private Boolean activo = true;
    
    private LocalDate fecha; // Fecha opcional para el horario

    // ==================== CONSTRUCTORES ====================

    public HorarioCreateDTO() {
    }

    public HorarioCreateDTO(Long barberoId, LocalTime horaEntrada, LocalTime horaSalida, Boolean activo) {
        this.barberoId = barberoId;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.activo = activo;
    }

    // ==================== GETTERS Y SETTERS ====================

    public Long getBarberoId() {
        return barberoId;
    }

    public void setBarberoId(Long barberoId) {
        this.barberoId = barberoId;
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

