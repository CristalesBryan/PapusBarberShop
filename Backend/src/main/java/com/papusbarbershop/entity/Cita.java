package com.papusbarbershop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad que representa una cita agendada en la barbería.
 * 
 * Cada cita está asociada a un barbero, un tipo de corte y contiene
 * información del cliente.
 */
@Entity
@Table(name = "citas")
// Se eliminó la restricción única porque impide crear nuevas citas en horas donde hay citas completadas o canceladas.
// La validación de disponibilidad se maneja en CitaService.validarDisponibilidad() que excluye citas completadas/canceladas.
public class Cita {

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

    @NotNull(message = "El tipo de corte es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_corte_id", nullable = false)
    private TipoCorte tipoCorte;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @NotBlank(message = "El correo del cliente es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Column(name = "correo_cliente", nullable = false, length = 100)
    private String correoCliente;

    @Column(name = "telefono_cliente", length = 20)
    private String telefonoCliente;

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE"; // PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA

    @Column(name = "correos_enviados", columnDefinition = "TEXT")
    private String correosEnviados; // Almacena los correos a los que se envió la confirmación

    // ==================== CONSTRUCTORES ====================

    public Cita() {
    }

    public Cita(LocalDate fecha, LocalTime hora, Barbero barbero, TipoCorte tipoCorte,
                String nombreCliente, String correoCliente, String telefonoCliente, 
                String comentarios, String estado) {
        this.fecha = fecha;
        this.hora = hora;
        this.barbero = barbero;
        this.tipoCorte = tipoCorte;
        this.nombreCliente = nombreCliente;
        this.correoCliente = correoCliente;
        this.telefonoCliente = telefonoCliente;
        this.comentarios = comentarios;
        this.estado = estado;
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

    public TipoCorte getTipoCorte() {
        return tipoCorte;
    }

    public void setTipoCorte(TipoCorte tipoCorte) {
        this.tipoCorte = tipoCorte;
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

    public String getCorreosEnviados() {
        return correosEnviados;
    }

    public void setCorreosEnviados(String correosEnviados) {
        this.correosEnviados = correosEnviados;
    }
}

