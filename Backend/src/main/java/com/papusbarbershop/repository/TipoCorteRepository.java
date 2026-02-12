package com.papusbarbershop.repository;

import com.papusbarbershop.entity.TipoCorte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad TipoCorte.
 */
@Repository
public interface TipoCorteRepository extends JpaRepository<TipoCorte, Long> {
    
    /**
     * Busca todos los tipos de corte activos.
     */
    List<TipoCorte> findByActivoTrue();
}

