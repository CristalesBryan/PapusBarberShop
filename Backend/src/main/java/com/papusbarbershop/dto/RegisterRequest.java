package com.papusbarbershop.dto;

import jakarta.validation.constraints.NotBlank;
import com.papusbarbershop.entity.Usuario.Rol;

/**
 * DTO para solicitudes de registro de usuarios.
 */
public class RegisterRequest {

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private Rol rol;

    // ==================== CONSTRUCTORES ====================

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password, Rol rol) {
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    // ==================== GETTERS Y SETTERS ====================

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
}

