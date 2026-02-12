package com.papusbarbershop.service;

import com.papusbarbershop.dto.TipoCorteCreateDTO;
import com.papusbarbershop.dto.TipoCorteDTO;
import com.papusbarbershop.entity.TipoCorte;
import com.papusbarbershop.entity.Barbero;
import com.papusbarbershop.exception.RecursoNoEncontradoException;
import com.papusbarbershop.repository.TipoCorteRepository;
import com.papusbarbershop.service.BarberoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar tipos de corte.
 */
@Service
@Transactional
public class TipoCorteService {

    @Autowired
    private TipoCorteRepository tipoCorteRepository;

    @Autowired
    private BarberoService barberoService;

    /**
     * Obtiene todos los tipos de corte activos.
     */
    public List<TipoCorteDTO> obtenerTodosActivos() {
        return tipoCorteRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los tipos de corte (activos e inactivos).
     */
    public List<TipoCorteDTO> obtenerTodos() {
        return tipoCorteRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un tipo de corte por ID.
     */
    public TipoCorteDTO obtenerPorId(Long id) {
        TipoCorte tipoCorte = tipoCorteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de corte no encontrado con ID: " + id));
        return convertirADTO(tipoCorte);
    }

    /**
     * Obtiene la entidad TipoCorte por ID.
     */
    public TipoCorte obtenerEntidadPorId(Long id) {
        return tipoCorteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de corte no encontrado con ID: " + id));
    }

    /**
     * Crea un nuevo tipo de corte.
     */
    public TipoCorteDTO crear(TipoCorteCreateDTO tipoCorteCreateDTO) {
        TipoCorte tipoCorte = new TipoCorte();
        tipoCorte.setNombre(tipoCorteCreateDTO.getNombre());
        tipoCorte.setDescripcion(tipoCorteCreateDTO.getDescripcion());
        tipoCorte.setTiempoMinutos(tipoCorteCreateDTO.getTiempoMinutos());
        tipoCorte.setPrecio(tipoCorteCreateDTO.getPrecio());
        tipoCorte.setActivo(tipoCorteCreateDTO.getActivo() != null ? tipoCorteCreateDTO.getActivo() : true);
        
        // Asignar barbero si se proporciona
        if (tipoCorteCreateDTO.getBarberoId() != null && tipoCorteCreateDTO.getBarberoId() > 0) {
            Barbero barbero = barberoService.findEntityById(tipoCorteCreateDTO.getBarberoId());
            tipoCorte.setBarbero(barbero);
        }
        
        TipoCorte saved = tipoCorteRepository.save(tipoCorte);
        return convertirADTO(saved);
    }

    /**
     * Actualiza un tipo de corte existente.
     */
    public TipoCorteDTO actualizar(Long id, TipoCorteCreateDTO tipoCorteCreateDTO) {
        TipoCorte tipoCorte = tipoCorteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de corte no encontrado con ID: " + id));
        
        tipoCorte.setNombre(tipoCorteCreateDTO.getNombre());
        tipoCorte.setDescripcion(tipoCorteCreateDTO.getDescripcion());
        tipoCorte.setTiempoMinutos(tipoCorteCreateDTO.getTiempoMinutos());
        tipoCorte.setPrecio(tipoCorteCreateDTO.getPrecio());
        if (tipoCorteCreateDTO.getActivo() != null) {
            tipoCorte.setActivo(tipoCorteCreateDTO.getActivo());
        }
        
        // Actualizar barbero asignado
        if (tipoCorteCreateDTO.getBarberoId() != null && tipoCorteCreateDTO.getBarberoId() > 0) {
            Barbero barbero = barberoService.findEntityById(tipoCorteCreateDTO.getBarberoId());
            tipoCorte.setBarbero(barbero);
        } else {
            // Si no se proporciona barberoId o es 0, eliminar la asignación
            tipoCorte.setBarbero(null);
        }
        
        TipoCorte saved = tipoCorteRepository.save(tipoCorte);
        return convertirADTO(saved);
    }

    /**
     * Elimina un tipo de corte por ID.
     */
    public void eliminar(Long id) {
        TipoCorte tipoCorte = tipoCorteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de corte no encontrado con ID: " + id));
        tipoCorteRepository.delete(tipoCorte);
    }

    /**
     * Convierte una entidad TipoCorte a DTO.
     */
    private TipoCorteDTO convertirADTO(TipoCorte tipoCorte) {
        TipoCorteDTO dto = new TipoCorteDTO();
        dto.setId(tipoCorte.getId());
        dto.setNombre(tipoCorte.getNombre());
        dto.setDescripcion(tipoCorte.getDescripcion());
        dto.setTiempoMinutos(tipoCorte.getTiempoMinutos());
        dto.setPrecio(tipoCorte.getPrecio());
        dto.setActivo(tipoCorte.getActivo());
        
        // Incluir información del barbero si está asignado
        if (tipoCorte.getBarbero() != null) {
            dto.setBarberoId(tipoCorte.getBarbero().getId());
            dto.setBarberoNombre(tipoCorte.getBarbero().getNombre());
        }
        
        return dto;
    }
}

