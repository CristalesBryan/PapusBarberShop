package com.papusbarbershop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Servicio de scheduler para gestionar automáticamente los horarios.
 * 
 * Este servicio ejecuta tareas programadas para:
 * - Desactivar horarios pasados
 * - Activar horarios del día actual
 * 
 * Las tareas se ejecutan automáticamente:
 * - Al iniciar la aplicación (a través de DataInitializationService)
 * - Diariamente a las 00:00 (medianoche)
 * - Cada hora como respaldo
 */
@Service
public class HorarioSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(HorarioSchedulerService.class);

    @Autowired
    private HorarioService horarioService;

    /**
     * Ejecuta la gestión automática de horarios diariamente a las 00:00 (medianoche).
     * 
     * Este método:
     * 1. Desactiva todos los horarios pasados
     * 2. Activa los horarios del día actual
     * 
     * Se ejecuta automáticamente todos los días a las 00:00:00
     */
    @Scheduled(cron = "0 0 0 * * ?") // Ejecutar a medianoche todos los días
    public void gestionarHorariosDiariamente() {
        logger.info("=== Iniciando gestión automática diaria de horarios ===");
        try {
            Map<String, Integer> resultado = horarioService.gestionarHorariosAutomaticamente();
            logger.info("Gestión diaria completada: {} horarios desactivados, {} horarios activados", 
                    resultado.get("horariosDesactivados"), 
                    resultado.get("horariosActivados"));
        } catch (Exception e) {
            logger.error("Error al ejecutar la gestión automática diaria de horarios: {}", e.getMessage(), e);
        }
        logger.info("=== Finalizando gestión automática diaria de horarios ===");
    }

    /**
     * Ejecuta la gestión automática de horarios cada hora como respaldo.
     * 
     * Este método asegura que los horarios se actualicen incluso si el servidor
     * no estaba ejecutándose a medianoche.
     * 
     * Se ejecuta automáticamente al inicio de cada hora (minuto 0)
     */
    @Scheduled(cron = "0 0 * * * ?") // Ejecutar al inicio de cada hora
    public void gestionarHorariosCadaHora() {
        logger.debug("Ejecutando gestión automática de horarios (respaldo horario)");
        try {
            Map<String, Integer> resultado = horarioService.gestionarHorariosAutomaticamente();
            if (resultado.get("horariosDesactivados") > 0 || resultado.get("horariosActivados") > 0) {
                logger.info("Gestión horaria completada: {} horarios desactivados, {} horarios activados", 
                        resultado.get("horariosDesactivados"), 
                        resultado.get("horariosActivados"));
            }
        } catch (Exception e) {
            logger.error("Error al ejecutar la gestión automática horaria de horarios: {}", e.getMessage(), e);
        }
    }
}

