# 🚂 Configuración para Railway

## 📋 Variables de Entorno Requeridas

### 1. Base de Datos (PostgreSQL)

Railway proporciona automáticamente la variable `DATABASE_URL` cuando creas un servicio PostgreSQL. 

**Opción A: Usar DATABASE_URL directamente (Recomendado) ⭐**

En Railway, en tu servicio **Backend**, agrega esta variable:

1. Ve a tu servicio Backend → Pestaña **"Variables"**
2. Haz clic en **"+ New Variable"**
3. Agrega:

```
Nombre: DATABASE_URL
Valor: ${{ Postgres.DATABASE_URL }}
```

**⚠️ IMPORTANTE:** Reemplaza `Postgres` con el nombre exacto de tu servicio PostgreSQL en Railway.

Esto conectará automáticamente tu backend con la base de datos PostgreSQL de Railway.

**Opción B: Variables individuales (Alternativa)**

Si prefieres usar variables separadas:

```
DATABASE_URL=jdbc:postgresql://metro.proxy.rlwy.net:13283/railway
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=tu_contraseña_de_railway
```

### 2. AWS S3

```
AWS_ACCESS_KEY_ID=tu_access_key_id
AWS_SECRET_ACCESS_KEY=tu_secret_access_key
```

### 3. JWT

```
JWT_SECRET=clave_secreta_muy_larga_y_aleatoria_minimo_256_bits
JWT_EXPIRATION=86400000
```

### 4. Email (Gmail)

```
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=contraseña_de_aplicacion_gmail
```

### 5. CORS

```
CORS_ALLOWED_ORIGINS=https://tu-dominio.com,https://admin.tu-dominio.com
```

**Nota:** Separa múltiples dominios con comas.

### 6. Logging (Opcional)

```
LOG_LEVEL=INFO
```

### 7. Hibernate (Opcional)

```
DDL_AUTO=validate
```

### 8. Puerto

Railway proporciona automáticamente la variable `PORT`. No necesitas configurarla manualmente.

---

## 🔧 Pasos para Configurar en Railway

### Paso 1: Crear Servicio PostgreSQL

1. En tu proyecto Railway, haz clic en **"+ New"**
2. Selecciona **"Database"** → **"Add PostgreSQL"**
3. Railway creará automáticamente la base de datos

### Paso 2: Crear Servicio Backend

1. Haz clic en **"+ New"**
2. Selecciona **"GitHub Repo"** y conecta tu repositorio
3. Railway detectará automáticamente el Dockerfile o railpack-plan.json

### Paso 3: Configurar Variables de Entorno

1. Ve a tu servicio **Backend** en Railway
2. Haz clic en la pestaña **"Variables"**
3. Haz clic en **"+ New Variable"**

#### 🔴 PRIMERO: Conectar con PostgreSQL (CRÍTICO)

**Agrega esta variable primero:**

```
Nombre de Variable: DATABASE_URL
Valor: ${{ Postgres.DATABASE_URL }}
```

**⚠️ IMPORTANTE:** 
- Reemplaza `Postgres` con el **nombre exacto** de tu servicio PostgreSQL en Railway
- Si tu servicio PostgreSQL se llama diferente (ej: "PostgreSQL", "DB", etc.), usa ese nombre
- Ejemplo: Si tu servicio se llama "PostgreSQL", usa: `${{ PostgreSQL.DATABASE_URL }}`

**Cómo verificar el nombre de tu servicio PostgreSQL:**
1. Ve a tu proyecto en Railway
2. Busca el servicio PostgreSQL en la lista
3. El nombre aparece debajo del icono de PostgreSQL

#### Luego agrega las demás variables:

Sigue agregando las demás variables listadas en la sección "Variables de Entorno Requeridas" arriba.

### Paso 4: Ejecutar Scripts SQL

Después del primer despliegue, necesitas ejecutar los scripts SQL para crear las tablas:

1. Ve a tu servicio PostgreSQL en Railway
2. Haz clic en **"Connect"**
3. Usa el comando `psql` o la interfaz web de Railway
4. Ejecuta el script: `Backend/src/main/resources/database/papus_barbershop.sql`

O puedes usar Railway CLI:

```bash
railway connect Postgres
psql -f Backend/src/main/resources/database/papus_barbershop.sql
```

---

## 🔍 Verificación

### Verificar Conexión a Base de Datos

Revisa los logs de tu servicio Backend en Railway. Deberías ver:

```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

Si ves errores de conexión, verifica:
- Que la variable `DATABASE_URL` esté configurada correctamente
- Que el servicio PostgreSQL esté ejecutándose
- Que las credenciales sean correctas

### Verificar Variables de Entorno

En Railway, ve a **"Variables"** y verifica que todas las variables estén configuradas.

---

## 📝 Notas Importantes

1. **DATABASE_URL de Railway**: Railway proporciona la URL en formato `postgresql://user:pass@host:port/db`. La clase `DatabaseConfig` convierte automáticamente esto al formato JDBC que Spring Boot necesita.

2. **Puerto**: Railway asigna automáticamente un puerto y lo proporciona en la variable `PORT`. Tu aplicación ya está configurada para usarlo.

3. **CORS**: Asegúrate de configurar `CORS_ALLOWED_ORIGINS` con los dominios reales de producción (no localhost).

4. **JWT_SECRET**: Genera una clave secreta fuerte y única para producción. Puedes usar:
   ```bash
   openssl rand -base64 64
   ```

5. **Base de Datos**: Railway crea automáticamente la base de datos, pero necesitas ejecutar los scripts SQL para crear las tablas.

---

## 🚀 Despliegue

Una vez configuradas todas las variables:

1. Railway detectará automáticamente cambios en tu repositorio
2. Construirá la aplicación usando el Dockerfile o railpack-plan.json
3. Desplegará automáticamente

Puedes ver el progreso en la pestaña **"Deployments"** de tu servicio.

---

## 🆘 Solución de Problemas

### Error: "Could not resolve placeholder 'DATABASE_URL'"

- Verifica que la variable `DATABASE_URL` esté configurada en Railway
- Asegúrate de usar `${{ Postgres.DATABASE_URL }}` para referenciar el servicio PostgreSQL

### Error: "Connection refused" o "Connection timeout"

- Verifica que el servicio PostgreSQL esté ejecutándose
- Revisa que la variable `DATABASE_URL` tenga el formato correcto
- Verifica que no haya restricciones de firewall

### Error: "Authentication failed"

- Verifica las credenciales en la variable `DATABASE_URL`
- Asegúrate de que el usuario tenga permisos en la base de datos

### La aplicación no inicia

- Revisa los logs en Railway
- Verifica que todas las variables de entorno requeridas estén configuradas
- Asegúrate de que el puerto esté configurado correctamente

