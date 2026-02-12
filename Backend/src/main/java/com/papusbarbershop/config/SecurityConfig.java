package com.papusbarbershop.config;

import com.papusbarbershop.security.JwtAuthenticationFilter;
import com.papusbarbershop.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de seguridad para el sistema de gestión de barbería.
 * 
 * Esta clase configura Spring Security para proporcionar:
 * - Autenticación y autorización de usuarios
 * - Configuración CORS para comunicación con el frontend
 * - Codificación de contraseñas con BCrypt
 * - Configuración de sesiones stateless con JWT
 * - Configuración de permisos específicos por rol:
 *   - ADMIN: Acceso completo a todas las APIs
 *   - BARBERO: Solo acceso a registro de servicios y ventas
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtService jwtService;

    /**
     * Configura el codificador de contraseñas usando BCrypt.
     * 
     * @return PasswordEncoder configurado con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la cadena de filtros de seguridad para HTTP.
     * 
     * @param http HttpSecurity para configurar
     * @return SecurityFilterChain configurado
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF para APIs REST (se manejará con JWT)
                .csrf(csrf -> csrf.disable())
                
                // Configurar CORS personalizado
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configurar sesiones como stateless (sin estado)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configurar reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso público a endpoints de autenticación
                        .requestMatchers("/auth/**").permitAll()
                        
                        // Permitir acceso público al endpoint temporal de reset de contraseña (SOLO DESARROLLO)
                        .requestMatchers("/admin/reset-admin-password").permitAll()
                        
                        // Permitir acceso público a endpoints para vista de clientes (sin autenticación)
                        .requestMatchers("/api/tipos-corte").permitAll() // GET para tipos de corte activos
                        .requestMatchers("/barberos").permitAll() // GET para lista de barberos
                        .requestMatchers("/productos").permitAll() // GET para lista de productos
                        .requestMatchers("/api/citas/disponibilidad").permitAll() // GET para disponibilidad
                        .requestMatchers("/api/citas").permitAll() // POST para crear citas (vista pública)
                        
                        // Endpoints de S3 - URLs presignadas públicas, eliminación requiere autenticación
                        .requestMatchers("/api/s3/presigned-url/**").permitAll() // Generación de URLs presignadas
                        .requestMatchers("/api/s3/exists").permitAll() // Verificación de existencia de archivos
                        .requestMatchers("/api/s3/producto-imagen/**").permitAll() // Referencias de imágenes de productos (público para sincronización)
                        .requestMatchers("/api/s3/producto-imagenes/**").permitAll() // Todas las referencias de imágenes (público para sincronización)
                        .requestMatchers("/api/s3/delete").hasAnyRole("ADMIN", "BARBERO") // Eliminación requiere autenticación
                        
                        // Proteger endpoints según roles
                        // ADMIN y BARBERO: Acceso a servicios y ventas
                        .requestMatchers("/servicios/**").hasAnyRole("ADMIN", "BARBERO")
                        .requestMatchers("/ventas-productos/**").hasAnyRole("ADMIN", "BARBERO")
                        // Permitir lectura de barberos, tipos-corte y productos para BARBERO (necesario para formularios)
                        // Las rutas específicas deben ir antes que las generales con **
                        .requestMatchers("/barberos").hasAnyRole("ADMIN", "BARBERO") // GET para lista de barberos
                        .requestMatchers("/tipos-corte").hasAnyRole("ADMIN", "BARBERO") // GET para lista de tipos de corte
                        .requestMatchers("/productos").hasAnyRole("ADMIN", "BARBERO") // GET para lista de productos (necesario para ventas)
                        // ADMIN: Acceso completo a todo lo demás
                        .requestMatchers("/barberos/**").hasRole("ADMIN") // Otros métodos de barberos solo para ADMIN
                        .requestMatchers("/tipos-corte/**").hasRole("ADMIN") // Otros métodos de tipos-corte solo para ADMIN
                        .requestMatchers("/productos/**").hasRole("ADMIN") // Otros métodos de productos solo para ADMIN
                        .requestMatchers("/horarios/**").hasRole("ADMIN")
                        .requestMatchers("/citas/**").hasRole("ADMIN")
                        .requestMatchers("/mobiliario-equipo/**").hasRole("ADMIN")
                        .requestMatchers("/reportes/**").hasRole("ADMIN")
                        
                        // Requerir autenticación para todas las demás peticiones
                        .anyRequest().authenticated()
                )
                
                // Configurar headers de seguridad
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.deny()));

        // Agregar el filtro JWT antes del filtro de autenticación por defecto
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura la fuente de configuración CORS para permitir comunicación
     * entre el frontend y el backend desde diferentes orígenes.
     * 
     * @return CorsConfigurationSource configurado
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir cualquier origen (usar patrones específicos en producción)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Permitir métodos HTTP estándar
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Permitir cualquier header
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciales en las peticiones
        configuration.setAllowCredentials(true);

        // Registrar la configuración para todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Crea una instancia del filtro JWT con las dependencias inyectadas.
     * 
     * @return JwtAuthenticationFilter configurado
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        // Inyectar manualmente el JwtService
        filter.setJwtService(jwtService);
        return filter;
    }
}

