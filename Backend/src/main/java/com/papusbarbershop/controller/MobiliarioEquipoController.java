package com.papusbarbershop.controller;

import com.papusbarbershop.dto.MobiliarioEquipoCreateDTO;
import com.papusbarbershop.dto.MobiliarioEquipoDTO;
import com.papusbarbershop.service.MobiliarioEquipoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la gestión de mobiliario y equipo.
 */
@RestController
@RequestMapping("/mobiliario-equipo")
@CrossOrigin(origins = "*")
public class MobiliarioEquipoController {

    @Autowired
    private MobiliarioEquipoService mobiliarioEquipoService;

    /**
     * Crea un nuevo elemento de mobiliario o equipo.
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * @param mobiliarioEquipoCreateDTO DTO con los datos del elemento
     * @return Elemento creado
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MobiliarioEquipoDTO> createMobiliarioEquipo(
            @Valid @RequestBody MobiliarioEquipoCreateDTO mobiliarioEquipoCreateDTO) {
        MobiliarioEquipoDTO mobiliarioEquipo = mobiliarioEquipoService.create(mobiliarioEquipoCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(mobiliarioEquipo);
    }

    /**
     * Actualiza un elemento existente.
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * @param id ID del elemento
     * @param mobiliarioEquipoCreateDTO DTO con los datos actualizados
     * @return Elemento actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MobiliarioEquipoDTO> updateMobiliarioEquipo(
            @PathVariable Long id,
            @Valid @RequestBody MobiliarioEquipoCreateDTO mobiliarioEquipoCreateDTO) {
        MobiliarioEquipoDTO mobiliarioEquipo = mobiliarioEquipoService.update(id, mobiliarioEquipoCreateDTO);
        return ResponseEntity.ok(mobiliarioEquipo);
    }

    /**
     * Obtiene todos los elementos.
     * 
     * @return Lista de elementos
     */
    @GetMapping
    public ResponseEntity<List<MobiliarioEquipoDTO>> getAllMobiliarioEquipo() {
        List<MobiliarioEquipoDTO> elementos = mobiliarioEquipoService.findAll();
        return ResponseEntity.ok(elementos);
    }

    /**
     * Obtiene un elemento por su ID.
     * 
     * @param id ID del elemento
     * @return Elemento encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<MobiliarioEquipoDTO> getMobiliarioEquipoById(@PathVariable Long id) {
        MobiliarioEquipoDTO mobiliarioEquipo = mobiliarioEquipoService.findById(id);
        return ResponseEntity.ok(mobiliarioEquipo);
    }

    /**
     * Elimina un elemento por su ID.
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * @param id ID del elemento a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMobiliarioEquipo(@PathVariable Long id) {
        mobiliarioEquipoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

