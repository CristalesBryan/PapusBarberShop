package com.papusbarbershop.controller;

import com.papusbarbershop.dto.HorarioCreateDTO;
import com.papusbarbershop.dto.HorarioDTO;
import com.papusbarbershop.service.HorarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la gestión de horarios de barberos.
 */
@RestController
@RequestMapping("/horarios")
@CrossOrigin(origins = "*")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    /**
     * Obtiene todos los horarios.
     * 
     * @return Lista de horarios
     */
    @GetMapping
    public ResponseEntity<List<HorarioDTO>> getAllHorarios() {
        List<HorarioDTO> horarios = horarioService.findAll();
        return ResponseEntity.ok(horarios);
    }

    /**
     * Obtiene un horario por su ID.
     * 
     * @param id ID del horario
     * @return Horario encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<HorarioDTO> getHorarioById(@PathVariable Long id) {
        HorarioDTO horario = horarioService.findById(id);
        return ResponseEntity.ok(horario);
    }

    /**
     * Obtiene todos los horarios de un barbero específico.
     * 
     * @param barberoId ID del barbero
     * @return Lista de horarios del barbero
     */
    @GetMapping("/barbero/{barberoId}")
    public ResponseEntity<List<HorarioDTO>> getHorariosByBarberoId(@PathVariable Long barberoId) {
        List<HorarioDTO> horarios = horarioService.findByBarberoId(barberoId);
        return ResponseEntity.ok(horarios);
    }

    /**
     * Desactiva automáticamente todos los horarios pasados.
     * 
     * Este endpoint permite ejecutar manualmente la función de desactivación
     * de horarios pasados. Normalmente se ejecuta automáticamente al iniciar la aplicación.
     * 
     * @return Respuesta con el número de horarios desactivados
     */
    @PostMapping("/desactivar-pasados")
    public ResponseEntity<Map<String, Object>> desactivarHorariosPasados() {
        int horariosDesactivados = horarioService.desactivarHorariosPasados();
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Proceso completado");
        respuesta.put("horariosDesactivados", horariosDesactivados);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Crea un nuevo horario.
     * 
     * @param horarioCreateDTO DTO con los datos del horario
     * @return Horario creado
     */
    @PostMapping
    public ResponseEntity<HorarioDTO> createHorario(@Valid @RequestBody HorarioCreateDTO horarioCreateDTO) {
        HorarioDTO horario = horarioService.create(horarioCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(horario);
    }

    /**
     * Actualiza un horario existente.
     * 
     * @param id ID del horario
     * @param horarioCreateDTO DTO con los datos actualizados
     * @return Horario actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<HorarioDTO> updateHorario(
            @PathVariable Long id,
            @Valid @RequestBody HorarioCreateDTO horarioCreateDTO) {
        HorarioDTO horario = horarioService.update(id, horarioCreateDTO);
        return ResponseEntity.ok(horario);
    }

    /**
     * Elimina un horario.
     * 
     * @param id ID del horario
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHorario(@PathVariable Long id) {
        horarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

