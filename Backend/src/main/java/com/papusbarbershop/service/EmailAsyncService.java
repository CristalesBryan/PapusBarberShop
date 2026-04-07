package com.papusbarbershop.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio asíncrono para el envío de correos electrónicos usando Resend.
 *
 * Al agendar una cita se envían automáticamente 3 correos:
 * - Al cliente: confirmación con datos de la cita (fecha, hora, barbero, tipo de corte).
 * - Al barbero seleccionado: notificación de nueva cita con datos del cliente.
 * - Al admin de la barbería: notificación general de nueva cita.
 */
@Service
public class EmailAsyncService {

    private static final Logger logger = LoggerFactory.getLogger(EmailAsyncService.class);

    @Autowired
    private EmailExecutor emailExecutor;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from.email:Citas Papus BarberShop <citas@papusbarbershop.com>}")
    private String emailFrom;

    @Value("${resend.admin.email:}")
    private String adminEmail;

    private Resend resend;

    @PostConstruct
    public void init() {
        logger.info("=== Validando configuración de Resend ===");
        if (resendApiKey == null || resendApiKey.isBlank()) {
            logger.warn("⚠️  RESEND_API_KEY no está configurada. Los correos NO se enviarán.");
            logger.warn("⚠️  Configura resend.api-key o la variable de entorno RESEND_API_KEY.");
            resend = null;
        } else {
            resend = new Resend(resendApiKey);
            logger.info("✓ Resend configurado correctamente");
            logger.info("✓ Email remitente: {}", emailFrom);
            if (adminEmail == null || adminEmail.isBlank()) {
                logger.info("  (RESEND_ADMIN_EMAIL no configurado: no se enviará notificación al admin)");
            } else {
                logger.info("✓ Email admin para notificaciones: {}", adminEmail);
            }
        }
        logger.info("=== Validación de Resend completada ===");
    }

    /**
     * Envía los 3 correos de cita de forma ASÍNCRONA: al cliente, al barbero y al admin.
     *
     * @param correosCliente     Lista de correos del cliente (confirmación)
     * @param nombreCliente      Nombre del cliente
     * @param correoCliente      Correo principal del cliente (para datos en correo al barbero/admin)
     * @param telefonoCliente    Teléfono del cliente (opcional)
     * @param fecha              Fecha de la cita (formateada)
     * @param hora               Hora de la cita (formateada)
     * @param barberoNombre      Nombre del barbero
     * @param barberoCorreo      Correo del barbero (para notificación; puede ser null/vacío)
     * @param tipoCorteNombre    Nombre del tipo de corte
     * @param comentarios        Comentarios adicionales
     */
    public void enviarConfirmacionCitaAsync(List<String> correosCliente, String nombreCliente,
                                            String correoCliente, String telefonoCliente,
                                            String fecha, String hora, String barberoNombre,
                                            String barberoCorreo, String tipoCorteNombre,
                                            String comentarios) {
        emailExecutor.ejecutarEnvioAsincrono(() -> {
            enviarCorreosCita(correosCliente, nombreCliente, correoCliente, telefonoCliente,
                    fecha, hora, barberoNombre, barberoCorreo, tipoCorteNombre, comentarios);
        });
        logger.info("Tarea de envío de correos de cita enviada al pool asíncrono (cliente, barbero, admin).");
    }

    /**
     * Envía un correo genérico de forma asíncrona.
     */
    public void enviarCorreoAsync(String destinatario, String asunto, String mensaje) {
        if (destinatario == null || destinatario.trim().isEmpty()) {
            logger.warn("No se proporcionó destinatario para el correo");
            return;
        }
        emailExecutor.ejecutarEnvioAsincrono(() -> enviarCorreoSimple(destinatario, asunto, mensaje));
        logger.info("Tarea de envío de correo genérico enviada al pool asíncrono. Destinatario: {}", destinatario);
    }

    private void enviarCorreosCita(List<String> correosCliente, String nombreCliente,
                                   String correoCliente, String telefonoCliente,
                                   String fecha, String hora, String barberoNombre,
                                   String barberoCorreo, String tipoCorteNombre,
                                   String comentarios) {
        if (resend == null) {
            logger.warn("Resend no está configurado. No se enviarán correos.");
            return;
        }

        List<String> correosValidos = correosCliente != null
                ? correosCliente.stream().filter(c -> c != null && !c.trim().isEmpty()).toList()
                : List.of();

        try {
            int enviados = 0;

            // 1. Correo al/los cliente(s): confirmación con datos de la cita
            String htmlCliente = construirCuerpoEmailHtmlCliente(nombreCliente, fecha, hora, barberoNombre, tipoCorteNombre, comentarios);
            String asuntoCliente = "Confirmación de Cita - Papus BarberShop";
            for (String to : correosValidos) {
                if (enviarEmail(to, asuntoCliente, htmlCliente)) {
                    enviados++;
                }
            }

            // 2. Correo al barbero: notificación de nueva cita con datos del cliente
            if (barberoCorreo != null && !barberoCorreo.trim().isEmpty()) {
                String htmlBarbero = construirCuerpoEmailHtmlBarbero(nombreCliente, correoCliente, telefonoCliente, fecha, hora, tipoCorteNombre, comentarios);
                if (enviarEmail(barberoCorreo.trim(), "Nueva cita asignada - Papus BarberShop", htmlBarbero)) {
                    enviados++;
                }
            }

            // 3. Correo al admin: notificación general de nueva cita
            if (adminEmail != null && !adminEmail.trim().isEmpty()) {
                String htmlAdmin = construirCuerpoEmailHtmlAdmin(nombreCliente, correoCliente, telefonoCliente, fecha, hora, barberoNombre, tipoCorteNombre, comentarios);
                if (enviarEmail(adminEmail.trim(), "Nueva cita registrada - Papus BarberShop", htmlAdmin)) {
                    enviados++;
                }
            }

            logger.info("Envío de correos de cita completado. Total enviados: {}", enviados);
        } catch (Exception e) {
            logger.error("Error en envío asíncrono de correos de cita: {}", e.getMessage(), e);
        }
    }

    private boolean enviarEmail(String to, String subject, String html) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(emailFrom)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();
            CreateEmailResponse response = resend.emails().send(params);
            logger.info("✓ Correo enviado a {} (id: {})", to, response.getId());
            return true;
        } catch (ResendException e) {
            logger.error("✗ Error al enviar correo a {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    private void enviarCorreoSimple(String destinatario, String asunto, String mensaje) {
        if (resend == null) {
            logger.warn("Resend no está configurado. No se enviará el correo.");
            return;
        }
        String html = "<html><body style=\"font-family: Arial, sans-serif;\"><p>" + escapeHtml(mensaje) + "</p></body></html>";
        enviarEmail(destinatario.trim(), asunto, html);
    }

    private String construirCuerpoEmailHtmlCliente(String nombreCliente, String fecha, String hora,
                                                   String barberoNombre, String tipoCorteNombre,
                                                   String comentarios) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">");
        html.append("<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">");
        html.append("<h2 style=\"color: #2c3e50;\">¡Hola ").append(escapeHtml(nombreCliente)).append("! 👋</h2>");
        html.append("<p style=\"font-size: 18px; color: #27ae60;\">✨ Su cita ha sido confirmada exitosamente ✨</p>");
        html.append("<div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;\">");
        html.append("<h3 style=\"color: #2c3e50; margin-top: 0;\">📋 Detalles de la cita:</h3>");
        html.append("<p><strong>📅 Fecha:</strong> ").append(escapeHtml(fecha)).append("</p>");
        html.append("<p><strong>🕐 Hora:</strong> ").append(escapeHtml(hora)).append("</p>");
        html.append("<p><strong>💇 Barbero:</strong> ").append(escapeHtml(barberoNombre)).append("</p>");
        html.append("<p><strong>✂️ Tipo de Corte:</strong> ").append(escapeHtml(tipoCorteNombre)).append("</p>");
        if (comentarios != null && !comentarios.trim().isEmpty()) {
            html.append("<p><strong>💬 Comentarios:</strong> ").append(escapeHtml(comentarios)).append("</p>");
        }
        html.append("</div>");
        html.append("<p style=\"font-size: 16px; color: #2c3e50;\">🎯 Esperamos verle pronto en Papus BarberShop 🎯</p>");
        html.append("<p>Saludos cordiales,<br>Equipo Papus BarberShop 💈</p>");
        html.append("</div></body></html>");
        return html.toString();
    }

    private String construirCuerpoEmailHtmlBarbero(String nombreCliente, String correoCliente, String telefonoCliente,
                                                  String fecha, String hora, String tipoCorteNombre, String comentarios) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">");
        html.append("<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">");
        html.append("<h2 style=\"color: #2c3e50;\">Nueva cita asignada</h2>");
        html.append("<p>Se ha registrado una nueva cita con los siguientes datos del cliente:</p>");
        html.append("<div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;\">");
        html.append("<p><strong>Nombre:</strong> ").append(escapeHtml(nombreCliente)).append("</p>");
        html.append("<p><strong>Correo:</strong> ").append(escapeHtml(correoCliente != null ? correoCliente : "-")).append("</p>");
        html.append("<p><strong>Teléfono:</strong> ").append(escapeHtml(telefonoCliente != null ? telefonoCliente : "-")).append("</p>");
        html.append("<p><strong>Fecha:</strong> ").append(escapeHtml(fecha)).append("</p>");
        html.append("<p><strong>Hora:</strong> ").append(escapeHtml(hora)).append("</p>");
        html.append("<p><strong>Servicio:</strong> ").append(escapeHtml(tipoCorteNombre)).append("</p>");
        if (comentarios != null && !comentarios.trim().isEmpty()) {
            html.append("<p><strong>Comentarios:</strong> ").append(escapeHtml(comentarios)).append("</p>");
        }
        html.append("</div>");
        html.append("<p>Saludos,<br>Papus BarberShop</p>");
        html.append("</div></body></html>");
        return html.toString();
    }

    private String construirCuerpoEmailHtmlAdmin(String nombreCliente, String correoCliente, String telefonoCliente,
                                                String fecha, String hora, String barberoNombre, String tipoCorteNombre, String comentarios) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">");
        html.append("<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">");
        html.append("<h2 style=\"color: #2c3e50;\">Nueva cita registrada en el sistema</h2>");
        html.append("<p>Se ha agendado una nueva cita con los siguientes datos:</p>");
        html.append("<div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;\">");
        html.append("<p><strong>Cliente:</strong> ").append(escapeHtml(nombreCliente)).append("</p>");
        html.append("<p><strong>Correo cliente:</strong> ").append(escapeHtml(correoCliente != null ? correoCliente : "-")).append("</p>");
        html.append("<p><strong>Teléfono:</strong> ").append(escapeHtml(telefonoCliente != null ? telefonoCliente : "-")).append("</p>");
        html.append("<p><strong>Fecha:</strong> ").append(escapeHtml(fecha)).append(" | <strong>Hora:</strong> ").append(escapeHtml(hora)).append("</p>");
        html.append("<p><strong>Barbero:</strong> ").append(escapeHtml(barberoNombre)).append("</p>");
        html.append("<p><strong>Servicio:</strong> ").append(escapeHtml(tipoCorteNombre)).append("</p>");
        if (comentarios != null && !comentarios.trim().isEmpty()) {
            html.append("<p><strong>Comentarios:</strong> ").append(escapeHtml(comentarios)).append("</p>");
        }
        html.append("</div>");
        html.append("<p>Saludos,<br>Papus BarberShop</p>");
        html.append("</div></body></html>");
        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
