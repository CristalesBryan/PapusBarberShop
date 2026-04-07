# ✅ Checklist para Railway - PapusBarberShop

## 🧩 Paso 1 — JWT Configurado ✅

**application-prod.properties** está correctamente configurado:

```properties
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

✅ No hay líneas duplicadas  
✅ No hay líneas vacías como `jwt.expiration=`

## 🧩 Paso 2 — Variables en Railway

Verifica que en **Backend → Variables** existan:

- [ ] `DATABASE_URL` = `${{Postgres.DATABASE_URL}}`
- [ ] `DATABASE_USERNAME` (opcional si DATABASE_URL tiene credenciales)
- [ ] `DATABASE_PASSWORD` (opcional si DATABASE_URL tiene credenciales)
- [ ] `JWT_SECRET` = `tu_clave_secreta_muy_larga`
- [ ] `JWT_EXPIRATION` = `86400000` (solo números, sin comillas, sin espacios)
- [ ] `CORS_ALLOWED_ORIGINS` = `https://tu-dominio.com`

**⚠️ IMPORTANTE:** `JWT_EXPIRATION` debe ser solo números: `86400000`

## 🧩 Paso 3 — Root Directory

En Railway → **Settings → Build**:

- [ ] **Root Directory** debe decir: `Backend`
- [ ] Si está vacío, cámbialo a: `Backend`

## 🧩 Paso 4 — Redeploy Limpio

Después de hacer commit y push:

1. Ve a **Deployments**
2. Presiona **Restart**
3. Espera que termine el despliegue

### ✅ Logs Correctos (deben aparecer):

```
Tomcat started on port(s): XXXX (http) with context path ''
Started PapusBarberShopApplication in X.XXX seconds
```

### ❌ Logs Incorrectos (NO deben aparecer):

```
UnsatisfiedDependencyException
Could not resolve placeholder 'JWT_SECRET'
```

## 🧪 Paso 5 — Exponer Dominio

Cuando el servicio esté **Online**:

1. Ve a **Settings → Networking**
2. Haz clic en **Generate Domain**
3. Copia el dominio generado
4. Prueba: `https://tu-dominio.railway.app/actuator/health`

### Respuesta Esperada:

```json
{
  "status": "UP"
}
```

O:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

## 🔍 Verificaciones Adicionales

### Spring Boot Actuator ✅

- [x] Dependencia agregada en `pom.xml`
- [x] Endpoint `/actuator/health` configurado como público
- [x] Configuración en `application.properties`

### DatabaseConfig ✅

- [x] Clase creada para convertir URL de Railway a formato JDBC
- [x] Extrae credenciales automáticamente de `DATABASE_URL`

### Variables de Entorno ✅

Todas las variables críticas están configuradas para usar variables de entorno.

## 🚨 Problemas Comunes

### Error: "Could not resolve placeholder 'JWT_SECRET'"

**Solución:** Verifica que `JWT_SECRET` esté configurada en Railway → Variables

### Error: "UnsatisfiedDependencyException"

**Solución:** 
- Verifica que todas las variables requeridas estén configuradas
- Revisa los logs para ver qué variable falta

### Error: "Connection refused" o "Connection timeout"

**Solución:**
- Verifica que el servicio PostgreSQL esté ejecutándose
- Verifica que `DATABASE_URL` esté correctamente configurada
- Asegúrate de que el nombre del servicio PostgreSQL coincida en `${{Postgres.DATABASE_URL}}`

### Health Check retorna 404

**Solución:**
- Verifica que Spring Boot Actuator esté en el `pom.xml`
- Verifica que `/actuator/health` esté permitido en `SecurityConfig`

## 📝 Notas

- El perfil de producción se activa automáticamente en Railway
- Si necesitas activar manualmente, agrega: `SPRING_PROFILES_ACTIVE=prod`
- El puerto se configura automáticamente desde la variable `PORT` de Railway

