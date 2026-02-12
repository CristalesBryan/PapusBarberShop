package com.papusbarbershop.repository;

import com.papusbarbershop.entity.Barbero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad Barbero.
 * 
 * Proporciona métodos para acceder a los datos de barberos en la base de datos.
 */
@Repository
public interface BarberoRepository extends JpaRepository<Barbero, Long> {
}

