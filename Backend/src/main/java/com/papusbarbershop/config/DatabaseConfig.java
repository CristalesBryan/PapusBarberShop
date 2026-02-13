package com.papusbarbershop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuración de base de datos para Railway.
 * 
 * Convierte automáticamente DATABASE_URL de Railway (formato postgresql://...)
 * al formato JDBC que requiere Spring Boot (jdbc:postgresql://...)
 * y extrae credenciales de la URL si están incluidas.
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String jdbcUrl = convertToJdbcUrl(datasourceUrl);
        String username = datasourceUsername;
        String password = datasourcePassword;
        
        // Si la URL contiene credenciales (formato Railway), extraerlas
        if (datasourceUrl != null && datasourceUrl.startsWith("postgresql://")) {
            try {
                URI uri = new URI(datasourceUrl);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isEmpty()) {
                    String[] credentials = userInfo.split(":");
                    if (credentials.length >= 1 && (username == null || username.isEmpty())) {
                        username = credentials[0];
                    }
                    if (credentials.length >= 2 && (password == null || password.isEmpty())) {
                        password = credentials[1];
                    }
                }
            } catch (URISyntaxException e) {
                // Si falla el parsing, usar las variables de entorno directamente
                System.err.println("Warning: No se pudo parsear DATABASE_URL, usando variables de entorno: " + e.getMessage());
            }
        }
        
        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username != null && !username.isEmpty() ? username : "postgres")
                .password(password != null && !password.isEmpty() ? password : "")
                .build();
    }

    /**
     * Convierte una URL de PostgreSQL al formato JDBC.
     * 
     * @param url URL original (puede ser postgresql:// o jdbc:postgresql://)
     * @return URL en formato JDBC (sin credenciales en la URL)
     */
    private String convertToJdbcUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "jdbc:postgresql://localhost:5432/papus_barbershop";
        }

        // Si ya está en formato JDBC, retornar tal cual
        if (url.startsWith("jdbc:postgresql://")) {
            return url;
        }

        // Si está en formato postgresql:// (Railway), convertir a jdbc:postgresql://
        // y remover credenciales de la URL (se usarán en username/password)
        if (url.startsWith("postgresql://")) {
            try {
                URI uri = new URI(url);
                // Construir URL JDBC sin credenciales
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s",
                    uri.getHost(),
                    uri.getPort() > 0 ? uri.getPort() : 5432,
                    uri.getPath());
                return jdbcUrl;
            } catch (URISyntaxException e) {
                // Si falla el parsing, simplemente agregar jdbc: al inicio
                return "jdbc:" + url;
            }
        }

        // Si no coincide con ningún formato conocido, retornar tal cual
        return url;
    }
}

