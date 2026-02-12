package com.papusbarbershop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Clase singleton para gestionar el ExecutorService que maneja el envío asíncrono de correos.
 * 
 * Esta clase proporciona un pool de hilos reutilizable para ejecutar tareas de envío de correos
 * de forma asíncrona, evitando bloquear las respuestas del servidor.
 * 
 * CARACTERÍSTICAS:
 * - Singleton: una única instancia para toda la aplicación
 * - ExecutorService con pool fijo de hilos
 * - Cierre graceful al detener la aplicación
 * - Manejo de excepciones dentro de las tareas asíncronas
 */
@Component
public class EmailExecutor {

    private static final Logger logger = LoggerFactory.getLogger(EmailExecutor.class);
    
    /**
     * Número de hilos en el pool. 
     * Se puede ajustar según la carga esperada de correos.
     */
    private static final int THREAD_POOL_SIZE = 5;
    
    /**
     * ExecutorService singleton para toda la aplicación.
     * Se inicializa una sola vez y se reutiliza para todas las tareas de envío de correos.
     */
    private final ExecutorService executorService;
    
    /**
     * Constructor que inicializa el ExecutorService con un pool fijo de hilos.
     */
    public EmailExecutor() {
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, r -> {
            Thread thread = new Thread(r, "EmailSender-" + System.currentTimeMillis());
            thread.setDaemon(false); // No son hilos daemon para asegurar que completen
            thread.setUncaughtExceptionHandler((t, e) -> {
                logger.error("Error no capturado en hilo de envío de correo: {}", t.getName(), e);
            });
            return thread;
        });
        logger.info("EmailExecutor inicializado con {} hilos en el pool", THREAD_POOL_SIZE);
    }
    
    /**
     * Ejecuta una tarea de envío de correo de forma asíncrona.
     * 
     * Este método NO bloquea la ejecución. La tarea se ejecuta en segundo plano
     * y cualquier excepción se maneja dentro del hilo asíncrono.
     * 
     * @param task Tarea Runnable que contiene la lógica de envío de correo
     */
    public void ejecutarEnvioAsincrono(Runnable task) {
        if (task == null) {
            logger.warn("Se intentó ejecutar una tarea de envío de correo nula");
            return;
        }
        
        executorService.submit(() -> {
            try {
                logger.debug("Iniciando envío de correo asíncrono en hilo: {}", Thread.currentThread().getName());
                task.run();
                logger.debug("Envío de correo asíncrono completado exitosamente");
            } catch (Exception e) {
                // Las excepciones se manejan aquí para no propagarlas al hilo principal
                logger.error("Error en envío asíncrono de correo (no afecta la respuesta al usuario): {}", 
                           e.getMessage(), e);
            }
        });
        
        logger.debug("Tarea de envío de correo enviada al pool de hilos (no bloqueante)");
    }
    
    /**
     * Obtiene el ExecutorService para uso avanzado (si es necesario).
     * 
     * @return El ExecutorService singleton
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    /**
     * Método de limpieza que se ejecuta al detener la aplicación.
     * Cierra el ExecutorService de forma graceful, esperando a que las tareas
     * en ejecución terminen (hasta 30 segundos).
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Cerrando EmailExecutor...");
        executorService.shutdown();
        
        try {
            // Esperar a que las tareas en ejecución terminen
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("El EmailExecutor no terminó en 30 segundos, forzando cierre...");
                executorService.shutdownNow();
                
                // Esperar otros 10 segundos antes de forzar
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.error("El EmailExecutor no pudo cerrarse correctamente");
                }
            }
            logger.info("EmailExecutor cerrado correctamente");
        } catch (InterruptedException e) {
            logger.error("Interrupción durante el cierre del EmailExecutor", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

