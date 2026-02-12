package com.papusbarbershop.service;

import com.papusbarbershop.dto.LoginRequest;
import com.papusbarbershop.dto.LoginResponse;
import com.papusbarbershop.dto.RegisterRequest;
import com.papusbarbershop.entity.Usuario;
import com.papusbarbershop.exception.RecursoDuplicadoException;
import com.papusbarbershop.exception.RecursoNoEncontradoException;
import com.papusbarbershop.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de autenticación para el sistema de gestión de barbería.
 * 
 * Este servicio maneja la autenticación de usuarios, incluyendo la validación
 * de credenciales y la generación de tokens JWT para sesiones autenticadas.
 */
@Service
public class AuthService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * Autentica un usuario con las credenciales proporcionadas.
     * 
     * @param loginRequest Solicitud de login con username y password
     * @return Respuesta de login con token JWT si es exitoso, o error si falla
     */
    public LoginResponse authenticate(LoginRequest loginRequest) {
        try {
            System.out.println("=== AUTH SERVICE - AUTHENTICATE ===");
            System.out.println("Username recibido: " + loginRequest.getUsername());
            
            if (loginRequest == null || loginRequest.getUsername() == null) {
                System.out.println("ERROR: Usuario requerido");
                return new LoginResponse(false, "Usuario requerido");
            }
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(loginRequest.getUsername());
            
            if (usuarioOpt.isEmpty()) {
                System.out.println("ERROR: Usuario no encontrado en BD: " + loginRequest.getUsername());
                return new LoginResponse(false, "Usuario no encontrado");
            }
            
            Usuario usuario = usuarioOpt.get();
            System.out.println("Usuario encontrado: " + usuario.getUsername());
            System.out.println("Rol: " + usuario.getRol());
            System.out.println("Activo: " + usuario.getActivo());
            System.out.println("Password hash en BD: " + usuario.getPassword().substring(0, 20) + "...");
            
            if (!usuario.getActivo()) {
                System.out.println("ERROR: Usuario inactivo");
                return new LoginResponse(false, "Usuario inactivo");
            }
            
            // Verificar contraseña usando BCrypt
            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword());
            System.out.println("Password match: " + passwordMatches);
            
            if (!passwordMatches) {
                System.out.println("ERROR: Contraseña incorrecta");
                return new LoginResponse(false, "Contraseña incorrecta");
            }
            
            // Generar token JWT
            String token = jwtService.generateToken(
                usuario.getUsername(), 
                usuario.getId(), 
                usuario.getRol().name()
            );
            
            // Retornar respuesta exitosa
            LoginResponse response = new LoginResponse();
            response.setSuccess(true);
            response.setMessage("Login exitoso");
            response.setToken(token);
            response.setUserId(usuario.getId());
            response.setUsername(usuario.getUsername());
            response.setRol(usuario.getRol().name());
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResponse(false, "Error interno del servidor: " + e.getMessage());
        }
    }
    
    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * @param registerRequest Solicitud de registro con datos del usuario
     * @return Usuario creado
     */
    @Transactional
    public Usuario register(RegisterRequest registerRequest) {
        // Verificar si el usuario ya existe
        if (usuarioRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RecursoDuplicadoException("El usuario con username '" + registerRequest.getUsername() + "' ya existe");
        }
        
        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(registerRequest.getUsername());
        usuario.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        usuario.setRol(registerRequest.getRol() != null ? registerRequest.getRol() : Usuario.Rol.BARBERO);
        usuario.setActivo(true);
        
        return usuarioRepository.save(usuario);
    }
    
    /**
     * Valida un token JWT.
     * 
     * @param token Token JWT a validar
     * @param username Nombre de usuario esperado
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token, String username) {
        return jwtService.validateToken(token, username);
    }
}

