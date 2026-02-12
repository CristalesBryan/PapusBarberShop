package com.papusbarbershop.repository;

import com.papusbarbershop.entity.VentaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad VentaProducto.
 * 
 * Proporciona métodos para acceder a los datos de ventas de productos en la base de datos.
 */
@Repository
public interface VentaProductoRepository extends JpaRepository<VentaProducto, Long> {

    /**
     * Busca todas las ventas de una fecha específica.
     * 
     * @param fecha Fecha a buscar
     * @return Lista de ventas
     */
    List<VentaProducto> findByFecha(LocalDate fecha);

    /**
     * Busca todas las ventas de un barbero específico.
     * 
     * @param barberoId ID del barbero
     * @return Lista de ventas
     */
    @Query("SELECT v FROM VentaProducto v WHERE v.barbero.id = :barberoId")
    List<VentaProducto> findByBarberoId(@Param("barberoId") Long barberoId);

    /**
     * Busca todas las ventas en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de ventas
     */
    @Query("SELECT v FROM VentaProducto v WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<VentaProducto> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio,
                                           @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de ventas de un barbero en un rango de fechas.
     * 
     * @param barberoId ID del barbero
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Total de ventas
     */
    @Query("SELECT COALESCE(SUM(v.importe), 0) FROM VentaProducto v WHERE v.barbero.id = :barberoId " +
           "AND v.fecha BETWEEN :fechaInicio AND :fechaFin")
    java.math.BigDecimal calcularTotalPorBarbero(@Param("barberoId") Long barberoId,
                                                 @Param("fechaInicio") LocalDate fechaInicio,
                                                 @Param("fechaFin") LocalDate fechaFin);
}

