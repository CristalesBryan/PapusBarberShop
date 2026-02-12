package com.papusbarbershop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Servicio para el envío de correos electrónicos.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String emailFrom;

    /**
     * Envía un correo de confirmación de cita a múltiples destinatarios.
     * 
     * @param correos Lista de correos destinatarios
     * @param nombreCliente Nombre del cliente
     * @param fecha Fecha de la cita
     * @param hora Hora de la cita
     * @param barberoNombre Nombre del barbero
     * @param tipoCorteNombre Nombre del tipo de corte
     * @param comentarios Comentarios adicionales
     */
    public void enviarConfirmacionCita(List<String> correos, String nombreCliente, 
                                      String fecha, String hora, String barberoNombre,
                                      String tipoCorteNombre, String comentarios) {
        if (mailSender == null) {
            logger.warn("JavaMailSender no está configurado. No se enviará el correo.");
            logger.info("Correo que se habría enviado a: {}", correos);
            return;
        }

        // Validar que hay correos para enviar
        if (correos == null || correos.isEmpty()) {
            logger.warn("No se proporcionaron correos para enviar la confirmación.");
            return;
        }

        // Filtrar correos vacíos o inválidos
        List<String> correosValidos = correos.stream()
                .filter(c -> c != null && !c.trim().isEmpty())
                .toList();

        if (correosValidos.isEmpty()) {
            logger.warn("No hay correos válidos para enviar la confirmación.");
            return;
        }

        try {
            String asunto = "Confirmación de Cita - Papus BarberShop";
            String cuerpo = construirCuerpoEmail(nombreCliente, fecha, hora, barberoNombre, 
                                                tipoCorteNombre, comentarios);

            // Usar el correo configurado en application.properties o un valor por defecto
            String correoRemitente = (emailFrom != null && !emailFrom.isEmpty()) 
                    ? emailFrom 
                    : "noreply@papusbarbershop.com";

            logger.info("Iniciando envío de correos de confirmación. Remitente: {}, Destinatarios: {}", 
                    correoRemitente, correosValidos);

            int correosEnviadosExitosamente = 0;
            for (String correo : correosValidos) {
                try {
                    SimpleMailMessage mensaje = new SimpleMailMessage();
                    mensaje.setTo(correo.trim());
                    mensaje.setSubject(asunto);
                    mensaje.setText(cuerpo);
                    mensaje.setFrom(correoRemitente);

                    mailSender.send(mensaje);
                    correosEnviadosExitosamente++;
                    logger.info("✓ Correo de confirmación enviado exitosamente a: {}", correo);
                } catch (Exception e) {
                    logger.error("✗ Error al enviar correo a {}: {}", correo, e.getMessage(), e);
                    // Continuar con los demás correos aunque uno falle
                }
            }
            
            logger.info("Proceso de envío de correos completado. Total enviados: {}/{}", 
                    correosEnviadosExitosamente, correosValidos.size());
        } catch (Exception e) {
            logger.error("Error crítico al enviar correos de confirmación: {}", e.getMessage(), e);
            throw new RuntimeException("Error al enviar correo de confirmación", e);
        }
    }

    /**
     * Construye el cuerpo del correo electrónico.
     * Los emojis Unicode funcionan correctamente en correos electrónicos.
     */
    private String construirCuerpoEmail(String nombreCliente, String fecha, String hora,
                                       String barberoNombre, String tipoCorteNombre, 
                                       String comentarios) {
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("¡Hola ").append(nombreCliente).append("! 👋\n\n");
        cuerpo.append("✨ Su cita ha sido confirmada exitosamente ✨\n\n");
        cuerpo.append("📋 Detalles de la cita:\n");
        cuerpo.append("─────────────────────────────────────\n");
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

