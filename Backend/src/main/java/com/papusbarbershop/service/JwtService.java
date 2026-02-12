package com.papusbarbershop.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para manejo de tokens JWT.
 * 
 * Este servicio proporciona funcionalidades para generar, validar y extraer
 * información de tokens JWT utilizados para autenticación en el sistema.
 */
@Service
public class JwtService {
    
    @Value("${jwt.secret:PapusBarberShopSecretKey2024SecureKeyForJWTTokenGeneration}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}") // 24 horas por defecto
    private long jwtExpiration;
    
    /**
     * Genera un token JWT para un usuario.
     * 
     * @param username Nombre de usuario
     * @param userId ID del usuario
     * @param rol Rol del usuario
     * @return Token JWT generado
     */
    public String generateToken(String username, Long userId, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("rol", rol);
        return createToken(claims, username);
    }
    
    /**
     * Crea un token JWT con los claims especificados.
     * 
     * @param claims Claims a incluir en el token
     * @param subject Subject del token (usuario)
     * @return Token JWT creado
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Extrae el nombre de usuario del token JWT.
     * 
     * @param token Token JWT
     * @return Nombre de usuario
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extrae la fecha de expiración del token JWT.
     * 
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extrae el ID del usuario del token JWT.
     * 
     * @param token Token JWT
     * @return ID del usuario
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }
    
    /**
     * Extrae el rol del usuario del token JWT.
     * 
     * @param token Token JWT
     * @return Rol del usuario
     */
    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }
    
    /**
     * Extrae un claim específico del token JWT.
     * 
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim
     * @param <T> Tipo del claim
     * @return Valor del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extrae todos los claims del token JWT.
     * 
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * Verifica si el token JWT ha expirado.
     * 
     * @param token Token JWT
     * @return true si ha expirado, false en caso contrario
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Valida un token JWT.
     * 
     * @param token Token JWT
     * @param username Nombre de usuario esperado
     * @return true si es válido, false en caso contrario
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
    
    /**
     * Obtiene la clave de firma para los tokens JWT.
     * 
     * @return Clave de firma
     */
    private Key getSignKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

