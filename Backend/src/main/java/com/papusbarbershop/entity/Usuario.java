package com.papusbarbershop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad que representa un usuario del sistema.
 * 
 * Los usuarios pueden ser ADMIN o BARBERO, cada uno con diferentes permisos.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El username es obligatorio")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @NotNull(message = "El estado activo es obligatorio")
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * Enum que representa los roles disponibles en el sistema.
     */
    public enum Rol {
        ADMIN,
        BARBERO
    }

    // ==================== CONSTRUCTORES ====================

    public Usuario() {
    }

    public Usuario(String username, String password, Rol rol, Boolean activo) {
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    // ==================== GETTERS Y SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

