package com.papusbarbershop.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitudes de login.
 */
public class LoginRequest {

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    // ==================== CONSTRUCTORES ====================

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
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
}

