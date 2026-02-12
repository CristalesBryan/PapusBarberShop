package com.papusbarbershop.service;

import com.papusbarbershop.dto.ServicioCreateDTO;
import com.papusbarbershop.dto.ServicioDTO;
import com.papusbarbershop.entity.Barbero;
import com.papusbarbershop.entity.Servicio;
import com.papusbarbershop.exception.RecursoNoEncontradoException;
import com.papusbarbershop.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de servicios (cortes).
 */
@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private BarberoService barberoService;

    /**
     * Crea un nuevo servicio.
     * 
     * @param servicioCreateDTO DTO con los datos del servicio
     * @return Servicio creado
     */
    @Transactional
    public ServicioDTO create(ServicioCreateDTO servicioCreateDTO) {
        Barbero barbero = barberoService.findEntityById(servicioCreateDTO.getBarberoId());

        Servicio servicio = new Servicio();
        servicio.setFecha(servicioCreateDTO.getFecha());
        servicio.setHora(servicioCreateDTO.getHora());
        servicio.setBarbero(barbero);
        servicio.setTipoCorte(servicioCreateDTO.getTipoCorte());
        servicio.setMetodoPago(servicioCreateDTO.getMetodoPago());
        servicio.setPrecio(servicioCreateDTO.getPrecio());

        Servicio saved = servicioRepository.save(servicio);
        return convertToDTO(saved);
    }

    /**
     * Obtiene todos los servicios.
     * 
     * @return Lista de servicios
     */
    public List<ServicioDTO> findAll() {
        return servicioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los servicios de una fecha específica.
     * 
     * @param fecha Fecha a buscar
     * @return Lista de servicios
     */
    public List<ServicioDTO> findByFecha(LocalDate fecha) {
        return servicioRepository.findByFecha(fecha).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un servicio por su ID.
     * 
     * @param id ID del servicio
     * @return Servicio encontrado
     * @throws RecursoNoEncontradoException si no se encuentra el servicio
     */
    public ServicioDTO findById(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio con ID " + id + " no encontrado"));
        return convertToDTO(servicio);
    }

    /**
     * Actualiza un servicio existente.
     * 
     * @param id ID del servicio
     * @param servicioCreateDTO DTO con los datos actualizados
     * @return Servicio actualizado
     * @throws RecursoNoEncontradoException si no se encuentra el servicio
     */
    @Transactional
    public ServicioDTO update(Long id, ServicioCreateDTO servicioCreateDTO) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio con ID " + id + " no encontrado"));

        Barbero barbero = barberoService.findEntityById(servicioCreateDTO.getBarberoId());

        servicio.setFecha(servicioCreateDTO.getFecha());
        servicio.setHora(servicioCreateDTO.getHora());
        servicio.setBarbero(barbero);
        servicio.setTipoCorte(servicioCreateDTO.getTipoCorte());
        servicio.setMetodoPago(servicioCreateDTO.getMetodoPago());
        servicio.setPrecio(servicioCreateDTO.getPrecio());

        Servicio saved = servicioRepository.save(servicio);
        return convertToDTO(saved);
    }

    /**
     * Elimina un servicio por su ID.
     * 
     * @param id ID del servicio a eliminar
     * @throws RecursoNoEncontradoException si no se encuentra el servicio
     */
    @Transactional
    public void delete(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio con ID " + id + " no encontrado"));
        servicioRepository.delete(servicio);
    }

    /**
     * Convierte una entidad Servicio a DTO.
     * 
     * @param servicio Entidad Servicio
     * @return DTO de Servicio
     */
    private ServicioDTO convertToDTO(Servicio servicio) {
        return new ServicioDTO(
                servicio.getId(),
                servicio.getFecha(),
                servicio.getHora(),
                servicio.getBarbero().getId(),
                servicio.getBarbero().getNombre(),
                servicio.getTipoCorte(),
                servicio.getMetodoPago(),
                servicio.getPrecio()
        );
    }
}

