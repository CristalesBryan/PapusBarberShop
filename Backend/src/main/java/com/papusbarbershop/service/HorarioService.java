package com.papusbarbershop.service;

import com.papusbarbershop.dto.HorarioCreateDTO;
import com.papusbarbershop.dto.HorarioDTO;
import com.papusbarbershop.entity.Barbero;
import com.papusbarbershop.entity.Horario;
import com.papusbarbershop.exception.RecursoDuplicadoException;
import com.papusbarbershop.exception.RecursoNoEncontradoException;
import com.papusbarbershop.exception.ValidacionException;
import com.papusbarbershop.repository.HorarioRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de horarios de barberos.
 */
@Service
public class HorarioService {

    private static final Logger logger = LoggerFactory.getLogger(HorarioService.class);

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private BarberoService barberoService;

    @Autowired
    private EntityManager entityManager;

    /**
     * Obtiene todos los horarios.
     * 
     * @return Lista de horarios
     */
    public List<HorarioDTO> findAll() {
        return horarioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un horario por su ID.
     * 
     * @param id ID del horario
     * @return Horario encontrado
     * @throws RecursoNoEncontradoException si no se encuentra el horario
     */
    public HorarioDTO findById(Long id) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Horario con ID " + id + " no encontrado"));
        return convertToDTO(horario);
    }

    /**
     * Obtiene todos los horarios de un barbero específico.
     * 
     * @param barberoId ID del barbero
     * @return Lista de horarios del barbero
     */
    public List<HorarioDTO> findByBarberoId(Long barberoId) {
        return horarioRepository.findByBarberoId(barberoId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo horario.
     * 
     * @param horarioCreateDTO DTO con los datos del horario
     * @return Horario creado
     * @throws RecursoNoEncontradoException si no se encuentra el barbero
     * @throws ValidacionException si la hora de entrada es mayor o igual a la hora de salida
     * @throws RecursoDuplicadoException si ya existe un horario activo para el barbero
     */
    @Transactional
    public HorarioDTO create(HorarioCreateDTO horarioCreateDTO) {
        // Validar que el barbero existe
        Barbero barbero = barberoService.findEntityById(horarioCreateDTO.getBarberoId());

        // Validar que la hora de entrada sea menor que la hora de salida
        if (horarioCreateDTO.getHoraEntrada().compareTo(horarioCreateDTO.getHoraSalida()) >= 0) {
            throw new ValidacionException("La hora de entrada debe ser menor que la hora de salida");
        }

        // Si el horario será activo, verificar que no exista otro horario activo para el mismo barbero
        if (horarioCreateDTO.getActivo() != null && horarioCreateDTO.getActivo()) {
            if (horarioRepository.existsByBarberoIdAndActivoTrue(barbero.getId())) {
                // Desactivar el horario activo anterior
                horarioRepository.findByBarberoIdAndActivoTrue(barbero.getId())
                        .ifPresent(horarioAnterior -> {
                            horarioAnterior.setActivo(false);
                            horarioRepository.save(horarioAnterior);
                        });
            }
        }

        Horario horario = new Horario();
        horario.setBarbero(barbero);
        horario.setHoraEntrada(horarioCreateDTO.getHoraEntrada());
        horario.setHoraSalida(horarioCreateDTO.getHoraSalida());
        horario.setActivo(horarioCreateDTO.getActivo() != null ? horarioCreateDTO.getActivo() : true);
        
        // Si se proporciona una fecha, establecerla en el created_at
        if (horarioCreateDTO.getFecha() != null) {
            horario.setCreatedAt(horarioCreateDTO.getFecha().atStartOfDay());
        }

        Horario horarioGuardado = horarioRepository.save(horario);
        // Refrescar la entidad para obtener el created_at de la base de datos
        entityManager.refresh(horarioGuardado);
        return convertToDTO(horarioGuardado);
    }

    /**
     * Actualiza un horario existente.
     * 
     * @param id ID del horario
     * @param horarioCreateDTO DTO con los datos actualizados
     * @return Horario actualizado
     * @throws RecursoNoEncontradoException si no se encuentra el horario o el barbero
     * @throws ValidacionException si la hora de entrada es mayor o igual a la hora de salida
     */
    @Transactional
    public HorarioDTO update(Long id, HorarioCreateDTO horarioCreateDTO) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Horario con ID " + id + " no encontrado"));

        // Validar que el barbero existe
        Barbero barbero = barberoService.findEntityById(horarioCreateDTO.getBarberoId());

        // Validar que la hora de entrada sea menor que la hora de salida
        if (horarioCreateDTO.getHoraEntrada().compareTo(horarioCreateDTO.getHoraSalida()) >= 0) {
            throw new ValidacionException("La hora de entrada debe ser menor que la hora de salida");
        }

        // Si el horario será activo, verificar que no exista otro horario activo para el mismo barbero
        if (horarioCreateDTO.getActivo() != null && horarioCreateDTO.getActivo()) {
            if (horarioRepository.existsByBarberoIdAndActivoTrue(barbero.getId())) {
                Horario horarioActivoExistente = horarioRepository.findByBarberoIdAndActivoTrue(barbero.getId())
                        .orElse(null);
                // Si existe otro horario activo y no es el mismo que estamos actualizando
                if (horarioActivoExistente != null && !horarioActivoExistente.getId().equals(id)) {
                    horarioActivoExistente.setActivo(false);
                    horarioRepository.save(horarioActivoExistente);
                }
            }
        }

        horario.setBarbero(barbero);
        horario.setHoraEntrada(horarioCreateDTO.getHoraEntrada());
        horario.setHoraSalida(horarioCreateDTO.getHoraSalida());
        horario.setActivo(horarioCreateDTO.getActivo() != null ? horarioCreateDTO.getActivo() : true);

        Horario horarioActualizado = horarioRepository.save(horario);
        // Refrescar la entidad para obtener el created_at de la base de datos
        entityManager.refresh(horarioActualizado);
        return convertToDTO(horarioActualizado);
    }

    /**
     * Elimina un horario.
     * 
     * @param id ID del horario
     * @throws RecursoNoEncontradoException si no se encuentra el horario
     */
    @Transactional
    public void delete(Long id) {
        if (!horarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Horario con ID " + id + " no encontrado");
        }
        horarioRepository.deleteById(id);
    }

    /**
     * Desactiva automáticamente todos los horarios pasados (cuya fecha es anterior a hoy).
     * 
     * Este método busca todos los horarios activos cuya fecha (createdAt) sea anterior
     * a la fecha actual y los desactiva automáticamente. Esto asegura que solo los
     * horarios de hoy o futuros estén activos.
     * 
     * @return Número de horarios desactivados
     */
    @Transactional
    public int desactivarHorariosPasados() {
        LocalDate fechaHoy = LocalDate.now();
        List<Horario> horariosPasados = horarioRepository.findHorariosPasadosActivos(fechaHoy);
        
        if (horariosPasados.isEmpty()) {
            logger.info("No hay horarios pasados para desactivar. Fecha actual: {}", fechaHoy);
            return 0;
        }
        
        int contador = 0;
        for (Horario horario : horariosPasados) {
            horario.setActivo(false);
            horarioRepository.save(horario);
            contador++;
            logger.debug("Horario desactivado automáticamente: ID={}, Barbero={}, Fecha={}", 
                    horario.getId(), 
                    horario.getBarbero().getNombre(),
                    horario.getCreatedAt() != null ? horario.getCreatedAt().toLocalDate() : "N/A");
        }
        
        logger.info("Se desactivaron automáticamente {} horarios pasados. Fecha actual: {}", contador, fechaHoy);
        return contador;
    }

    /**
     * Activa automáticamente los horarios del día actual.
     * 
     * Este método busca todos los horarios inactivos para la fecha de hoy y los activa.
     * Antes de activar un horario, desactiva cualquier otro horario activo del mismo barbero
     * para asegurar que solo haya un horario activo por barbero a la vez.
     * 
     * @return Número de horarios activados
     */
    @Transactional
    public int activarHorariosDelDia() {
        LocalDate fechaHoy = LocalDate.now();
        List<Horario> horariosDelDia = horarioRepository.findHorariosPorFecha(fechaHoy);
        
        if (horariosDelDia.isEmpty()) {
            logger.debug("No hay horarios para el día actual ({}) para activar.", fechaHoy);
            return 0;
        }
        
        int contador = 0;
        for (Horario horario : horariosDelDia) {
            // Si el horario ya está activo, no hacer nada
            if (horario.getActivo()) {
                continue;
            }
            
            // Desactivar cualquier otro horario activo del mismo barbero
            horarioRepository.findByBarberoIdAndActivoTrue(horario.getBarbero().getId())
                    .ifPresent(horarioActivoAnterior -> {
                        if (!horarioActivoAnterior.getId().equals(horario.getId())) {
                            horarioActivoAnterior.setActivo(false);
                            horarioRepository.save(horarioActivoAnterior);
                            logger.debug("Horario anterior desactivado: ID={}, Barbero={}, Fecha={}", 
                                    horarioActivoAnterior.getId(), 
                                    horarioActivoAnterior.getBarbero().getNombre(),
                                    horarioActivoAnterior.getCreatedAt() != null ? 
                                    horarioActivoAnterior.getCreatedAt().toLocalDate() : "N/A");
                        }
                    });
            
            // Activar el horario del día actual
            horario.setActivo(true);
            horarioRepository.save(horario);
            contador++;
            logger.debug("Horario activado automáticamente: ID={}, Barbero={}, Fecha={}", 
                    horario.getId(), 
                    horario.getBarbero().getNombre(),
                    fechaHoy);
        }
        
        if (contador > 0) {
            logger.info("Se activaron automáticamente {} horarios del día actual ({})", contador, fechaHoy);
        }
        return contador;
    }

    /**
     * Proceso completo de gestión automática de horarios.
     * Desactiva horarios pasados y activa horarios del día actual.
     * 
     * @return Map con el número de horarios desactivados y activados
     */
    @Transactional
    public java.util.Map<String, Integer> gestionarHorariosAutomaticamente() {
        int desactivados = desactivarHorariosPasados();
        int activados = activarHorariosDelDia();
        
        java.util.Map<String, Integer> resultado = new java.util.HashMap<>();
        resultado.put("horariosDesactivados", desactivados);
        resultado.put("horariosActivados", activados);
        
        logger.info("Gestión automática de horarios completada: {} desactivados, {} activados", 
                desactivados, activados);
        
        return resultado;
    }

    /**
     * Convierte una entidad Horario a DTO.
     * 
     * @param horario Entidad Horario
     * @return DTO de Horario
     */
    private HorarioDTO convertToDTO(Horario horario) {
        LocalDate fecha = null;
        if (horario.getCreatedAt() != null) {
            fecha = horario.getCreatedAt().toLocalDate();
        } else {
            // Si created_at es null, intentar leerlo nuevamente desde la base de datos
            horario = horarioRepository.findById(horario.getId()).orElse(horario);
            if (horario.getCreatedAt() != null) {
                fecha = horario.getCreatedAt().toLocalDate();
            }
        }
        return new HorarioDTO(
                horario.getId(),
                horario.getBarbero().getId(),
                horario.getBarbero().getNombre(),
                horario.getHoraEntrada(),
                horario.getHoraSalida(),
                horario.getActivo(),
                fecha
        );
    }
}

