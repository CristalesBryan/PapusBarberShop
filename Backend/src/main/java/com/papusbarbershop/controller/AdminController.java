package com.papusbarbershop.controller;

import com.papusbarbershop.entity.Usuario;
import com.papusbarbershop.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador temporal para administración.
 * Este endpoint permite actualizar la contraseña del admin sin reiniciar el servidor.
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Endpoint temporal para actualizar la contraseña del usuario admin.
     * SOLO PARA DESARROLLO - ELIMINAR EN PRODUCCIÓN
     */
    @PostMapping("/reset-admin-password")
    public ResponseEntity<String> resetAdminPassword() {
        try {
            Usuario admin = usuarioRepository.findByUsername("admin")
                    .orElseThrow(() -> new RuntimeException("Usuario admin no encontrado"));

            // Actualizar contraseña a "admin123"
            admin.setPassword(passwordEncoder.encode("admin123"));
            usuarioRepository.save(admin);

            return ResponseEntity.ok("Contraseña del usuario admin actualizada exitosamente a 'admin123'");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar contraseña: " + e.getMessage());
        }
    }
}

