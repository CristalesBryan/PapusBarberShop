package com.papusbarbershop.service;

import com.papusbarbershop.entity.Usuario;
import com.papusbarbershop.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Servicio para inicializar datos por defecto en la aplicación.
 * 
 * Este servicio se ejecuta al iniciar la aplicación y crea el usuario
 * administrador por defecto si no existe.
 */
@Service
public class DataInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Iniciando inicialización de datos...");
        eliminarRestriccionUnicaCitas();
        initializeAdminUser();
        initializeBarberoUser();
        desactivarHorariosPasados();
        logger.info("Inicialización de datos completada.");
    }
    
    /**
     * Elimina la restricción única de la tabla citas si existe.
     * Esto permite crear nuevas citas en horas donde hay citas completadas o canceladas.
     * Usa JDBC directamente porque las operaciones DDL pueden no funcionar bien con JPA.
     * Se ejecuta automáticamente al iniciar la aplicación.
     */
    private void eliminarRestriccionUnicaCitas() {
        try {
            logger.info("═══════════════════════════════════════════════════════════");
            logger.info("Verificando y eliminando restricción única de citas...");
            logger.info("═══════════════════════════════════════════════════════════");
            
            // Usar JDBC directamente para operaciones DDL
            try (Connection connection = dataSource.getConnection()) {
                // Asegurar que autocommit esté habilitado para DDL
                boolean autoCommitOriginal = connection.getAutoCommit();
                connection.setAutoCommit(true);
                
                try (Statement statement = connection.createStatement()) {
                    // Nombres comunes de restricciones únicas en PostgreSQL
                    String[] nombresRestriccion = {
                        "citas_barbero_id_fecha_hora_key",
                        "citas_barbero_id_fecha_hora_uk",
                        "uk_citas_barbero_fecha_hora",
                        "citas_barbero_id_fecha_hora_unique"
                    };
                    
                    boolean restriccionEliminada = false;
                    String nombreRestriccionEliminada = null;
                    
                    // Intentar eliminar directamente las restricciones más comunes
                    for (String nombreRestriccion : nombresRestriccion) {
                        try {
                            String sql = "ALTER TABLE citas DROP CONSTRAINT IF EXISTS " + nombreRestriccion;
                            statement.execute(sql);
                            logger.info("✓ Intento de eliminar restricción: {}", nombreRestriccion);
                            // Verificar si realmente existía
                            String checkSql = "SELECT COUNT(*) as count FROM information_schema.table_constraints " +
                                            "WHERE table_name = 'citas' AND constraint_type = 'UNIQUE' " +
                                            "AND constraint_name = '" + nombreRestriccion + "'";
                            try (java.sql.ResultSet checkRs = statement.executeQuery(checkSql)) {
                                if (checkRs.next() && checkRs.getLong("count") == 0) {
                                    // La restricción no existe, lo que significa que fue eliminada o nunca existió
                                    // Intentar con el siguiente nombre
                                } else {
                                    // La restricción todavía existe, intentar eliminarla sin IF EXISTS
                                    try {
                                        String dropSql = "ALTER TABLE citas DROP CONSTRAINT " + nombreRestriccion;
                                        statement.execute(dropSql);
                                        logger.info("✓✓ Restricción única eliminada exitosamente: {}", nombreRestriccion);
                                        restriccionEliminada = true;
                                        nombreRestriccionEliminada = nombreRestriccion;
                                        break;
                                    } catch (Exception e2) {
                                        logger.debug("No se pudo eliminar la restricción {}: {}", nombreRestriccion, e2.getMessage());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("La restricción {} no existe o ya fue eliminada: {}", nombreRestriccion, e.getMessage());
                        }
                    }
                    
                    // Si no se eliminó ninguna, buscar cualquier otra restricción única
                    if (!restriccionEliminada) {
                        try {
                            String querySql = "SELECT constraint_name FROM information_schema.table_constraints " +
                                             "WHERE table_name = 'citas' AND constraint_type = 'UNIQUE'";
                            try (java.sql.ResultSet rs = statement.executeQuery(querySql)) {
                                while (rs.next()) {
                                    String constraintName = rs.getString("constraint_name");
                                    try {
                                        // Verificar que la restricción incluya las columnas correctas
                                        String checkSql = "SELECT COUNT(*) as count FROM information_schema.key_column_usage " +
                                                        "WHERE constraint_name = '" + constraintName + "' " +
                                                        "AND table_name = 'citas' " +
                                                        "AND column_name IN ('barbero_id', 'fecha', 'hora')";
                                        try (java.sql.ResultSet checkRs = statement.executeQuery(checkSql)) {
                                            if (checkRs.next()) {
                                                long count = checkRs.getLong("count");
                                                if (count >= 2) { // Al menos 2 de las 3 columnas
                                                    String dropSql = "ALTER TABLE citas DROP CONSTRAINT " + constraintName;
                                                    statement.execute(dropSql);
                                                    logger.info("✓✓ Restricción única eliminada: {}", constraintName);
                                                    restriccionEliminada = true;
                                                    nombreRestriccionEliminada = constraintName;
                                                    break;
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        logger.debug("No se pudo eliminar la restricción {}: {}", constraintName, e.getMessage());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("No se pudieron buscar restricciones únicas: {}", e.getMessage());
                        }
                    }
                    
                    // Restaurar autocommit original
                    connection.setAutoCommit(autoCommitOriginal);
                    
                    if (!restriccionEliminada) {
                        logger.info("✓ No se encontró restricción única en la tabla citas (o ya fue eliminada)");
                        System.out.println("✓ No se encontró restricción única en la tabla citas");
                    } else {
                        logger.info("✓✓✓ Restricción única eliminada exitosamente: {}", nombreRestriccionEliminada);
                        System.out.println("✓✓✓ Restricción única eliminada exitosamente: " + nombreRestriccionEliminada);
                    }
                }
            }
            logger.info("═══════════════════════════════════════════════════════════");
        } catch (Exception e) {
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("ERROR al intentar eliminar restricción única de citas: {}", e.getMessage(), e);
            logger.error("═══════════════════════════════════════════════════════════");
            System.err.println("⚠ ERROR: No se pudo eliminar la restricción única automáticamente.");
            System.err.println("⚠ Ejecuta manualmente el script SQL:");
            System.err.println("   ALTER TABLE citas DROP CONSTRAINT IF EXISTS citas_barbero_id_fecha_hora_key;");
            // No lanzar la excepción para que la aplicación pueda iniciar
        }
    }
    
    /**
     * Gestiona automáticamente los horarios al iniciar la aplicación.
     * Desactiva horarios pasados y activa horarios del día actual.
     */
    private void desactivarHorariosPasados() {
        try {
            logger.info("Gestionando horarios automáticamente al iniciar la aplicación...");
            java.util.Map<String, Integer> resultado = horarioService.gestionarHorariosAutomaticamente();
            int desactivados = resultado.get("horariosDesactivados");
            int activados = resultado.get("horariosActivados");
            
            if (desactivados > 0 || activados > 0) {
                logger.info("✓ Gestión de horarios completada: {} desactivados, {} activados.", 
                        desactivados, activados);
            } else {
                logger.info("✓ No había horarios para gestionar.");
            }
        } catch (Exception e) {
            logger.error("Error al gestionar horarios automáticamente: {}", e.getMessage(), e);
            // No lanzar la excepción para que la aplicación pueda iniciar
        }
    }

    /**
     * Inicializa el usuario administrador por defecto.
     * Si el usuario 'admin' no existe, lo crea con la contraseña 'admin123'.
     * Si existe pero la contraseña no es correcta, la actualiza.
     */
    private void initializeAdminUser() {
        String adminUsername = "admin";
        String adminPassword = "admin123";
        
        if (!usuarioRepository.existsByUsername(adminUsername)) {
            // Crear nuevo usuario admin
            Usuario admin = new Usuario();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRol(Usuario.Rol.ADMIN);
            admin.setActivo(true);
            usuarioRepository.save(admin);
            System.out.println("✓ Usuario administrador creado: " + adminUsername + "/" + adminPassword);
        } else {
            // Verificar si la contraseña es correcta
            Usuario admin = usuarioRepository.findByUsername(adminUsername).orElse(null);
            if (admin != null) {
                // Verificar si la contraseña actual coincide
                boolean passwordCorrect = passwordEncoder.matches(adminPassword, admin.getPassword());
                
                if (!passwordCorrect) {
                    // Actualizar la contraseña
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    usuarioRepository.save(admin);
                    System.out.println("✓ Contraseña del usuario administrador actualizada: " + adminUsername + "/" + adminPassword);
                } else {
                    System.out.println("✓ Usuario administrador ya existe con contraseña correcta");
                }
            }
        }
    }

    /**
     * Inicializa el usuario barbero por defecto.
     * Si el usuario 'barbero' no existe, lo crea con la contraseña 'barbero123'.
     * Si existe pero la contraseña no es correcta, la actualiza.
     */
    private void initializeBarberoUser() {
        String barberoUsername = "barbero";
        String barberoPassword = "barbero123";
        
        if (!usuarioRepository.existsByUsername(barberoUsername)) {
            // Crear nuevo usuario barbero
            Usuario barbero = new Usuario();
            barbero.setUsername(barberoUsername);
            barbero.setPassword(passwordEncoder.encode(barberoPassword));
            barbero.setRol(Usuario.Rol.BARBERO);
            barbero.setActivo(true);
            usuarioRepository.save(barbero);
            System.out.println("✓ Usuario barbero creado: " + barberoUsername + "/" + barberoPassword);
        } else {
            // Verificar si la contraseña es correcta
            Usuario barbero = usuarioRepository.findByUsername(barberoUsername).orElse(null);
            if (barbero != null) {
                // Verificar si la contraseña actual coincide
                boolean passwordCorrect = passwordEncoder.matches(barberoPassword, barbero.getPassword());
                
                if (!passwordCorrect) {
                    // Actualizar la contraseña
                    barbero.setPassword(passwordEncoder.encode(barberoPassword));
                    usuarioRepository.save(barbero);
                    System.out.println("✓ Contraseña del usuario barbero actualizada: " + barberoUsername + "/" + barberoPassword);
                } else {
                    System.out.println("✓ Usuario barbero ya existe con contraseña correcta");
                }
            }
        }
    }
}

