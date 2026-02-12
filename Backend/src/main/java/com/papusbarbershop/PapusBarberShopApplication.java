package com.papusbarbershop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Clase principal de la aplicación Spring Boot para Papus BarberShop.
 * 
 * Esta aplicación gestiona:
 * - Usuarios y autenticación
 * - Barberos y sus porcentajes
 * - Servicios (cortes)
 * - Productos e inventario
 * - Ventas de productos
 * - Reportes y cálculos de pagos
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class PapusBarberShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(PapusBarberShopApplication.class, args);
    }
}

