package com.papusbarbershop.repository;

import com.papusbarbershop.entity.Cita;
import com.papusbarbershop.entity.Barbero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Cita.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    
    /**
     * Busca una cita por barbero, fecha y hora.
     */
    Optional<Cita> findByBarberoAndFechaAndHora(Barbero barbero, LocalDate fecha, LocalTime hora);
    
    /**
     * Busca todas las citas de un barbero en una fecha específica.
     */
    List<Cita> findByBarberoAndFecha(Barbero barbero, LocalDate fecha);
    
    /**
     * Busca todas las citas en un rango de fechas.
     */
    List<Cita> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    /**
     * Busca todas las citas de un barbero.
     */
    List<Cita> findByBarbero(Barbero barbero);
    
    /**
     * Verifica si existe una cita para un barbero en una fecha y hora específica.
     */
    boolean existsByBarberoAndFechaAndHora(Barbero barbero, LocalDate fecha, LocalTime hora);
    
    /**
     * Busca todas las citas pendientes o confirmadas.
     */
    @Query("SELECT c FROM Cita c WHERE c.estado IN ('PENDIENTE', 'CONFIRMADA')")
    List<Cita> findCitasActivas();
}

