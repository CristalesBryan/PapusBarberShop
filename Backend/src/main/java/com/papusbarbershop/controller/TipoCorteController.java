package com.papusbarbershop.controller;

import com.papusbarbershop.dto.TipoCorteCreateDTO;
import com.papusbarbershop.dto.TipoCorteDTO;
import com.papusbarbershop.service.TipoCorteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar tipos de corte.
 */
@RestController
@RequestMapping("/api/tipos-corte")
@CrossOrigin(origins = "*")
public class TipoCorteController {

    @Autowired
    private TipoCorteService tipoCorteService;

    /**
     * Obtiene todos los tipos de corte activos.
     */
    @GetMapping
    public ResponseEntity<List<TipoCorteDTO>> obtenerTodosActivos() {
        List<TipoCorteDTO> tiposCorte = tipoCorteService.obtenerTodosActivos();
        return ResponseEntity.ok(tiposCorte);
    }

    /**
     * Obtiene todos los tipos de corte (activos e inactivos) para gestión.
     */
    @GetMapping("/todos")
    public ResponseEntity<List<TipoCorteDTO>> obtenerTodos() {
        List<TipoCorteDTO> tiposCorte = tipoCorteService.obtenerTodos();
        return ResponseEntity.ok(tiposCorte);
    }

    /**
     * Obtiene un tipo de corte por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TipoCorteDTO> obtenerPorId(@PathVariable Long id) {
        TipoCorteDTO tipoCorte = tipoCorteService.obtenerPorId(id);
        return ResponseEntity.ok(tipoCorte);
    }

    /**
     * Crea un nuevo tipo de corte.
     */
    @PostMapping
    public ResponseEntity<TipoCorteDTO> crear(@Valid @RequestBody TipoCorteCreateDTO tipoCorteCreateDTO) {
        TipoCorteDTO tipoCorte = tipoCorteService.crear(tipoCorteCreateDTO);
        return ResponseEntity.ok(tipoCorte);
    }

    /**
     * Actualiza un tipo de corte existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TipoCorteDTO> actualizar(@PathVariable Long id, 
                                                    @Valid @RequestBody TipoCorteCreateDTO tipoCorteCreateDTO) {
        TipoCorteDTO tipoCorte = tipoCorteService.actualizar(id, tipoCorteCreateDTO);
        return ResponseEntity.ok(tipoCorte);
    }

    /**
     * Elimina un tipo de corte por ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tipoCorteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

