package com.papusbarbershop.service;

import com.papusbarbershop.dto.MobiliarioEquipoCreateDTO;
import com.papusbarbershop.dto.MobiliarioEquipoDTO;
import com.papusbarbershop.entity.MobiliarioEquipo;
import com.papusbarbershop.exception.RecursoNoEncontradoException;
import com.papusbarbershop.repository.MobiliarioEquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de mobiliario y equipo.
 */
@Service
public class MobiliarioEquipoService {

    @Autowired
    private MobiliarioEquipoRepository mobiliarioEquipoRepository;

    /**
     * Crea un nuevo elemento de mobiliario o equipo.
     * 
     * @param mobiliarioEquipoCreateDTO DTO con los datos del elemento
     * @return Elemento creado
     */
    @Transactional
    public MobiliarioEquipoDTO create(MobiliarioEquipoCreateDTO mobiliarioEquipoCreateDTO) {
        MobiliarioEquipo mobiliarioEquipo = new MobiliarioEquipo();
        mobiliarioEquipo.setNombre(mobiliarioEquipoCreateDTO.getNombre());
        mobiliarioEquipo.setDescripcion(mobiliarioEquipoCreateDTO.getDescripcion());
        mobiliarioEquipo.setCategoria(mobiliarioEquipoCreateDTO.getCategoria());
        mobiliarioEquipo.setEstado(mobiliarioEquipoCreateDTO.getEstado());
        mobiliarioEquipo.setFechaAdquisicion(mobiliarioEquipoCreateDTO.getFechaAdquisicion());
        mobiliarioEquipo.setValor(mobiliarioEquipoCreateDTO.getValor());
        mobiliarioEquipo.setCantidad(mobiliarioEquipoCreateDTO.getCantidad());
        mobiliarioEquipo.setUbicacion(mobiliarioEquipoCreateDTO.getUbicacion());
        mobiliarioEquipo.setNumeroSerie(mobiliarioEquipoCreateDTO.getNumeroSerie());

        MobiliarioEquipo saved = mobiliarioEquipoRepository.save(mobiliarioEquipo);
        return convertToDTO(saved);
    }

    /**
     * Actualiza un elemento existente.
     * 
     * @param id ID del elemento
     * @param mobiliarioEquipoCreateDTO DTO con los datos actualizados
     * @return Elemento actualizado
     */
    @Transactional
    public MobiliarioEquipoDTO update(Long id, MobiliarioEquipoCreateDTO mobiliarioEquipoCreateDTO) {
        MobiliarioEquipo mobiliarioEquipo = mobiliarioEquipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Elemento con ID " + id + " no encontrado"));

        mobiliarioEquipo.setNombre(mobiliarioEquipoCreateDTO.getNombre());
        mobiliarioEquipo.setDescripcion(mobiliarioEquipoCreateDTO.getDescripcion());
        mobiliarioEquipo.setCategoria(mobiliarioEquipoCreateDTO.getCategoria());
        mobiliarioEquipo.setEstado(mobiliarioEquipoCreateDTO.getEstado());
        mobiliarioEquipo.setFechaAdquisicion(mobiliarioEquipoCreateDTO.getFechaAdquisicion());
        mobiliarioEquipo.setValor(mobiliarioEquipoCreateDTO.getValor());
        mobiliarioEquipo.setCantidad(mobiliarioEquipoCreateDTO.getCantidad());
        mobiliarioEquipo.setUbicacion(mobiliarioEquipoCreateDTO.getUbicacion());
        mobiliarioEquipo.setNumeroSerie(mobiliarioEquipoCreateDTO.getNumeroSerie());

        MobiliarioEquipo saved = mobiliarioEquipoRepository.save(mobiliarioEquipo);
        return convertToDTO(saved);
    }

    /**
     * Obtiene todos los elementos.
     * 
     * @return Lista de elementos
     */
    public List<MobiliarioEquipoDTO> findAll() {
        return mobiliarioEquipoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un elemento por su ID.
     * 
     * @param id ID del elemento
     * @return Elemento encontrado
     * @throws RecursoNoEncontradoException si no se encuentra el elemento
     */
    public MobiliarioEquipoDTO findById(Long id) {
        MobiliarioEquipo mobiliarioEquipo = mobiliarioEquipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Elemento con ID " + id + " no encontrado"));
        return convertToDTO(mobiliarioEquipo);
    }

    /**
     * Elimina un elemento por su ID.
     * 
     * @param id ID del elemento a eliminar
     * @throws RecursoNoEncontradoException si no se encuentra el elemento
     */
    @Transactional
    public void delete(Long id) {
        MobiliarioEquipo mobiliarioEquipo = mobiliarioEquipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Elemento con ID " + id + " no encontrado"));
        mobiliarioEquipoRepository.delete(mobiliarioEquipo);
    }

    /**
     * Convierte una entidad MobiliarioEquipo a DTO.
     * 
     * @param mobiliarioEquipo Entidad MobiliarioEquipo
     * @return DTO de MobiliarioEquipo
     */
    private MobiliarioEquipoDTO convertToDTO(MobiliarioEquipo mobiliarioEquipo) {
        return new MobiliarioEquipoDTO(
                mobiliarioEquipo.getId(),
                mobiliarioEquipo.getNombre(),
                mobiliarioEquipo.getDescripcion(),
                mobiliarioEquipo.getCategoria(),
                mobiliarioEquipo.getEstado(),
                mobiliarioEquipo.getFechaAdquisicion(),
                mobiliarioEquipo.getValor(),
                mobiliarioEquipo.getCantidad(),
                mobiliarioEquipo.getUbicacion(),
                mobiliarioEquipo.getNumeroSerie(),
                mobiliarioEquipo.getCreatedAt(),
                mobiliarioEquipo.getUpdatedAt()
        );
    }
}

