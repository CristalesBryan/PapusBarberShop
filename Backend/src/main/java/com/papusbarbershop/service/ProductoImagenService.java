package com.papusbarbershop.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestionar las referencias de imágenes de productos en S3.
 * 
 * Este servicio almacena temporalmente las referencias de imágenes S3
 * para que puedan ser compartidas entre diferentes aplicaciones frontend.
 */
@Service
public class ProductoImagenService {

    // Almacenamiento en memoria de las referencias de imágenes
    // Estructura: Map<productoId, Map<"s3Key"|"nombreArchivo"|"timestamp", valor>>
    private final Map<Long, Map<String, Object>> imagenesProductos = new ConcurrentHashMap<>();

    /**
     * Guarda o actualiza la referencia de imagen S3 para un producto.
     * 
     * @param productoId ID del producto
     * @param s3Key Clave S3 de la imagen
     */
    public void guardarReferenciaImagen(Long productoId, String s3Key) {
        Map<String, Object> imagenData = new HashMap<>();
        imagenData.put("s3Key", s3Key);
        imagenData.put("timestamp", System.currentTimeMillis());
        imagenesProductos.put(productoId, imagenData);
        System.out.println("ProductoImagenService: Guardada referencia para producto " + productoId + " con s3Key: " + s3Key);
    }

    /**
     * Guarda o actualiza la referencia de imagen local para un producto.
     * 
     * @param productoId ID del producto
     * @param nombreArchivo Nombre del archivo local
     */
    public void guardarReferenciaImagenLocal(Long productoId, String nombreArchivo) {
        Map<String, Object> imagenData = imagenesProductos.getOrDefault(productoId, new HashMap<>());
        imagenData.put("nombreArchivo", nombreArchivo);
        imagenData.put("timestamp", System.currentTimeMillis());
        imagenesProductos.put(productoId, imagenData);
    }

    /**
     * Obtiene la referencia de imagen para un producto.
     * 
     * @param productoId ID del producto
     * @return Mapa con los datos de la imagen (s3Key, nombreArchivo, timestamp) o null si no existe
     */
    public Map<String, Object> obtenerReferenciaImagen(Long productoId) {
        Map<String, Object> referencia = imagenesProductos.get(productoId);
        if (referencia == null) {
            System.out.println("ProductoImagenService: No se encontró referencia para producto " + productoId);
        } else {
            System.out.println("ProductoImagenService: Referencia encontrada para producto " + productoId + ": " + referencia.get("s3Key"));
        }
        return referencia;
    }

    /**
     * Obtiene todas las referencias de imágenes.
     * 
     * @return Mapa con todas las referencias de imágenes
     */
    public Map<Long, Map<String, Object>> obtenerTodasLasReferencias() {
        return new HashMap<>(imagenesProductos);
    }

    /**
     * Guarda múltiples referencias de imágenes a la vez.
     * 
     * @param referencias Mapa con las referencias a guardar
     */
    public void guardarReferencias(Map<Long, Map<String, Object>> referencias) {
        imagenesProductos.putAll(referencias);
        System.out.println("ProductoImagenService: Guardadas " + referencias.size() + " referencias. Total: " + imagenesProductos.size());
    }

    /**
     * Elimina la referencia de imagen de un producto.
     * 
     * @param productoId ID del producto
     */
    public void eliminarReferenciaImagen(Long productoId) {
        imagenesProductos.remove(productoId);
    }
}

