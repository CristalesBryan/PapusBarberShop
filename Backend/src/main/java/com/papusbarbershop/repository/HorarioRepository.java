package com.papusbarbershop.repository;

import com.papusbarbershop.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Horario.
 * 
 * Proporciona métodos para acceder a los datos de horarios en la base de datos.
 */
@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {
    
    /**
     * Busca todos los horarios de un barbero específico.
     * 
     * @param barberoId ID del barbero
     * @return Lista de horarios del barbero
     */
    List<Horario> findByBarberoId(Long barberoId);
    
    /**
     * Busca el horario activo de un barbero específico.
     * 
     * @param barberoId ID del barbero
     * @return Horario activo del barbero, si existe
     */
    Optional<Horario> findByBarberoIdAndActivoTrue(Long barberoId);
    
    /**
     * Verifica si existe un horario activo para un barbero.
     * 
     * @param barberoId ID del barbero
     * @return true si existe un horario activo
     */
    boolean existsByBarberoIdAndActivoTrue(Long barberoId);
    
    /**
     * Busca el horario de un barbero para una fecha específica.
     * Busca por la fecha del created_at del horario.
     * Solo busca horarios activos.
     * 
     * @param barberoId ID del barbero
     * @param fecha Fecha para buscar el horario
     * @return Horario del barbero para esa fecha, si existe
     */
    @Query("SELECT h FROM Horario h WHERE h.barbero.id = :barberoId AND CAST(h.createdAt AS date) = :fecha AND h.activo = true ORDER BY h.createdAt DESC")
    Optional<Horario> findByBarberoIdAndFecha(@Param("barberoId") Long barberoId, @Param("fecha") LocalDate fecha);
    
    /**
     * Busca el horario más reciente de un barbero para una fecha específica o anterior.
     * Si no hay horario para la fecha exacta, busca el más reciente anterior a esa fecha.
     * 
     * @param barberoId ID del barbero
     * @param fecha Fecha para buscar el horario
     * @return Horario del barbero más reciente para esa fecha o anterior, si existe
     */
    @Query("SELECT h FROM Horario h WHERE h.barbero.id = :barberoId AND CAST(h.createdAt AS date) <= :fecha ORDER BY h.createdAt DESC")
    List<Horario> findByBarberoIdAndFechaAnterior(@Param("barberoId") Long barberoId, @Param("fecha") LocalDate fecha);
    
    /**
     * Busca el horario más reciente de un barbero para una fecha específica o futura.
     * Solo busca horarios que sean de la fecha solicitada o posteriores (no pasados).
     * 
     * @param barberoId ID del barbero
     * @param fecha Fecha mínima para buscar el horario (no busca fechas anteriores)
     * @return Horario del barbero más reciente para esa fecha o futura, si existe
     */
    @Query("SELECT h FROM Horario h WHERE h.barbero.id = :barberoId AND CAST(h.createdAt AS date) >= :fecha AND h.activo = true ORDER BY h.createdAt ASC")
    List<Horario> findByBarberoIdAndFechaFutura(@Param("barberoId") Long barberoId, @Param("fecha") LocalDate fecha);
    
    /**
     * Busca todos los horarios activos cuya fecha (createdAt) sea anterior a la fecha especificada.
     * Útil para desactivar automáticamente horarios pasados.
     * 
     * @param fecha Fecha límite (hoy). Se buscarán horarios anteriores a esta fecha.
     * @return Lista de horarios activos de fechas pasadas
     */
    @Query("SELECT h FROM Horario h WHERE h.activo = true AND CAST(h.createdAt AS date) < :fecha")
    List<Horario> findHorariosPasadosActivos(@Param("fecha") LocalDate fecha);
    
    /**
     * Busca todos los horarios inactivos para una fecha específica.
     * Útil para activar automáticamente horarios del día actual.
     * 
     * @param fecha Fecha para buscar horarios
     * @return Lista de horarios inactivos para esa fecha
     */
    @Query("SELECT h FROM Horario h WHERE CAST(h.createdAt AS date) = :fecha AND h.activo = false")
    List<Horario> findHorariosInactivosPorFecha(@Param("fecha") LocalDate fecha);
    
    /**
     * Busca todos los horarios (activos e inactivos) para una fecha específica.
     * 
     * @param fecha Fecha para buscar horarios
     * @return Lista de horarios para esa fecha
     */
    @Query("SELECT h FROM Horario h WHERE CAST(h.createdAt AS date) = :fecha")
    List<Horario> findHorariosPorFecha(@Param("fecha") LocalDate fecha);
}

