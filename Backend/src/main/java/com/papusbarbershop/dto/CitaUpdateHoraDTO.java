package com.papusbarbershop.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * DTO para actualizar solo la hora de una cita.
 */
public class CitaUpdateHoraDTO {
    
    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    public CitaUpdateHoraDTO() {
    }

    public CitaUpdateHoraDTO(LocalTime hora) {
        this.hora = hora;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }
}

