package com.papusbarbershop.repository;

import com.papusbarbershop.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 * 
 * Proporciona métodos para acceder a los datos de usuarios en la base de datos.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     * 
     * @param username Nombre de usuario
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado.
     * 
     * @param username Nombre de usuario
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsername(String username);
}

