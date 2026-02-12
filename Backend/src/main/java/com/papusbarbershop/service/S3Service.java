package com.papusbarbershop.service;

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
     * Crea un cliente S3 configurado con las credenciales.
     * 
     * @return Cliente S3 configurado
     */
    private S3Client createS3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    /**
     * Crea un presigner S3 configurado con las credenciales.
     * 
     * @return Presigner S3 configurado
     */
    private S3Presigner createS3Presigner() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
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
        // Generar un nombre único para el archivo
        String uniqueFileName = generateUniqueFileName(fileName);
        String key = folder + "/" + uniqueFileName;

        try (S3Presigner presigner = createS3Presigner()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                    .putObjectRequest(putObjectRequest)
                    .build();

            String presignedUrl = presigner.presignPutObject(presignRequest).url().toString();

            return new PresignedUrlResponse(presignedUrl, key);
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
        try (S3Presigner presigner = createS3Presigner()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirationTime))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return presigner.presignGetObject(presignRequest).url().toString();
        }
    }

    /**
     * Elimina un archivo de S3.
     * 
     * @param key Clave del objeto en S3
     * @throws Exception Si hay un error al eliminar el archivo
     */
    public void deleteFile(String key) throws Exception {
        try (S3Client s3Client = createS3Client()) {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
        }
    }

    /**
     * Verifica si un archivo existe en S3.
     * 
     * @param key Clave del objeto en S3
     * @return true si el archivo existe
     */
    public boolean fileExists(String key) {
        try (S3Client s3Client = createS3Client()) {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            // Log error si es necesario
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

