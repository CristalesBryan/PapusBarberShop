package com.papusbarbershop.controller;

import com.papusbarbershop.dto.ChangePasswordRequest;
import com.papusbarbershop.entity.Usuario;
import com.papusbarbershop.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para administración.
 * - reset-admin-password: resetea la contraseña del usuario admin a "admin123" (sin autenticación, solo desarrollo/recuperación).
 * - change-password: permite al usuario admin (autenticado) cambiar su contraseña indicando la actual y la nueva.
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
     * Endpoint para restablecer la contraseña del usuario admin a "admin123".
     * Acceso público. Útil para desarrollo o recuperación de acceso.
     */
    @PostMapping("/reset-admin-password")
    public ResponseEntity<String> resetAdminPassword() {
        try {
            Usuario admin = usuarioRepository.findByUsername("admin")
                    .orElseThrow(() -> new RuntimeException("Usuario admin no encontrado"));

            admin.setPassword(passwordEncoder.encode("admin123"));
            usuarioRepository.saveAndFlush(admin);

            return ResponseEntity.ok("Contraseña del usuario admin actualizada a 'admin123'. Inicia sesión y cambia la contraseña desde el menú.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar contraseña: " + e.getMessage());
        }
    }

    /**
     * Cambiar la contraseña del usuario actual (debe ser ADMIN).
     * Requiere contraseña actual y nueva. El usuario debe estar autenticado.
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        if (request == null || request.getCurrentPassword() == null || request.getNewPassword() == null
                || request.getCurrentPassword().isBlank() || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Debe indicar la contraseña actual y la nueva.");
        }
        String newPassword = request.getNewPassword().trim();
        if (newPassword.length() < 4) {
            return ResponseEntity.badRequest().body("La nueva contraseña debe tener al menos 4 caracteres.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return ResponseEntity.status(401).body("No autenticado.");
        }
        String username = auth.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
            return ResponseEntity.badRequest().body("La contraseña actual no es correcta.");
        }

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.saveAndFlush(usuario);
        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }
}

