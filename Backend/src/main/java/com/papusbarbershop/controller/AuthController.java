package com.papusbarbershop.controller;

import com.papusbarbershop.dto.LoginRequest;
import com.papusbarbershop.dto.LoginResponse;
import com.papusbarbershop.dto.RegisterRequest;
import com.papusbarbershop.entity.Usuario;
import com.papusbarbershop.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para operaciones de autenticación.
 * 
 * Este controlador maneja las peticiones relacionadas con la autenticación
 * de usuarios, incluyendo el login y registro.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * Endpoint para autenticar un usuario.
     * 
     * @param loginRequest Solicitud con username y password
     * @return Respuesta con token JWT si la autenticación es exitosa
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("=== LOGIN REQUEST RECIBIDO ===");
        System.out.println("Username: " + (loginRequest != null ? loginRequest.getUsername() : "null"));
        System.out.println("Password: " + (loginRequest != null && loginRequest.getPassword() != null ? "***" : "null"));
        
        if (loginRequest == null) {
            System.out.println("ERROR: LoginRequest es null");
            LoginResponse errorResponse = new LoginResponse(false, "Solicitud inválida");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
            System.out.println("ERROR: Username vacío");
            LoginResponse errorResponse = new LoginResponse(false, "El username es obligatorio");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            System.out.println("ERROR: Password vacío");
            LoginResponse errorResponse = new LoginResponse(false, "La contraseña es obligatoria");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        LoginResponse response = authService.authenticate(loginRequest);
        
        System.out.println("=== RESPUESTA DE AUTENTICACIÓN ===");
        System.out.println("Success: " + response.isSuccess());
        System.out.println("Message: " + response.getMessage());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint para registrar un nuevo usuario.
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * @param registerRequest Solicitud con datos del usuario
     * @return Usuario creado
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> register(@Valid @RequestBody RegisterRequest registerRequest) {
        Usuario usuario = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }
}

