package com.papusbarbershop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.UUID;

/**
 * Servicio para la gestión de archivos en Amazon S3.
 * 
 * Este servicio proporciona funcionalidades para:
 * - Generar URLs presignadas para subir archivos
 * - Generar URLs presignadas para descargar archivos
 * - Eliminar archivos de S3
 */
@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.access-key-id}")
    private String accessKeyId;

    @Value("${aws.s3.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.s3.presigned-url-expiration:3600}")
    private long presignedUrlExpiration;

    /**
     * Valida la configuración de S3 después de la inicialización.
     * Se ejecuta automáticamente después de que Spring inyecta las dependencias.
     */
    @PostConstruct
    public void validateConfiguration() {
        logger.info("=== Validando configuración de S3 ===");
        logger.info("Región: {}", region);
        logger.info("Bucket: {}", bucketName);
        logger.info("Access Key ID presente: {}", (accessKeyId != null && !accessKeyId.isEmpty()));
        logger.info("Secret Access Key presente: {}", (secretAccessKey != null && !secretAccessKey.isEmpty()));
        logger.info("Tiempo de expiración de URLs presignadas: {} segundos", presignedUrlExpiration);
        
        if (region == null || region.isEmpty()) {
            logger.error("ERROR: AWS_REGION no está configurada");
            throw new IllegalStateException("AWS_REGION no está configurada");
        }
        
        if (bucketName == null || bucketName.isEmpty()) {
            logger.error("ERROR: AWS_S3_BUCKET_NAME no está configurada");
            throw new IllegalStateException("AWS_S3_BUCKET_NAME no está configurada");
        }
        
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            logger.error("ERROR: AWS_ACCESS_KEY_ID no está configurada");
            throw new IllegalStateException("AWS_ACCESS_KEY_ID no está configurada");
        }
        
        if (secretAccessKey == null || secretAccessKey.isEmpty()) {
            logger.error("ERROR: AWS_SECRET_ACCESS_KEY no está configurada");
            throw new IllegalStateException("AWS_SECRET_ACCESS_KEY no está configurada");
        }
        
        // Validar formato de región
        try {
            Region.of(region);
            logger.info("Región válida: {}", region);
        } catch (Exception e) {
            logger.error("ERROR: Región inválida: {}", region, e);
            throw new IllegalStateException("Región AWS inválida: " + region, e);
        }
        
        logger.info("=== Configuración de S3 validada correctamente ===");
    }

    /**
     * Crea un cliente S3 configurado con las credenciales.
     * 
     * @return Cliente S3 configurado
     */
    private S3Client createS3Client() {
        logger.debug("Creando cliente S3 con región: {}, bucket: {}", region, bucketName);
        
        if (accessKeyId == null || accessKeyId.isEmpty() || secretAccessKey == null || secretAccessKey.isEmpty()) {
            logger.error("ERROR: Credenciales AWS no están disponibles");
            throw new IllegalStateException("Credenciales AWS no están configuradas correctamente");
        }
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        S3Client client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
        
        logger.debug("Cliente S3 creado exitosamente");
        return client;
    }

    /**
     * Crea un presigner S3 configurado con las credenciales.
     * 
     * @return Presigner S3 configurado
     */
    private S3Presigner createS3Presigner() {
        logger.debug("Creando presigner S3 con región: {}, bucket: {}", region, bucketName);
        
        if (accessKeyId == null || accessKeyId.isEmpty() || secretAccessKey == null || secretAccessKey.isEmpty()) {
            logger.error("ERROR: Credenciales AWS no están disponibles para presigner");
            throw new IllegalStateException("Credenciales AWS no están configuradas correctamente");
        }
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
        
        logger.debug("Presigner S3 creado exitosamente");
        return presigner;
    }

    /**
     * Genera una URL presignada para subir un archivo a S3.
     * 
     * @param fileName Nombre del archivo
     * @param folder Carpeta donde se guardará (ej: 'productos', 'barberos', 'cortes')
     * @param contentType Tipo de contenido (ej: 'image/jpeg', 'image/png')
     * @return URL presignada y la key del objeto
     */
    public PresignedUrlResponse generatePresignedUploadUrl(String fileName, String folder, String contentType) {
        logger.info("Generando URL presignada para subir archivo: fileName={}, folder={}, contentType={}", 
                fileName, folder, contentType);
        
        // Validar parámetros
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }
        
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
            logger.warn("ContentType no proporcionado, usando por defecto: {}", contentType);
        } else {
            // Normalizar Content-Type: lowercase y trim para asegurar coincidencia exacta
            contentType = contentType.toLowerCase().trim();
            logger.debug("ContentType normalizado: {}", contentType);
        }
        
        // Generar un nombre único para el archivo
        String uniqueFileName = generateUniqueFileName(fileName);
        String key = folder + "/" + uniqueFileName;
        
        logger.info("Key generada: {}", key);
        logger.info("Configuración: bucket={}, region={}, expiration={}s", 
                bucketName, region, presignedUrlExpiration);

        try (S3Presigner presigner = createS3Presigner()) {
            // Construir PutObjectRequest SIN Content-Type para evitar SignatureDoesNotMatch
            // El Content-Type NO debe estar firmado en la URL presignada
            // Solo se firma el header "host", permitiendo que el frontend envíe cualquier Content-Type
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            // Nota: No incluimos Content-Type en el PutObjectRequest porque:
            // 1. Si se incluye, queda firmado en SignedHeaders (content-type;host)
            // 2. Esto causa SignatureDoesNotMatch si el Content-Type enviado no coincide exactamente
            // 3. Al no incluirlo, solo se firma "host" y el frontend puede enviar cualquier Content-Type

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                    .putObjectRequest(putObjectRequest)
                    .build();

            String presignedUrl = presigner.presignPutObject(presignRequest).url().toString();
            
            logger.info("URL presignada generada exitosamente para key: {}", key);
            logger.debug("URL presignada (primeros 100 caracteres): {}...", 
                    presignedUrl.length() > 100 ? presignedUrl.substring(0, 100) : presignedUrl);

            return new PresignedUrlResponse(presignedUrl, key);
        } catch (Exception e) {
            logger.error("Error al generar URL presignada: bucket={}, region={}, key={}", 
                    bucketName, region, key, e);
            throw new RuntimeException("Error al generar URL presignada para S3: " + e.getMessage(), e);
        }
    }

    /**
     * Genera una URL presignada para descargar/ver un archivo de S3.
     * 
     * @param key Clave del objeto en S3
     * @param expirationTime Tiempo de expiración en segundos
     * @return URL presignada
     */
    public String generatePresignedDownloadUrl(String key, long expirationTime) {
        logger.info("Generando URL presignada para descargar: key={}, expiration={}s", key, expirationTime);
        
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("La key no puede estar vacía");
        }
        
        try (S3Presigner presigner = createS3Presigner()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirationTime))
                    .getObjectRequest(getObjectRequest)
                    .build();

            String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();
            logger.info("URL presignada de descarga generada exitosamente para key: {}", key);
            
            return presignedUrl;
        } catch (Exception e) {
            logger.error("Error al generar URL presignada de descarga: bucket={}, region={}, key={}", 
                    bucketName, region, key, e);
            throw new RuntimeException("Error al generar URL presignada de descarga: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un archivo de S3.
     * 
     * @param key Clave del objeto en S3
     * @throws Exception Si hay un error al eliminar el archivo
     */
    public void deleteFile(String key) throws Exception {
        logger.info("Eliminando archivo de S3: key={}, bucket={}", key, bucketName);
        
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("La key no puede estar vacía");
        }
        
        try (S3Client s3Client = createS3Client()) {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            logger.info("Archivo eliminado exitosamente: key={}", key);
        } catch (Exception e) {
            logger.error("Error al eliminar archivo de S3: bucket={}, region={}, key={}", 
                    bucketName, region, key, e);
            throw e;
        }
    }

    /**
     * Verifica si un archivo existe en S3.
     * 
     * @param key Clave del objeto en S3
     * @return true si el archivo existe
     */
    public boolean fileExists(String key) {
        logger.debug("Verificando existencia de archivo: key={}, bucket={}", key, bucketName);
        
        if (key == null || key.isEmpty()) {
            return false;
        }
        
        try (S3Client s3Client = createS3Client()) {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            logger.debug("Archivo existe: key={}", key);
            return true;
        } catch (NoSuchKeyException e) {
            logger.debug("Archivo no existe: key={}", key);
            return false;
        } catch (Exception e) {
            logger.error("Error al verificar existencia de archivo: bucket={}, region={}, key={}", 
                    bucketName, region, key, e);
            return false;
        }
    }

    /**
     * Genera un nombre de archivo único basado en el nombre original.
     * 
     * @param originalFileName Nombre original del archivo
     * @return Nombre de archivo único con timestamp y UUID
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFileName.substring(lastDotIndex);
            originalFileName = originalFileName.substring(0, lastDotIndex);
        }

        // Sanitizar el nombre del archivo
        String sanitizedName = originalFileName
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // Limitar la longitud
        if (sanitizedName.length() > 50) {
            sanitizedName = sanitizedName.substring(0, 50);
        }

        // Agregar timestamp y UUID para garantizar unicidad
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return sanitizedName + "-" + timestamp + "-" + uuid + extension;
    }

    /**
     * Clase interna para la respuesta de URL presignada.
     */
    public static class PresignedUrlResponse {
        private String url;
        private String key;

        public PresignedUrlResponse(String url, String key) {
            this.url = url;
            this.key = key;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}

