package com.papusbarbershop.repository;

import com.papusbarbershop.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Servicio.
 * 
 * Proporciona métodos para acceder a los datos de servicios en la base de datos.
 */
@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    /**
     * Busca todos los servicios de una fecha específica.
     * 
     * @param fecha Fecha a buscar
     * @return Lista de servicios
     */
    List<Servicio> findByFecha(LocalDate fecha);

    /**
     * Busca todos los servicios de un barbero específico.
     * 
     * @param barberoId ID del barbero
     * @return Lista de servicios
     */
    @Query("SELECT s FROM Servicio s WHERE s.barbero.id = :barberoId")
    List<Servicio> findByBarberoId(@Param("barberoId") Long barberoId);

    /**
     * Busca todos los servicios en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de servicios
     */
    @Query("SELECT s FROM Servicio s WHERE s.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Servicio> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                      @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de servicios de un barbero en un rango de fechas.
     * 
     * @param barberoId ID del barbero
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Total de servicios
     */
    @Query("SELECT COALESCE(SUM(s.precio), 0) FROM Servicio s WHERE s.barbero.id = :barberoId " +
           "AND s.fecha BETWEEN :fechaInicio AND :fechaFin")
    java.math.BigDecimal calcularTotalPorBarbero(@Param("barberoId") Long barberoId,
                                                  @Param("fechaInicio") LocalDate fechaInicio,
                                                  @Param("fechaFin") LocalDate fechaFin);
}

