package com.papusbarbershop.controller;

import com.papusbarbershop.dto.*;
import com.papusbarbershop.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar citas.
 */
@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "*")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private DataSource dataSource;

    /**
     * Crea una nueva cita.
     */
    @PostMapping
    public ResponseEntity<CitaDTO> crearCita(@Valid @RequestBody CitaCreateDTO citaCreateDTO) {
        CitaDTO cita = citaService.crearCita(citaCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cita);
    }

    /**
     * Obtiene todas las citas.
     */
    @GetMapping
    public ResponseEntity<List<CitaDTO>> obtenerTodas() {
        List<CitaDTO> citas = citaService.obtenerTodas();
        return ResponseEntity.ok(citas);
    }

    /**
     * Obtiene una cita por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaDTO> obtenerPorId(@PathVariable Long id) {
        CitaDTO cita = citaService.obtenerPorId(id);
        return ResponseEntity.ok(cita);
    }

    /**
     * Obtiene las citas de un barbero en una fecha específica.
     */
    @GetMapping("/barbero/{barberoId}/fecha/{fecha}")
    public ResponseEntity<List<CitaDTO>> obtenerPorBarberoYFecha(
            @PathVariable Long barberoId,
            @PathVariable LocalDate fecha) {
        List<CitaDTO> citas = citaService.obtenerPorBarberoYFecha(barberoId, fecha);
        return ResponseEntity.ok(citas);
    }

    /**
     * Obtiene la disponibilidad de barberos para una fecha específica.
     */
    @GetMapping("/disponibilidad/{fecha}")
    public ResponseEntity<List<DisponibilidadDTO>> obtenerDisponibilidad(@PathVariable LocalDate fecha) {
        List<DisponibilidadDTO> disponibilidad = citaService.obtenerDisponibilidad(fecha);
        return ResponseEntity.ok(disponibilidad);
    }

    /**
     * Cancela una cita.
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Actualiza la hora de una cita existente.
     */
    @PutMapping("/{id}/hora")
    public ResponseEntity<CitaDTO> actualizarHora(
            @PathVariable Long id,
            @Valid @RequestBody com.papusbarbershop.dto.CitaUpdateHoraDTO citaUpdateHoraDTO) {
        CitaDTO cita = citaService.actualizarHora(id, citaUpdateHoraDTO.getHora());
        return ResponseEntity.ok(cita);
    }

    /**
     * Marca una cita como completada.
     */
    @PutMapping("/{id}/completar")
    public ResponseEntity<CitaDTO> completarCita(@PathVariable Long id) {
        CitaDTO cita = citaService.completarCita(id);
        return ResponseEntity.ok(cita);
    }

    /**
     * Elimina la restricción única de la tabla citas manualmente.
     * Este endpoint permite eliminar la restricción única que impide crear nuevas citas
     * en horas donde hay citas completadas o canceladas.
     * 
     * @return Respuesta con el resultado de la operación
     */
    @PostMapping("/eliminar-restriccion-unica")
    public ResponseEntity<Map<String, Object>> eliminarRestriccionUnica() {
        Map<String, Object> respuesta = new HashMap<>();
        
        try {
            // Nombres comunes de restricciones únicas en PostgreSQL
            String[] nombresRestriccion = {
                "citas_barbero_id_fecha_hora_key",
                "citas_barbero_id_fecha_hora_uk",
                "uk_citas_barbero_fecha_hora",
                "citas_barbero_id_fecha_hora_unique"
            };
            
            boolean restriccionEliminada = false;
            String nombreRestriccionEliminada = null;
            
            // Usar JDBC directamente para operaciones DDL
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                
                // Intentar eliminar directamente las restricciones más comunes
                for (String nombreRestriccion : nombresRestriccion) {
                    try {
                        String sql = "ALTER TABLE citas DROP CONSTRAINT IF EXISTS " + nombreRestriccion;
                        statement.execute(sql);
                        restriccionEliminada = true;
                        nombreRestriccionEliminada = nombreRestriccion;
                        break; // Si una funciona, no intentar las demás
                    } catch (Exception e) {
                        // Continuar con el siguiente nombre
                    }
                }
                
                // Si no se eliminó ninguna, buscar cualquier restricción única
                if (!restriccionEliminada) {
                    String querySql = "SELECT constraint_name FROM information_schema.table_constraints " +
                                     "WHERE table_name = 'citas' AND constraint_type = 'UNIQUE'";
                    try (java.sql.ResultSet rs = statement.executeQuery(querySql)) {
                        while (rs.next()) {
                            String constraintName = rs.getString("constraint_name");
                            try {
                                String dropSql = "ALTER TABLE citas DROP CONSTRAINT " + constraintName;
                                statement.execute(dropSql);
                                restriccionEliminada = true;
                                nombreRestriccionEliminada = constraintName;
                                break;
                            } catch (Exception e) {
                                // Continuar con la siguiente
                            }
                        }
                    }
                }
            }
            
            if (restriccionEliminada) {
                respuesta.put("mensaje", "Restricción única eliminada exitosamente: " + nombreRestriccionEliminada);
                respuesta.put("restriccionEliminada", nombreRestriccionEliminada);
                respuesta.put("exito", true);
                return ResponseEntity.ok(respuesta);
            } else {
                respuesta.put("mensaje", "No se encontró ninguna restricción única en la tabla citas");
                respuesta.put("exito", true);
                return ResponseEntity.ok(respuesta);
            }
        } catch (Exception e) {
            respuesta.put("mensaje", "Error al eliminar restricción única: " + e.getMessage());
            respuesta.put("exito", false);
            respuesta.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }
}

