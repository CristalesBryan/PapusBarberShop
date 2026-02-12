package com.papusbarbershop.controller;

import com.papusbarbershop.service.ProductoService;
import com.papusbarbershop.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para operaciones con Amazon S3.
 * 
 * Este controlador maneja las peticiones relacionadas con el almacenamiento
 * de archivos en S3, incluyendo la generación de URLs presignadas para
 * subir y descargar archivos.
 */
@RestController
@RequestMapping("/api/s3")
@CrossOrigin(origins = "*")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private ProductoService productoService;

    /**
     * Endpoint para generar una URL presignada para subir un archivo.
     * 
     * @param request Solicitud con fileName, folder y contentType
     * @return URL presignada y la key del objeto
     */
    @PostMapping("/presigned-url/upload")
    public ResponseEntity<?> generatePresignedUploadUrl(@RequestBody PresignedUploadRequest request) {
        try {
            if (request.getFileName() == null || request.getFileName().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("El nombre del archivo es requerido"));
            }

            String folder = request.getFolder() != null ? request.getFolder() : "general";
            String contentType = request.getContentType() != null ? request.getContentType() : "application/octet-stream";

            S3Service.PresignedUrlResponse response = s3Service.generatePresignedUploadUrl(
                    request.getFileName(),
                    folder,
                    contentType
            );

            Map<String, String> result = new HashMap<>();
            result.put("url", response.getUrl());
            result.put("key", response.getKey());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al generar URL presignada: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para generar una URL presignada para descargar/ver un archivo.
     * 
     * @param request Solicitud con key y expirationTime
     * @return URL presignada
     */
    @PostMapping("/presigned-url/download")
    public ResponseEntity<?> generatePresignedDownloadUrl(@RequestBody PresignedDownloadRequest request) {
        try {
            if (request.getKey() == null || request.getKey().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("La clave del archivo es requerida"));
            }

            long expirationTime = request.getExpirationTime() != null && request.getExpirationTime() > 0
                    ? request.getExpirationTime()
                    : 3600; // Por defecto 1 hora

            String presignedUrl = s3Service.generatePresignedDownloadUrl(request.getKey(), expirationTime);

            Map<String, String> result = new HashMap<>();
            result.put("url", presignedUrl);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al generar URL presignada: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar un archivo de S3.
     * 
     * @param key Clave del objeto en S3
     * @return Respuesta sin contenido si la eliminación fue exitosa
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestParam String key) {
        try {
            if (key == null || key.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("La clave del archivo es requerida"));
            }

            s3Service.deleteFile(key);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al eliminar archivo: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para verificar si un archivo existe en S3.
     * 
     * @param key Clave del objeto en S3
     * @return true si el archivo existe, false en caso contrario
     */
    @GetMapping("/exists")
    public ResponseEntity<?> fileExists(@RequestParam String key) {
        try {
            if (key == null || key.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("La clave del archivo es requerida"));
            }

            boolean exists = s3Service.fileExists(key);

            Map<String, Object> result = new HashMap<>();
            result.put("exists", exists);
            result.put("key", key);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al verificar archivo: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para guardar la referencia de imagen S3 de un producto.
     * 
     * @param request Solicitud con productoId y s3Key
     * @return Respuesta de confirmación
     */
    @PostMapping("/producto-imagen")
    public ResponseEntity<?> guardarReferenciaImagen(@RequestBody ProductoImagenRequest request) {
        try {
            if (request.getProductoId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("El ID del producto es requerido"));
            }
            if (request.getS3Key() == null || request.getS3Key().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("La clave S3 es requerida"));
            }

            // Guardar s3Key directamente en la entidad Producto (base de datos)
            productoService.actualizarS3Key(request.getProductoId(), request.getS3Key());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Referencia de imagen guardada correctamente");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al guardar referencia: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener la referencia de imagen de un producto.
     * 
     * @param productoId ID del producto
     * @return Referencia de imagen o null si no existe
     */
    @GetMapping("/producto-imagen/{productoId}")
    public ResponseEntity<?> obtenerReferenciaImagen(@PathVariable Long productoId) {
        try {
            // Obtener s3Key directamente de la entidad Producto
            com.papusbarbershop.entity.Producto producto = productoService.findEntityById(productoId);
            Map<String, Object> referencia = new HashMap<>();
            
            if (producto.getS3Key() != null && !producto.getS3Key().isEmpty()) {
                referencia.put("s3Key", producto.getS3Key());
                referencia.put("timestamp", System.currentTimeMillis());
            }

            return ResponseEntity.ok(referencia);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener referencia: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener todas las referencias de imágenes.
     * 
     * @return Mapa con todas las referencias de imágenes
     */
    @GetMapping("/producto-imagenes")
    public ResponseEntity<?> obtenerTodasLasReferencias() {
        try {
            // Obtener todas las referencias directamente de la base de datos
            java.util.List<com.papusbarbershop.entity.Producto> productos = productoService.findAllEntities();
            Map<Long, Map<String, Object>> referencias = new HashMap<>();
            
            for (com.papusbarbershop.entity.Producto producto : productos) {
                if (producto.getS3Key() != null && !producto.getS3Key().isEmpty()) {
                    Map<String, Object> imagenData = new HashMap<>();
                    imagenData.put("s3Key", producto.getS3Key());
                    imagenData.put("timestamp", System.currentTimeMillis());
                    referencias.put(producto.getId(), imagenData);
                }
            }
            
            return ResponseEntity.ok(referencias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener referencias: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para sincronizar múltiples referencias de imágenes.
     * 
     * @param referencias Mapa con las referencias a sincronizar
     * @return Respuesta de confirmación
     */
    @PostMapping("/producto-imagenes/sincronizar")
    public ResponseEntity<?> sincronizarReferencias(@RequestBody Map<Long, Map<String, Object>> referencias) {
        try {
            // Guardar cada referencia directamente en la entidad Producto (base de datos)
            int count = 0;
            for (Map.Entry<Long, Map<String, Object>> entry : referencias.entrySet()) {
                Long productoId = entry.getKey();
                Map<String, Object> imagenData = entry.getValue();
                String s3Key = (String) imagenData.get("s3Key");
                
                if (s3Key != null && !s3Key.isEmpty()) {
                    try {
                        productoService.actualizarS3Key(productoId, s3Key);
                        count++;
                    } catch (Exception e) {
                        System.err.println("Error al actualizar s3Key para producto " + productoId + ": " + e.getMessage());
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Referencias sincronizadas correctamente");
            result.put("count", count);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al sincronizar referencias: " + e.getMessage()));
        }
    }

    /**
     * Crea una respuesta de error en formato JSON.
     * 
     * @param message Mensaje de error
     * @return Mapa con el mensaje de error
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    /**
     * Clase para la solicitud de URL presignada de subida.
     */
    public static class PresignedUploadRequest {
        private String fileName;
        private String folder;
        private String contentType;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFolder() {
            return folder;
        }

        public void setFolder(String folder) {
            this.folder = folder;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }

    /**
     * Clase para la solicitud de URL presignada de descarga.
     */
    public static class PresignedDownloadRequest {
        private String key;
        private Long expirationTime;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(Long expirationTime) {
            this.expirationTime = expirationTime;
        }
    }

    /**
     * Clase para la solicitud de guardar referencia de imagen de producto.
     */
    public static class ProductoImagenRequest {
        private Long productoId;
        private String s3Key;

        public Long getProductoId() {
            return productoId;
        }

        public void setProductoId(Long productoId) {
            this.productoId = productoId;
        }

        public String getS3Key() {
            return s3Key;
        }

        public void setS3Key(String s3Key) {
            this.s3Key = s3Key;
        }
    }
}

