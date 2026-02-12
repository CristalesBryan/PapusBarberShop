package com.papusbarbershop.security;

import com.papusbarbershop.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de autenticación JWT para Spring Security.
 * 
 * Este filtro intercepta todas las peticiones HTTP y valida los tokens JWT
 * presentes en el header Authorization, estableciendo la autenticación
 * en el contexto de seguridad de Spring.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private JwtService jwtService;
    
    /**
     * Establece el servicio JWT para este filtro.
     * 
     * @param jwtService Servicio JWT a utilizar
     */
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        
        // Verificar si el header Authorization contiene un token JWT
        if (authHeader == null || !authHeader.startsWith("Bearer ") || jwtService == null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extraer el token JWT del header
        jwt = authHeader.substring(7);
        
        try {
            // Extraer el nombre de usuario del token
            username = jwtService.extractUsername(jwt);
            
            // Verificar que el usuario no esté ya autenticado y que el token sea válido
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Validar el token JWT
                if (jwtService.validateToken(jwt, username)) {
                    
                    // Extraer el rol del usuario del token
                    String rol = jwtService.extractRol(jwt);
                    
                    // Crear la autenticación con el rol del usuario
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol))
                    );
                    
                    // Establecer la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Si hay algún error con el token, continuar sin autenticación
            logger.error("Error al procesar token JWT: " + e.getMessage());
        }
        
        // Continuar con el siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }
}

