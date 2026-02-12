package com.papusbarbershop.repository;

import com.papusbarbershop.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad Producto.
 * 
 * Proporciona métodos para acceder a los datos de productos en la base de datos.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}

