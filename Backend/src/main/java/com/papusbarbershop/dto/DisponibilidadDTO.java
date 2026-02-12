package com.papusbarbershop.dto;

import java.time.LocalTime;
import java.util.List;

/**
 * DTO para representar la disponibilidad de un barbero en una fecha.
 */
public class DisponibilidadDTO {
    private Long barberoId;
    private String barberoNombre;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private List<LocalTime> horasDisponibles;
    private List<LocalTime> horasOcupadas;

    public DisponibilidadDTO() {
    }

    // Getters y Setters
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

    public List<LocalTime> getHorasDisponibles() {
        return horasDisponibles;
    }

    public void setHorasDisponibles(List<LocalTime> horasDisponibles) {
        this.horasDisponibles = horasDisponibles;
    }

    public List<LocalTime> getHorasOcupadas() {
        return horasOcupadas;
    }

    public void setHorasOcupadas(List<LocalTime> horasOcupadas) {
        this.horasOcupadas = horasOcupadas;
    }
}

