package com.papusbarbershop.service;

import com.papusbarbershop.dto.BarberoDTO;
import com.papusbarbershop.entity.Barbero;
import com.papusbarbershop.exception.RecursoNoEncontradoException;
import com.papusbarbershop.repository.BarberoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de barberos.
 */
@Service
public class BarberoService {

    @Autowired
    private BarberoRepository barberoRepository;

    /**
     * Obtiene todos los barberos.
     * 
     * @return Lista de barberos
     */
    public List<BarberoDTO> findAll() {
        return barberoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un barbero por su ID.
     * 
     * @param id ID del barbero
     * @return Barbero encontrado
     * @throws RecursoNoEncontradoException si no se encuentra el barbero
     */
    public BarberoDTO findById(Long id) {
        Barbero barbero = barberoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Barbero con ID " + id + " no encontrado"));
        return convertToDTO(barbero);
    }

    /**
     * Obtiene la entidad Barbero por su ID.
     * 
     * @param id ID del barbero
     * @return Entidad Barbero
     * @throws RecursoNoEncontradoException si no se encuentra el barbero
     */
    public Barbero findEntityById(Long id) {
        return barberoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Barbero con ID " + id + " no encontrado"));
    }

    /**
     * Obtiene todas las entidades Barbero.
     * 
     * @return Lista de entidades Barbero
     */
    public List<Barbero> findAllEntities() {
        return barberoRepository.findAll();
    }

    /**
     * Crea un nuevo barbero.
     * 
     * @param barberoDTO DTO con los datos del barbero
     * @return DTO del barbero creado
     */
    @Transactional
    public BarberoDTO create(BarberoDTO barberoDTO) {
        Barbero barbero = new Barbero();
        barbero.setNombre(barberoDTO.getNombre());
        barbero.setPorcentajeServicio(barberoDTO.getPorcentajeServicio());
        barbero.setCorreo(barberoDTO.getCorreo());
        
        Barbero saved = barberoRepository.save(barbero);
        return convertToDTO(saved);
    }

    /**
     * Actualiza un barbero existente.
     * 
     * @param id ID del barbero a actualizar
     * @param barberoDTO DTO con los datos actualizados
     * @return DTO del barbero actualizado
     * @throws RecursoNoEncontradoException si no se encuentra el barbero
     */
    @Transactional
    public BarberoDTO update(Long id, BarberoDTO barberoDTO) {
        Barbero barbero = barberoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Barbero con ID " + id + " no encontrado"));
        
        barbero.setNombre(barberoDTO.getNombre());
        barbero.setPorcentajeServicio(barberoDTO.getPorcentajeServicio());
        barbero.setCorreo(barberoDTO.getCorreo());
        
        Barbero saved = barberoRepository.save(barbero);
        return convertToDTO(saved);
    }

    /**
     * Elimina un barbero por su ID.
     * 
     * @param id ID del barbero a eliminar
     * @throws RecursoNoEncontradoException si no se encuentra el barbero
     */
    @Transactional
    public void delete(Long id) {
        if (!barberoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Barbero con ID " + id + " no encontrado");
        }
        barberoRepository.deleteById(id);
    }

    /**
     * Convierte una entidad Barbero a DTO.
     * 
     * @param barbero Entidad Barbero
     * @return DTO de Barbero
     */
    private BarberoDTO convertToDTO(Barbero barbero) {
        return new BarberoDTO(
                barbero.getId(),
                barbero.getNombre(),
                barbero.getPorcentajeServicio(),
                barbero.getCorreo()
        );
    }
}

