# 🚂 Configuración de Variables en Railway

## 📋 Variables Requeridas para el Backend

### Paso 1: Ir a Variables en Railway

1. Ve a tu proyecto en Railway
2. Selecciona tu servicio **Backend**
3. Haz clic en la pestaña **"Variables"**
4. Haz clic en **"+ New Variable"**

### Paso 2: Agregar Variables

Agrega las siguientes variables una por una:

#### 🔴 CRÍTICO: Base de Datos

```
Variable: DATABASE_URL
Valor: ${{ Postgres.DATABASE_URL }}
```

**Importante:** Reemplaza `Postgres` con el nombre exacto de tu servicio PostgreSQL en Railway.

#### 🔴 CRÍTICO: AWS S3

```
Variable: AWS_ACCESS_KEY_ID
Valor: tu_access_key_id_de_aws
```

```
Variable: AWS_SECRET_ACCESS_KEY
Valor: tu_secret_access_key_de_aws
```

#### 🔴 CRÍTICO: JWT

```
Variable: JWT_SECRET
Valor: genera_una_clave_secreta_muy_larga_y_aleatoria
```

**Para generar una clave secreta:**
```bash
openssl rand -base64 64
```

```
Variable: JWT_EXPIRATION
Valor: 86400000
```

#### 🔴 CRÍTICO: Email

```
Variable: MAIL_USERNAME
Valor: tu_email@gmail.com
```

```
Variable: MAIL_PASSWORD
Valor: tu_contraseña_de_aplicacion_gmail
```

#### 🟡 IMPORTANTE: CORS

```
Variable: CORS_ALLOWED_ORIGINS
Valor: https://tu-dominio.com,https://admin.tu-dominio.com
```

**Nota:** Separa múltiples dominios con comas. No incluyas espacios.

#### 🟢 OPCIONAL: Logging

```
Variable: LOG_LEVEL
Valor: INFO
```

#### 🟢 OPCIONAL: Hibernate

```
Variable: DDL_AUTO
Valor: validate
```

---

## ✅ Checklist de Variables

Marca cada variable después de agregarla:

- [ ] `DATABASE_URL=${{ Postgres.DATABASE_URL }}`
- [ ] `AWS_ACCESS_KEY_ID`
- [ ] `AWS_SECRET_ACCESS_KEY`
- [ ] `JWT_SECRET`
- [ ] `JWT_EXPIRATION` (opcional, por defecto 86400000)
- [ ] `MAIL_USERNAME`
- [ ] `MAIL_PASSWORD`
- [ ] `CORS_ALLOWED_ORIGINS`
- [ ] `LOG_LEVEL` (opcional, por defecto INFO)
- [ ] `DDL_AUTO` (opcional, por defecto validate en prod)

---

## 🔍 Verificar Variables

Después de agregar todas las variables:

1. Verifica que todas estén en la lista de **Variables**
2. Asegúrate de que `DATABASE_URL` use la sintaxis `${{ Postgres.DATABASE_URL }}`
3. Revisa que no haya espacios extra en los valores

---

## 🚀 Después de Configurar

Una vez configuradas todas las variables:

1. Railway reiniciará automáticamente tu servicio
2. Revisa los logs para verificar que la conexión a la base de datos sea exitosa
3. Si hay errores, verifica que todas las variables estén correctamente configuradas

---

## 🆘 Problemas Comunes

### Error: "Could not resolve placeholder 'DATABASE_URL'"

- Verifica que la variable `DATABASE_URL` esté configurada
- Asegúrate de usar `${{ Postgres.DATABASE_URL }}` (reemplaza `Postgres` con el nombre de tu servicio)

### Error: "Connection refused"

- Verifica que el servicio PostgreSQL esté ejecutándose
- Revisa que `DATABASE_URL` tenga el formato correcto

### Error: "Authentication failed"

- Verifica las credenciales en `DATABASE_URL`
- Asegúrate de que el servicio PostgreSQL esté correctamente configurado

