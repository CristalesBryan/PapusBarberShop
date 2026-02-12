package com.papusbarbershop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio asíncrono para el envío de correos electrónicos.
 * 
 * Este servicio desacopla el envío de correos del flujo principal de la aplicación,
 * ejecutando las operaciones de envío en segundo plano mediante un ExecutorService.
 * 
 * ARQUITECTURA:
 * - EmailAsyncService: Coordina el envío asíncrono
 * - EmailExecutor: Gestiona el pool de hilos (ExecutorService)
 * - JavaMailSender: Realiza el envío real del correo
 * 
 * VENTAJAS:
 * - No bloquea las respuestas del servidor
 * - Las excepciones se manejan dentro del hilo asíncrono
 * - Escalable: puede manejar múltiples envíos simultáneos
 */
@Service
public class EmailAsyncService {

    private static final Logger logger = LoggerFactory.getLogger(EmailAsyncService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private EmailExecutor emailExecutor;

    @Value("${spring.mail.username:}")
    private String emailFrom;

    /**
     * Envía un correo de confirmación de cita de forma ASÍNCRONA.
     * 
     * Este método NO bloquea la ejecución. El correo se envía en segundo plano
     * y cualquier error se registra sin afectar la respuesta al usuario.
     * 
     * @param correos Lista de correos destinatarios
     * @param nombreCliente Nombre del cliente
     * @param fecha Fecha de la cita
     * @param hora Hora de la cita
     * @param barberoNombre Nombre del barbero
     * @param tipoCorteNombre Nombre del tipo de corte
     * @param comentarios Comentarios adicionales
     */
    public void enviarConfirmacionCitaAsync(List<String> correos, String nombreCliente, 
                                           String fecha, String hora, String barberoNombre,
                                           String tipoCorteNombre, String comentarios) {
        
        // Validar que hay correos para enviar
        if (correos == null || correos.isEmpty()) {
            logger.warn("No se proporcionaron correos para enviar la confirmación (asíncrono)");
            return;
        }

        // Filtrar correos vacíos o inválidos
        List<String> correosValidos = correos.stream()
                .filter(c -> c != null && !c.trim().isEmpty())
                .toList();

        if (correosValidos.isEmpty()) {
            logger.warn("No hay correos válidos para enviar la confirmación (asíncrono)");
            return;
        }

        // Ejecutar el envío de forma asíncrona
        emailExecutor.ejecutarEnvioAsincrono(() -> {
            enviarCorreoConfirmacion(correosValidos, nombreCliente, fecha, hora, 
                                   barberoNombre, tipoCorteNombre, comentarios);
        });
        
        logger.info("Tarea de envío de correo de confirmación enviada al pool asíncrono. " +
                   "Destinatarios: {}. El correo se enviará en segundo plano.", correosValidos);
    }

    /**
     * Método genérico para enviar un correo de forma asíncrona.
     * 
     * @param destinatario Correo del destinatario
     * @param asunto Asunto del correo
     * @param mensaje Cuerpo del mensaje
     */
    public void enviarCorreoAsync(String destinatario, String asunto, String mensaje) {
        if (destinatario == null || destinatario.trim().isEmpty()) {
            logger.warn("No se proporcionó destinatario para el correo");
            return;
        }

        emailExecutor.ejecutarEnvioAsincrono(() -> {
            enviarCorreoSimple(destinatario, asunto, mensaje);
        });
        
        logger.info("Tarea de envío de correo genérico enviada al pool asíncrono. " +
                   "Destinatario: {}. El correo se enviará en segundo plano.", destinatario);
    }

    /**
     * Método privado que realiza el envío real del correo de confirmación.
     * Este método se ejecuta dentro del hilo asíncrono.
     */
    private void enviarCorreoConfirmacion(List<String> correos, String nombreCliente, 
                                         String fecha, String hora, String barberoNombre,
                                         String tipoCorteNombre, String comentarios) {
        
        if (mailSender == null) {
            logger.warn("JavaMailSender no está configurado. No se enviará el correo.");
            logger.info("Correo que se habría enviado a: {}", correos);
            return;
        }

        try {
            String asunto = "Confirmación de Cita - Papus BarberShop";
            String cuerpo = construirCuerpoEmail(nombreCliente, fecha, hora, barberoNombre, 
                                                tipoCorteNombre, comentarios);

            String correoRemitente = (emailFrom != null && !emailFrom.isEmpty()) 
                    ? emailFrom 
                    : "noreply@papusbarbershop.com";

            logger.info("Iniciando envío asíncrono de correos de confirmación. Remitente: {}, Destinatarios: {}", 
                    correoRemitente, correos);

            int correosEnviadosExitosamente = 0;
            for (String correo : correos) {
                try {
                    SimpleMailMessage mensaje = new SimpleMailMessage();
                    mensaje.setTo(correo.trim());
                    mensaje.setSubject(asunto);
                    mensaje.setText(cuerpo);
                    mensaje.setFrom(correoRemitente);

                    mailSender.send(mensaje);
                    correosEnviadosExitosamente++;
                    logger.info("✓ Correo de confirmación enviado exitosamente a: {} (asíncrono)", correo);
                } catch (Exception e) {
                    logger.error("✗ Error al enviar correo a {} (asíncrono): {}", correo, e.getMessage(), e);
                    // Continuar con los demás correos aunque uno falle
                }
            }
            
            logger.info("Proceso de envío asíncrono de correos completado. Total enviados: {}/{}", 
                    correosEnviadosExitosamente, correos.size());
        } catch (Exception e) {
            logger.error("Error crítico en envío asíncrono de correos de confirmación: {}", e.getMessage(), e);
            // No propagar la excepción - ya está dentro del hilo asíncrono
        }
    }

    /**
     * Método privado que realiza el envío real de un correo simple.
     * Este método se ejecuta dentro del hilo asíncrono.
     */
    private void enviarCorreoSimple(String destinatario, String asunto, String mensaje) {
        if (mailSender == null) {
            logger.warn("JavaMailSender no está configurado. No se enviará el correo.");
            return;
        }

        try {
            String correoRemitente = (emailFrom != null && !emailFrom.isEmpty()) 
                    ? emailFrom 
                    : "noreply@papusbarbershop.com";

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(destinatario.trim());
            mailMessage.setSubject(asunto);
            mailMessage.setText(mensaje);
            mailMessage.setFrom(correoRemitente);

            mailSender.send(mailMessage);
            logger.info("✓ Correo enviado exitosamente a: {} (asíncrono)", destinatario);
        } catch (Exception e) {
            logger.error("✗ Error al enviar correo a {} (asíncrono): {}", destinatario, e.getMessage(), e);
            // No propagar la excepción - ya está dentro del hilo asíncrono
        }
    }

    /**
     * Construye el cuerpo del correo electrónico de confirmación.
     */
    private String construirCuerpoEmail(String nombreCliente, String fecha, String hora,
                                       String barberoNombre, String tipoCorteNombre, 
                                       String comentarios) {
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("¡Hola ").append(nombreCliente).append("! 👋\n\n");
        cuerpo.append("✨ Su cita ha sido confirmada exitosamente ✨\n\n");
        cuerpo.append("📋 Detalles de la cita:\n");
        cuerpo.append("📅 Fecha: ").append(fecha).append("\n");
        cuerpo.append("🕐 Hora: ").append(hora).append("\n");
        cuerpo.append("💇 Barbero: ").append(barberoNombre).append("\n");
        cuerpo.append("✂️ Tipo de Corte: ").append(tipoCorteNombre).append("\n");
        
        if (comentarios != null && !comentarios.trim().isEmpty()) {
            cuerpo.append("💬 Comentarios: ").append(comentarios).append("\n");
        }
        
        cuerpo.append("\n");
        cuerpo.append("🎯 Esperamos verle pronto en Papus BarberShop 🎯\n\n");
        cuerpo.append("Saludos cordiales,\n");
        cuerpo.append("Equipo Papus BarberShop 💈");
        
        return cuerpo.toString();
    }
}

