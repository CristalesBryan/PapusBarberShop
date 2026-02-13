# 🔍 Revisión para Producción - PapusBarberShop

## ⚠️ PROBLEMAS CRÍTICOS ENCONTRADOS

### 1. **Credenciales Hardcodeadas en application.properties** 🔴 CRÍTICO

**Problema:** El archivo `Backend/src/main/resources/application.properties` contiene:
- Contraseña de base de datos: `04012005`
- Email y contraseña de Gmail: `cristalesbryan35@gmail.com` / `edegdltatlxavvsi`
- JWT Secret: `PapusBarberShopSecretKey2024SecureKeyForJWTTokenGeneration`

**Solución:** Mover todas las credenciales a variables de entorno.

### 2. **URLs Hardcodeadas en Frontends** 🔴 CRÍTICO

**Problema:** Los archivos `environment.prod.ts` tienen:
- `apiUrl: 'http://localhost:8080'` (debe ser la URL de producción)

**Solución:** Usar variables de entorno o configuración dinámica.

### 3. **CORS Configurado para Localhost** 🟡 IMPORTANTE

**Problema:** `application.properties` tiene:
```properties
cors.allowed-origins=http://localhost:3000,http://localhost:5173,http://localhost:4200
```

**Solución:** Configurar CORS para dominios de producción.

### 4. **Dockerfile no Usa Variable PORT** 🟡 IMPORTANTE

**Problema:** El `ENTRYPOINT` no usa la variable `PORT` de Railway.

**Solución:** Modificar el ENTRYPOINT para usar `${PORT}`.

### 5. **Configuración de Logging en Producción** 🟡 IMPORTANTE

**Problema:** Logging está en `DEBUG` y `TRACE`, demasiado verboso para producción.

**Solución:** Cambiar a `INFO` o `WARN` en producción.

### 6. **Hibernate ddl-auto=update** 🟡 IMPORTANTE

**Problema:** `spring.jpa.hibernate.ddl-auto=update` puede causar problemas en producción.

**Solución:** Cambiar a `validate` o `none` en producción.

---

## ✅ CORRECCIONES NECESARIAS

### Corrección 1: Actualizar application.properties para usar Variables de Entorno

```properties
# Base de datos
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/papus_barbershop}
spring.datasource.username=${DATABASE_USERNAME:postgres}
spring.datasource.password=${DATABASE_PASSWORD}

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# Email
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

# Puerto del servidor (Railway usa PORT)
server.port=${PORT:8080}

# CORS (usar variable de entorno)
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:4200}

# Logging (INFO para producción)
logging.level.com.papusbarbershop=${LOG_LEVEL:INFO}
logging.level.org.springframework.security=${LOG_LEVEL:INFO}
logging.level.org.hibernate.SQL=${LOG_LEVEL:WARN}
spring.jpa.show-sql=false

# Hibernate (validate en producción)
spring.jpa.hibernate.ddl-auto=${DDL_AUTO:validate}
```

### Corrección 2: Actualizar environment.prod.ts de Frontend

```typescript
export const environment = {
  production: true,
  apiUrl: window.location.origin, // Usar el mismo dominio
  // O usar variable de entorno en build time
  // apiUrl: process.env['API_URL'] || window.location.origin,
  ...
};
```

### Corrección 3: Actualizar Dockerfile

```dockerfile
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
```

### Corrección 4: Crear application-prod.properties

Crear `Backend/src/main/resources/application-prod.properties` con configuraciones específicas de producción.

---

## 📋 CHECKLIST PRE-PRODUCCIÓN

### Seguridad
- [ ] Todas las credenciales en variables de entorno
- [ ] JWT secret fuerte y único
- [ ] CORS configurado para dominios de producción
- [ ] Logging sin información sensible
- [ ] application.properties en .gitignore ✅

### Configuración
- [ ] Puerto configurado desde variable PORT
- [ ] URLs de API dinámicas (no localhost)
- [ ] Base de datos de producción configurada
- [ ] Hibernate ddl-auto=validate o none
- [ ] Logging en nivel INFO/WARN

### Build y Deployment
- [ ] Dockerfile funcional
- [ ] railpack-plan.json configurado
- [ ] Builds de frontends copiados correctamente
- [ ] Health check endpoint disponible

### Variables de Entorno Requeridas

```bash
# Base de datos
DATABASE_URL=jdbc:postgresql://host:5432/papus_barbershop
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=tu_password_seguro

# AWS
AWS_ACCESS_KEY_ID=tu_access_key
AWS_SECRET_ACCESS_KEY=tu_secret_key

# JWT
JWT_SECRET=clave_secreta_muy_larga_y_aleatoria
JWT_EXPIRATION=86400000

# Email
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=contraseña_de_aplicacion

# CORS
CORS_ALLOWED_ORIGINS=https://tu-dominio.com,https://admin.tu-dominio.com

# Logging
LOG_LEVEL=INFO

# Hibernate
DDL_AUTO=validate
```

---

## 🚀 PRÓXIMOS PASOS

1. Aplicar todas las correcciones listadas
2. Configurar variables de entorno en Railway
3. Probar en ambiente de staging
4. Verificar que no hay credenciales en el código
5. Configurar base de datos de producción
6. Desplegar a producción

