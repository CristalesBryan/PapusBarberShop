package com.papusbarbershop.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO para crear una nueva cita.
 */
public class CitaCreateDTO {
    
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;
    
    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;
    
    @NotNull(message = "El barbero es obligatorio")
    private Long barberoId;
    
    @NotNull(message = "El tipo de corte es obligatorio")
    private Long tipoCorteId;
    
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombreCliente;
    
    @NotBlank(message = "El correo del cliente es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    private String correoCliente;
    
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefonoCliente;
    
    private String comentarios;
    
    @NotEmpty(message = "Debe proporcionar al menos un correo para la confirmación")
    @Size(min = 1, max = 10, message = "Debe proporcionar entre 1 y 10 correos")
    private List<@Email(message = "Cada correo debe tener un formato válido") String> correosConfirmacion;

    public CitaCreateDTO() {
    }

    // Getters y Setters
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

    public Long getTipoCorteId() {
        return tipoCorteId;
    }

    public void setTipoCorteId(Long tipoCorteId) {
        this.tipoCorteId = tipoCorteId;
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

    public List<String> getCorreosConfirmacion() {
        return correosConfirmacion;
    }

    public void setCorreosConfirmacion(List<String> correosConfirmacion) {
        this.correosConfirmacion = correosConfirmacion;
    }
}

