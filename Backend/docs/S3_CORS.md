# Configuración CORS para el bucket S3 (papusbarbershop)

El error **"No 'Access-Control-Allow-Origin' header is present on the requested resource"** al subir imágenes desde https://gestion.papusbarbershop.com se soluciona configurando CORS en el **bucket S3**. El backend ya genera URLs presignadas con `PutObjectRequest` sin headers adicionales firmados.

## Opción 1: AWS CLI (recomendado)

Desde la raíz del proyecto Backend (donde está `s3-cors-config.json`):

```bash
aws s3api put-bucket-cors --bucket papusbarbershop --cors-configuration file://s3-cors-config.json
```

Asegúrate de tener credenciales configuradas (`aws configure` o variables `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`) con permiso `s3:PutBucketCors` en el bucket.

## Opción 2: Consola AWS

1. Entra en **S3** → bucket **papusbarbershop**.
2. Pestaña **Permisos** → sección **Configuración de CORS** → **Editar**.
3. Pega la siguiente configuración (equivalente a `s3-cors-config.json`):

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "HEAD"],
    "AllowedOrigins": [
      "https://gestion.papusbarbershop.com",
      "https://www.papusbarbershop.com",
      "https://papusbarbershop.com",
      "http://localhost:4200",
      "http://localhost:3000"
    ],
    "ExposeHeaders": ["ETag"]
  }
]
```

4. Guarda los cambios.

## Verificación

- **AllowedOrigins**: dominios desde los que el navegador hace el PUT/GET a la URL presignada.
- **AllowedMethods**: GET (ver imagen), PUT (subir con URL presignada), HEAD (comprobaciones).
- **AllowedHeaders**: `*` permite que el navegador envíe `Content-Type` u otros en el PUT sin que falle el preflight.
- **ExposeHeaders**: ETag por si en el futuro se usa la cabecera de respuesta.

Después de aplicar CORS, las subidas desde https://gestion.papusbarbershop.com deberían funcionar sin error de CORS.
