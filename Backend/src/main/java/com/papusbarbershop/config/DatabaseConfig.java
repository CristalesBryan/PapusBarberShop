package com.papusbarbershop.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuración de base de datos para Railway (Producción).
 * 
 * Esta clase parsea automáticamente DATABASE_URL de Railway (formato postgresql://...)
 * y configura HikariPool con parámetros optimizados para producción.
 * 
 * IMPORTANTE: Esta configuración está diseñada SOLO para producción en Railway.
 * No incluye fallbacks para localhost.
 * 
 * @Profile("prod") - Solo se activa en producción
 */
@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Value("${PGUSER:}")
    private String pgUser;

    @Value("${PGPASSWORD:}")
    private String pgPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        logger.info("=== Configurando DataSource para Railway (Producción) ===");
        logger.debug("DATABASE_URL recibida: {}", maskPassword(databaseUrl));
        
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new IllegalStateException(
                "DATABASE_URL no está configurada. Esta configuración requiere Railway."
            );
        }

        // Parsear DATABASE_URL de Railway
        DatabaseConnectionInfo connectionInfo = parseDatabaseUrl(databaseUrl);
        
        // Configurar HikariPool
        HikariConfig config = new HikariConfig();
        
        // URL JDBC (sin credenciales)
        config.setJdbcUrl(connectionInfo.getJdbcUrl());
        logger.info("JDBC URL configurada: {}", connectionInfo.getJdbcUrl());
        
        // Credenciales (de la URL o variables de entorno)
        String username = connectionInfo.getUsername() != null && !connectionInfo.getUsername().isEmpty() 
            ? connectionInfo.getUsername() 
            : (pgUser != null && !pgUser.isEmpty() ? pgUser : "postgres");
        String password = connectionInfo.getPassword() != null && !connectionInfo.getPassword().isEmpty()
            ? connectionInfo.getPassword()
            : (pgPassword != null && !pgPassword.isEmpty() ? pgPassword : "");
        
        config.setUsername(username);
        config.setPassword(password);
        logger.info("Usuario configurado: {}", username);
        logger.debug("Contraseña configurada: {}", password.isEmpty() ? "(vacía)" : "***");
        
        // Configuración del Pool HikariCP para producción
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(10);
        config.setIdleTimeout(300000); // 5 minutos
        config.setConnectionTimeout(30000); // 30 segundos
        config.setMaxLifetime(1800000); // 30 minutos
        config.setPoolName("PapusBarberShopPool-Prod");
        config.setLeakDetectionThreshold(60000); // 1 minuto
        
        // Configuraciones adicionales para PostgreSQL
        config.setDriverClassName("org.postgresql.Driver");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // Logging de HikariCP
        config.setRegisterMbeans(true);
        
        logger.info("=== Configuración de HikariPool ===");
        logger.info("Maximum Pool Size: {}", config.getMaximumPoolSize());
        logger.info("Minimum Idle: {}", config.getMinimumIdle());
        logger.info("Idle Timeout: {} ms", config.getIdleTimeout());
        logger.info("Connection Timeout: {} ms", config.getConnectionTimeout());
        logger.info("Max Lifetime: {} ms", config.getMaxLifetime());
        logger.info("Pool Name: {}", config.getPoolName());
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        logger.info("=== DataSource configurado exitosamente ===");
        logger.info("Estado del pool: {}", dataSource.getHikariPoolMXBean() != null ? "Inicializado" : "Pendiente");
        
        return dataSource;
    }

    /**
     * Parsea la URL de base de datos de Railway y extrae la información de conexión.
     * 
     * @param url URL en formato postgresql://user:pass@host:port/db
     * @return Información de conexión parseada
     */
    private DatabaseConnectionInfo parseDatabaseUrl(String url) {
        try {
            // Si ya está en formato JDBC, convertir primero
            String postgresUrl = url.startsWith("jdbc:postgresql://") 
                ? url.replace("jdbc:postgresql://", "postgresql://")
                : url;
            
            // Si no tiene el prefijo postgresql://, agregarlo
            if (!postgresUrl.startsWith("postgresql://")) {
                throw new IllegalArgumentException(
                    "DATABASE_URL debe estar en formato postgresql://user:pass@host:port/db"
                );
            }
            
            URI uri = new URI(postgresUrl);
            
            // Extraer credenciales
            String username = null;
            String password = null;
            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isEmpty()) {
                String[] credentials = userInfo.split(":", 2);
                username = credentials.length > 0 ? credentials[0] : null;
                password = credentials.length > 1 ? credentials[1] : null;
            }
            
            // Construir URL JDBC sin credenciales
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s",
                uri.getHost(),
                port,
                uri.getPath());
            
            logger.debug("URL parseada - Host: {}, Port: {}, Database: {}", 
                uri.getHost(), port, uri.getPath());
            
            return new DatabaseConnectionInfo(jdbcUrl, username, password);
            
        } catch (URISyntaxException e) {
            logger.error("Error al parsear DATABASE_URL: {}", url, e);
            throw new IllegalArgumentException(
                "DATABASE_URL tiene un formato inválido. Debe ser: postgresql://user:pass@host:port/db",
                e
            );
        }
    }

    /**
     * Enmascara la contraseña en los logs para seguridad.
     */
    private String maskPassword(String url) {
        if (url == null) return null;
        // Enmascarar contraseña en formato postgresql://user:pass@host
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:***@");
    }

    /**
     * Clase interna para almacenar información de conexión parseada.
     */
    private static class DatabaseConnectionInfo {
        private final String jdbcUrl;
        private final String username;
        private final String password;

        public DatabaseConnectionInfo(String jdbcUrl, String username, String password) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
