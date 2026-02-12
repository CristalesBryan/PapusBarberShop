package com.papusbarbershop.repository;

import com.papusbarbershop.entity.MobiliarioEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad MobiliarioEquipo.
 */
@Repository
public interface MobiliarioEquipoRepository extends JpaRepository<MobiliarioEquipo, Long> {
    
    /**
     * Busca todos los elementos por categoría.
     */
    List<MobiliarioEquipo> findByCategoria(String categoria);
    
    /**
     * Busca todos los elementos por estado.
     */
    List<MobiliarioEquipo> findByEstado(String estado);
}

