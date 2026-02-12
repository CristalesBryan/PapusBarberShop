package com.papusbarbershop.controller;

import com.papusbarbershop.dto.ResumenBarberoDTO;
import com.papusbarbershop.dto.ServicioCreateDTO;
import com.papusbarbershop.dto.ServicioDTO;
import com.papusbarbershop.service.ReporteService;
import com.papusbarbershop.service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para la gestión de servicios (cortes).
 */
@RestController
@RequestMapping("/servicios")
@CrossOrigin(origins = "*")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private ReporteService reporteService;

    /**
     * Crea un nuevo servicio.
     * 
     * @param servicioCreateDTO DTO con los datos del servicio
     * @return Servicio creado
     */
    @PostMapping
    public ResponseEntity<ServicioDTO> createServicio(@Valid @RequestBody ServicioCreateDTO servicioCreateDTO) {
        ServicioDTO servicio = servicioService.create(servicioCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio);
    }

    /**
     * Obtiene todos los servicios.
     * 
     * @return Lista de servicios
     */
    @GetMapping
    public ResponseEntity<List<ServicioDTO>> getAllServicios() {
        List<ServicioDTO> servicios = servicioService.findAll();
        return ResponseEntity.ok(servicios);
    }

    /**
     * Obtiene todos los servicios de una fecha específica.
     * 
     * @param fecha Fecha a buscar
     * @return Lista de servicios
     */
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<ServicioDTO>> getServiciosByFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<ServicioDTO> servicios = servicioService.findByFecha(fecha);
        return ResponseEntity.ok(servicios);
    }

    /**
     * Obtiene el resumen diario de servicios.
     * 
     * @return Resumen diario
     */
    @GetMapping("/resumen/diario")
    public ResponseEntity<com.papusbarbershop.dto.ResumenDiarioDTO> getResumenDiario() {
        com.papusbarbershop.dto.ResumenDiarioDTO resumen = reporteService.generarResumenDiario(LocalDate.now());
        return ResponseEntity.ok(resumen);
    }

    /**
     * Obtiene el resumen mensual de servicios.
     * 
     * @return Resumen mensual
     */
    @GetMapping("/resumen/mensual")
    public ResponseEntity<com.papusbarbershop.dto.ResumenMensualDTO> getResumenMensual() {
        com.papusbarbershop.dto.ResumenMensualDTO resumen = reporteService.generarResumenMensual(
                java.time.YearMonth.now());
        return ResponseEntity.ok(resumen);
    }

    /**
     * Obtiene el resumen de servicios de un barbero específico.
     * 
     * @param id ID del barbero
     * @return Resumen del barbero
     */
    @GetMapping("/resumen/barbero/{id}")
    public ResponseEntity<ResumenBarberoDTO> getResumenBarbero(@PathVariable Long id) {
        // Este endpoint requiere implementación adicional en el servicio
        // Por ahora retornamos un resumen básico
        return ResponseEntity.ok(new ResumenBarberoDTO());
    }

    /**
     * Obtiene un servicio por su ID.
     * 
     * @param id ID del servicio
     * @return Servicio encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServicioDTO> getServicioById(@PathVariable Long id) {
        ServicioDTO servicio = servicioService.findById(id);
        return ResponseEntity.ok(servicio);
    }

    /**
     * Actualiza un servicio existente.
     * 
     * @param id ID del servicio
     * @param servicioCreateDTO DTO con los datos actualizados
     * @return Servicio actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServicioDTO> updateServicio(
            @PathVariable Long id,
            @Valid @RequestBody ServicioCreateDTO servicioCreateDTO) {
        ServicioDTO servicio = servicioService.update(id, servicioCreateDTO);
        return ResponseEntity.ok(servicio);
    }

    /**
     * Elimina un servicio por su ID.
     * 
     * @param id ID del servicio a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServicio(@PathVariable Long id) {
        servicioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

