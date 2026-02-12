package com.papusbarbershop.controller;

import com.papusbarbershop.dto.BarberoDTO;
import com.papusbarbershop.service.BarberoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la gestión de barberos.
 */
@RestController
@RequestMapping("/barberos")
@CrossOrigin(origins = "*")
public class BarberoController {

    @Autowired
    private BarberoService barberoService;

    /**
     * Obtiene todos los barberos.
     * 
     * @return Lista de barberos
     */
    @GetMapping
    public ResponseEntity<List<BarberoDTO>> getAllBarberos() {
        List<BarberoDTO> barberos = barberoService.findAll();
        return ResponseEntity.ok(barberos);
    }

    /**
     * Obtiene un barbero por su ID.
     * 
     * @param id ID del barbero
     * @return Barbero encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<BarberoDTO> getBarberoById(@PathVariable Long id) {
        BarberoDTO barbero = barberoService.findById(id);
        return ResponseEntity.ok(barbero);
    }

    /**
     * Crea un nuevo barbero.
     * 
     * @param barberoDTO DTO con los datos del barbero
     * @return Barbero creado
     */
    @PostMapping
    public ResponseEntity<BarberoDTO> createBarbero(@RequestBody BarberoDTO barberoDTO) {
        BarberoDTO created = barberoService.create(barberoDTO);
        return ResponseEntity.ok(created);
    }

    /**
     * Actualiza un barbero existente.
     * 
     * @param id ID del barbero a actualizar
     * @param barberoDTO DTO con los datos actualizados
     * @return Barbero actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<BarberoDTO> updateBarbero(@PathVariable Long id, @RequestBody BarberoDTO barberoDTO) {
        BarberoDTO updated = barberoService.update(id, barberoDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina un barbero por su ID.
     * 
     * @param id ID del barbero a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBarbero(@PathVariable Long id) {
        barberoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

