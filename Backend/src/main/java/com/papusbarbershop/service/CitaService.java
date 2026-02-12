package com.papusbarbershop.service;

import com.papusbarbershop.dto.*;
import com.papusbarbershop.entity.*;
import com.papusbarbershop.exception.RecursoNoEncontradoException;
import com.papusbarbershop.exception.ValidacionException;
import com.papusbarbershop.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar citas.
 */
@Service
@Transactional
public class CitaService {

    private static final Logger logger = LoggerFactory.getLogger(CitaService.class);

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private BarberoService barberoService;

    @Autowired
    private TipoCorteService tipoCorteService;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private EmailAsyncService emailAsyncService;

    /**
     * Crea una nueva cita con validaciones de disponibilidad.
     */
    public CitaDTO crearCita(CitaCreateDTO citaCreateDTO) {
        // Validar que el barbero existe
        Barbero barbero = barberoService.findEntityById(citaCreateDTO.getBarberoId());

        // Validar que el tipo de corte existe
        TipoCorte tipoCorte = tipoCorteService.obtenerEntidadPorId(citaCreateDTO.getTipoCorteId());

        // Validar disponibilidad del barbero
        validarDisponibilidad(barbero, citaCreateDTO.getFecha(), citaCreateDTO.getHora(), 
                             tipoCorte.getTiempoMinutos(), null);

        // Crear la cita
        Cita cita = new Cita();
        cita.setFecha(citaCreateDTO.getFecha());
        cita.setHora(citaCreateDTO.getHora());
        cita.setBarbero(barbero);
        cita.setTipoCorte(tipoCorte);
        cita.setNombreCliente(citaCreateDTO.getNombreCliente());
        cita.setCorreoCliente(citaCreateDTO.getCorreoCliente());
        cita.setTelefonoCliente(citaCreateDTO.getTelefonoCliente());
        cita.setComentarios(citaCreateDTO.getComentarios());
        cita.setEstado("CONFIRMADA");

        // Guardar correos a los que se envió la confirmación
        String correosEnviados = String.join(",", citaCreateDTO.getCorreosConfirmacion());
        cita.setCorreosEnviados(correosEnviados);

        // Guardar la cita (ya no hay restricción única en la base de datos,
        // la validación se hace en validarDisponibilidad() que excluye citas completadas/canceladas)
        Cita citaGuardada = citaRepository.save(cita);

        // Enviar correos de confirmación de forma ASÍNCRONA (no bloquea la respuesta)
        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        logger.info("Preparando envío ASÍNCRONO de correos de confirmación para cita. Correos: {}", 
                citaCreateDTO.getCorreosConfirmacion());
        
        // El envío se ejecuta en segundo plano - NO bloquea la respuesta
        emailAsyncService.enviarConfirmacionCitaAsync(
                citaCreateDTO.getCorreosConfirmacion(),
                citaCreateDTO.getNombreCliente(),
                citaCreateDTO.getFecha().format(fechaFormatter),
                citaCreateDTO.getHora().format(horaFormatter),
                barbero.getNombre(),
                tipoCorte.getNombre(),
                citaCreateDTO.getComentarios()
        );
        
        // La respuesta se envía inmediatamente, el correo se enviará en segundo plano
        logger.info("Tarea de envío de correo enviada al pool asíncrono. La respuesta se enviará inmediatamente.");

        return convertirADTO(citaGuardada);
    }

    /**
     * Valida la disponibilidad de un barbero en una fecha y hora específica.
     * NO permite usar horarios de fechas pasadas - solo horarios de la fecha solicitada o futuros.
     */
    private void validarDisponibilidad(Barbero barbero, LocalDate fecha, LocalTime hora, 
                                      Integer tiempoCorte, Long citaIdExcluir) {
        LocalDate fechaHoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();
        
        // Si la fecha solicitada es pasada, no permitir crear la cita
        if (fecha.isBefore(fechaHoy)) {
            throw new ValidacionException(
                    "No se pueden crear citas para fechas pasadas. Fecha solicitada: " + fecha);
        }
        
        // Si la fecha es hoy, verificar que la hora no sea pasada
        if (fecha.equals(fechaHoy)) {
            if (hora.isBefore(horaActual)) {
                throw new ValidacionException(
                        "No se pueden crear citas para horas pasadas. Hora solicitada: " + hora + 
                        ", Hora actual: " + horaActual);
            }
        }
        
        Optional<Horario> horarioOpt = Optional.empty();
        
        // Buscar horario para la fecha específica
        horarioOpt = horarioRepository.findByBarberoIdAndFecha(barbero.getId(), fecha);
        
        // Si no hay horario para la fecha exacta, buscar el más cercano futuro (no pasado)
        if (horarioOpt.isEmpty()) {
            List<Horario> horariosFuturos = horarioRepository.findByBarberoIdAndFechaFutura(barbero.getId(), fecha);
            if (!horariosFuturos.isEmpty()) {
                horarioOpt = Optional.of(horariosFuturos.get(0));
                logger.debug("Validando disponibilidad: usando horario futuro para barbero {}: fecha del horario {}, fecha solicitada {}", 
                        barbero.getId(), horariosFuturos.get(0).getCreatedAt().toLocalDate(), fecha);
            }
        }
        
        if (horarioOpt.isEmpty()) {
            throw new ValidacionException(
                    "El barbero " + barbero.getNombre() + " no tiene un horario configurado para la fecha " + fecha + 
                    " o fechas futuras. Por favor, configure un horario para esta fecha.");
        }
        
        Horario horario = horarioOpt.get();
        LocalDate fechaHorario = horario.getCreatedAt().toLocalDate();
        
        // Verificar que el horario NO sea de una fecha pasada
        if (fechaHorario.isBefore(fechaHoy)) {
            throw new ValidacionException(
                    "El horario del barbero " + barbero.getNombre() + " es de una fecha pasada (" + fechaHorario + 
                    "). Por favor, configure un horario para la fecha " + fecha + " o futura.");
        }
        
        // Verificar que el horario sea de la fecha solicitada o futura
        // No usar horarios de fechas pasadas para fechas futuras
        if (fechaHorario.isBefore(fecha)) {
            throw new ValidacionException(
                    "El horario del barbero " + barbero.getNombre() + " es de una fecha anterior (" + fechaHorario + 
                    ") a la fecha solicitada (" + fecha + "). Por favor, configure un horario para la fecha " + fecha + " o futura.");
        }

        // Verificar que la hora esté dentro del horario del barbero
        if (hora.isBefore(horario.getHoraEntrada()) || hora.isAfter(horario.getHoraSalida())) {
            throw new ValidacionException(
                    "La hora seleccionada está fuera del horario del barbero. " +
                    "Horario disponible: " + horario.getHoraEntrada() + " - " + horario.getHoraSalida());
        }

        // Si la fecha es hoy, verificar que la hora de salida del barbero no haya pasado
        if (fecha.equals(fechaHoy)) {
            if (horario.getHoraSalida().isBefore(horaActual)) {
                throw new ValidacionException(
                        "No se pueden crear citas porque el horario del barbero ya terminó. " +
                        "El barbero sale a las " + horario.getHoraSalida() + " y la hora actual es " + horaActual);
            }
            // También verificar que la hora seleccionada no sea pasada (ya validado arriba, pero doble verificación)
            if (hora.isBefore(horaActual)) {
                throw new ValidacionException(
                        "No se pueden crear citas para horas pasadas. Hora solicitada: " + hora + 
                        ", Hora actual: " + horaActual);
            }
        }

        // Calcular la hora de finalización del corte
        LocalTime horaFin = hora.plusMinutes(tiempoCorte);
        
        // Verificar que la hora de finalización no exceda el horario del barbero
        if (horaFin.isAfter(horario.getHoraSalida())) {
            throw new ValidacionException(
                    "El corte no puede completarse dentro del horario del barbero. " +
                    "El corte terminaría a las " + horaFin + " pero el barbero sale a las " + horario.getHoraSalida());
        }
        
        // Si la fecha es hoy, verificar que la hora de finalización no sea pasada
        if (fecha.equals(fechaHoy)) {
            if (horaFin.isBefore(horaActual) || horaFin.equals(horaActual)) {
                throw new ValidacionException(
                        "No se pueden crear citas porque el corte terminaría en una hora pasada. " +
                        "Hora de finalización: " + horaFin + ", Hora actual: " + horaActual);
            }
        }

        // Verificar que no haya otra cita activa en la misma fecha y hora para este barbero
        // Buscar directamente la cita y verificar su estado
        Optional<Cita> citaExistenteOpt = citaRepository.findByBarberoAndFechaAndHora(barbero, fecha, hora);
        if (citaExistenteOpt.isPresent()) {
            Cita citaExistente = citaExistenteOpt.get();
            // Excluir la cita actual si se está editando
            if (citaIdExcluir == null || !citaExistente.getId().equals(citaIdExcluir)) {
                String estadoCita = citaExistente.getEstado();
                // Solo considerar citas activas (excluir CANCELADA y COMPLETADA)
                if (!"CANCELADA".equals(estadoCita) && !"COMPLETADA".equals(estadoCita)) {
                    logger.debug("Validando disponibilidad: cita activa encontrada - Barbero: {}, Fecha: {}, Hora: {}, Estado: {}", 
                            barbero.getId(), fecha, hora, estadoCita);
                    throw new ValidacionException(
                            "Ya existe una cita " + estadoCita.toLowerCase() + " para este barbero en la fecha " + fecha + 
                            " a las " + hora + ". Por favor, seleccione otra hora.");
                } else {
                    // Si la cita está cancelada o completada, permitir crear una nueva cita en esa hora
                    logger.debug("Validando disponibilidad: cita {} encontrada pero está {}, permitiendo nueva cita - Barbero: {}, Fecha: {}, Hora: {}", 
                            citaExistente.getId(), estadoCita, barbero.getId(), fecha, hora);
                }
            }
        }
        
        // Verificar solapamientos de horarios
        List<Cita> citasExistentes = citaRepository.findByBarberoAndFecha(barbero, fecha);
        
        for (Cita citaExistente : citasExistentes) {
            // Excluir la cita actual si se está editando
            if (citaIdExcluir != null && citaExistente.getId().equals(citaIdExcluir)) {
                continue;
            }

            // Solo considerar citas activas (PENDIENTE o CONFIRMADA)
            if (!"CANCELADA".equals(citaExistente.getEstado()) && 
                !"COMPLETADA".equals(citaExistente.getEstado())) {
                
                LocalTime horaInicioExistente = citaExistente.getHora();
                TipoCorte tipoCorteExistente = citaExistente.getTipoCorte();
                LocalTime horaFinExistente = horaInicioExistente.plusMinutes(
                        tipoCorteExistente.getTiempoMinutos());

                // Verificar solapamiento de horarios (excluyendo el caso exacto ya verificado arriba)
                if (!hora.equals(horaInicioExistente) && 
                    hora.isBefore(horaFinExistente) && horaFin.isAfter(horaInicioExistente)) {
                    throw new ValidacionException(
                            "Ya existe una cita para este barbero que se solapa con el horario seleccionado. " +
                            "Cita existente: " + horaInicioExistente + " - " + horaFinExistente);
                }
            }
        }
    }

    /**
     * Obtiene todas las citas.
     */
    public List<CitaDTO> obtenerTodas() {
        return citaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una cita por ID.
     */
    public CitaDTO obtenerPorId(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada con ID: " + id));
        return convertirADTO(cita);
    }

    /**
     * Obtiene las citas de un barbero en una fecha específica.
     */
    public List<CitaDTO> obtenerPorBarberoYFecha(Long barberoId, LocalDate fecha) {
        Barbero barbero = barberoService.findEntityById(barberoId);
        return citaRepository.findByBarberoAndFecha(barbero, fecha).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la disponibilidad de barberos para una fecha específica.
     * Solo incluye barberos que tengan un horario configurado para esa fecha.
     * NO muestra horarios de fechas pasadas - solo horarios de la fecha solicitada o futuros.
     */
    public List<DisponibilidadDTO> obtenerDisponibilidad(LocalDate fecha) {
        List<Barbero> barberos = barberoService.findAllEntities();
        List<DisponibilidadDTO> disponibilidades = new ArrayList<>();
        LocalDate fechaHoy = LocalDate.now();

        // Si la fecha solicitada es pasada, no mostrar disponibilidad
        if (fecha.isBefore(fechaHoy)) {
            logger.debug("Fecha solicitada {} es pasada, no se mostrará disponibilidad", fecha);
            return disponibilidades; // Retornar lista vacía
        }

        for (Barbero barbero : barberos) {
            Optional<Horario> horarioOpt = Optional.empty();
            
            // Buscar horario para la fecha específica
            horarioOpt = horarioRepository.findByBarberoIdAndFecha(barbero.getId(), fecha);
            
            // Si no hay horario para la fecha exacta, buscar el más cercano futuro (no pasado)
            if (horarioOpt.isEmpty()) {
                List<Horario> horariosFuturos = horarioRepository.findByBarberoIdAndFechaFutura(barbero.getId(), fecha);
                if (!horariosFuturos.isEmpty()) {
                    // Tomar el primer horario futuro (más cercano a la fecha solicitada)
                    horarioOpt = Optional.of(horariosFuturos.get(0));
                    logger.debug("Usando horario futuro para barbero {}: fecha del horario {}, fecha solicitada {}", 
                            barbero.getId(), horariosFuturos.get(0).getCreatedAt().toLocalDate(), fecha);
                }
            }
            
            // Solo agregar disponibilidad si el barbero tiene un horario configurado
            // y el horario es de la fecha solicitada o futura (no pasado)
            if (horarioOpt.isPresent()) {
                Horario horario = horarioOpt.get();
                LocalDate fechaHorario = horario.getCreatedAt().toLocalDate();
                
                // Verificar que el horario NO sea de una fecha pasada
                // Solo usar horarios de hoy o futuros
                if (fechaHorario.isBefore(fechaHoy)) {
                    logger.debug("Excluyendo horario de fecha pasada para barbero {}: fecha del horario {}, fecha hoy {}", 
                            barbero.getId(), fechaHorario, fechaHoy);
                    continue; // Saltar este barbero - no tiene horario válido
                }
                
                // Verificar que el horario sea de la fecha solicitada o futura
                // No usar horarios de fechas pasadas para fechas futuras
                if (fechaHorario.isBefore(fecha)) {
                    // Si el horario es de una fecha anterior a la solicitada, no usarlo
                    logger.info("Excluyendo horario anterior a fecha solicitada para barbero {}: fecha del horario {}, fecha solicitada {}", 
                            barbero.getId(), fechaHorario, fecha);
                    continue; // Saltar este barbero - no tiene horario válido para esta fecha
                }
                
                // Verificación adicional: asegurar que el horario no sea pasado
                if (fechaHorario.isBefore(fechaHoy)) {
                    logger.warn("ADVERTENCIA: Se encontró un horario pasado que debería haber sido filtrado. Barbero: {}, Fecha horario: {}, Fecha hoy: {}", 
                            barbero.getId(), fechaHorario, fechaHoy);
                    continue; // Saltar este barbero - no tiene horario válido
                }
                
                DisponibilidadDTO disponibilidad = new DisponibilidadDTO();
                disponibilidad.setBarberoId(barbero.getId());
                disponibilidad.setBarberoNombre(barbero.getNombre());
                disponibilidad.setHoraEntrada(horario.getHoraEntrada());
                disponibilidad.setHoraSalida(horario.getHoraSalida());

                // Obtener citas del barbero en esa fecha
                List<Cita> citas = citaRepository.findByBarberoAndFecha(barbero, fecha);
                // Filtrar solo citas activas (excluir CANCELADA y COMPLETADA)
                List<Cita> citasActivas = citas.stream()
                        .filter(c -> {
                            boolean esActiva = !"CANCELADA".equals(c.getEstado()) && !"COMPLETADA".equals(c.getEstado());
                            if (!esActiva) {
                                logger.debug("Excluyendo cita cancelada/completada: Barbero {}, Fecha {}, Hora {}, Estado {}", 
                                        barbero.getId(), fecha, c.getHora(), c.getEstado());
                            }
                            return esActiva;
                        })
                        .collect(Collectors.toList());
                
                // Crear rangos de tiempo ocupados considerando la duración del corte
                List<RangoTiempo> rangosOcupados = citasActivas.stream()
                        .map(c -> {
                            LocalTime horaInicio = c.getHora();
                            LocalTime horaFin = horaInicio.plusMinutes(c.getTipoCorte().getTiempoMinutos());
                            return new RangoTiempo(horaInicio, horaFin);
                        })
                        .collect(Collectors.toList());
                
                // Mantener horasOcupadas solo para compatibilidad (solo hora de inicio)
                List<LocalTime> horasOcupadas = citasActivas.stream()
                        .map(Cita::getHora)
                        .collect(Collectors.toList());
                
                logger.debug("Barbero {} - Fecha {}: {} citas activas, {} rangos ocupados", 
                        barbero.getId(), fecha, citasActivas.size(), rangosOcupados.size());
                disponibilidad.setHorasOcupadas(horasOcupadas);

                // Generar horas disponibles considerando la duración de los cortes
                List<LocalTime> horasDisponibles = generarHorasDisponibles(
                        horario.getHoraEntrada(), horario.getHoraSalida(), rangosOcupados);
                disponibilidad.setHorasDisponibles(horasDisponibles);
                
                disponibilidades.add(disponibilidad);
            }
        }

        return disponibilidades;
    }

    /**
     * Clase auxiliar para representar un rango de tiempo ocupado.
     */
    private static class RangoTiempo {
        LocalTime inicio;
        LocalTime fin;
        
        RangoTiempo(LocalTime inicio, LocalTime fin) {
            this.inicio = inicio;
            this.fin = fin;
        }
        
        /**
         * Verifica si una hora se solapa con este rango.
         * Una hora se considera ocupada si está dentro del rango [inicio, fin).
         */
        boolean estaOcupada(LocalTime hora) {
            return !hora.isBefore(inicio) && hora.isBefore(fin);
        }
    }

    /**
     * Genera lista de horas disponibles cada 5 minutos desde horaInicio hasta horaFin.
     * Considera la duración de los cortes, bloqueando todas las horas durante el tiempo que dura cada cita.
     */
    private List<LocalTime> generarHorasDisponibles(LocalTime horaInicio, LocalTime horaFin, 
                                                    List<RangoTiempo> rangosOcupados) {
        List<LocalTime> horasDisponibles = new ArrayList<>();
        LocalTime horaActual = horaInicio;

        // Generar horas cada 5 minutos desde horaInicio hasta horaFin (inclusive)
        while (!horaActual.isAfter(horaFin)) {
            // Verificar si la hora actual está ocupada por algún rango de tiempo
            boolean estaOcupada = false;
            for (RangoTiempo rango : rangosOcupados) {
                if (rango.estaOcupada(horaActual)) {
                    estaOcupada = true;
                    break;
                }
            }
            
            if (!estaOcupada) {
                horasDisponibles.add(horaActual);
            }
            
            horaActual = horaActual.plusMinutes(5);
            
            // Evitar bucle infinito si horaFin es 00:00 (medianoche)
            if (horaActual.equals(LocalTime.MIDNIGHT) && !horaFin.equals(LocalTime.MIDNIGHT)) {
                break;
            }
        }

        return horasDisponibles;
    }

    /**
     * Cancela una cita.
     */
    public void cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada con ID: " + id));
        
        if ("CANCELADA".equals(cita.getEstado())) {
            throw new ValidacionException("La cita ya está cancelada");
        }

        cita.setEstado("CANCELADA");
        citaRepository.save(cita);
    }

    /**
     * Actualiza la hora de una cita existente.
     * Valida que la nueva hora no esté ocupada.
     * 
     * @param id ID de la cita
     * @param nuevaHora Nueva hora para la cita
     * @return Cita actualizada
     * @throws RecursoNoEncontradoException si no se encuentra la cita
     * @throws ValidacionException si la nueva hora está ocupada o la cita está cancelada/completada
     */
    @Transactional
    public CitaDTO actualizarHora(Long id, LocalTime nuevaHora) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada con ID: " + id));
        
        // Verificar que la cita no esté cancelada o completada
        if ("CANCELADA".equals(cita.getEstado())) {
            throw new ValidacionException("No se puede cambiar la hora de una cita cancelada");
        }
        
        if ("COMPLETADA".equals(cita.getEstado())) {
            throw new ValidacionException("No se puede cambiar la hora de una cita completada");
        }

        // Validar que la nueva hora no esté ocupada
        validarDisponibilidad(
            cita.getBarbero(), 
            cita.getFecha(), 
            nuevaHora, 
            cita.getTipoCorte().getTiempoMinutos(), 
            id // Excluir la cita actual de la validación
        );

        // Actualizar la hora
        cita.setHora(nuevaHora);
        Cita citaActualizada = citaRepository.save(cita);
        
        return convertirADTO(citaActualizada);
    }

    /**
     * Marca una cita como completada.
     * 
     * @param id ID de la cita
     * @return Cita actualizada
     * @throws RecursoNoEncontradoException si no se encuentra la cita
     * @throws ValidacionException si la cita ya está cancelada o completada
     */
    @Transactional
    public CitaDTO completarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada con ID: " + id));
        
        // Verificar que la cita no esté cancelada
        if ("CANCELADA".equals(cita.getEstado())) {
            throw new ValidacionException("No se puede completar una cita cancelada");
        }
        
        // Verificar que la cita no esté ya completada
        if ("COMPLETADA".equals(cita.getEstado())) {
            throw new ValidacionException("La cita ya está completada");
        }

        // Actualizar el estado a COMPLETADA
        cita.setEstado("COMPLETADA");
        Cita citaActualizada = citaRepository.save(cita);
        
        return convertirADTO(citaActualizada);
    }

    /**
     * Convierte una entidad Cita a DTO.
     */
    private CitaDTO convertirADTO(Cita cita) {
        CitaDTO dto = new CitaDTO();
        dto.setId(cita.getId());
        dto.setFecha(cita.getFecha());
        dto.setHora(cita.getHora());
        dto.setBarberoId(cita.getBarbero().getId());
        dto.setBarberoNombre(cita.getBarbero().getNombre());
        dto.setTipoCorteId(cita.getTipoCorte().getId());
        dto.setTipoCorteNombre(cita.getTipoCorte().getNombre());
        dto.setTipoCorteDescripcion(cita.getTipoCorte().getDescripcion());
        dto.setTipoCorteTiempoMinutos(cita.getTipoCorte().getTiempoMinutos());
        dto.setTipoCortePrecio(cita.getTipoCorte().getPrecio());
        dto.setNombreCliente(cita.getNombreCliente());
        dto.setCorreoCliente(cita.getCorreoCliente());
        dto.setTelefonoCliente(cita.getTelefonoCliente());
        dto.setComentarios(cita.getComentarios());
        dto.setEstado(cita.getEstado());
        return dto;
    }
}

