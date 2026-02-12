# Papus BarberShop - Backend

Backend completo para el sistema de gestión de la barbería "Papus BarberShop" desarrollado con Java Spring Boot y PostgreSQL.

## 🚀 Tecnologías

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security (JWT)**
- **PostgreSQL**
- **Maven**

## 📋 Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- PostgreSQL 12+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## 🗄️ Base de Datos

### Configuración de PostgreSQL

1. Crear la base de datos:
```sql
CREATE DATABASE papus_barbershop;
```

2. Ejecutar el script SQL:
```bash
psql -U postgres -d papus_barbershop -f src/main/resources/database/papus_barbershop.sql
```

O ejecutar el script manualmente desde el archivo:
`src/main/resources/database/papus_barbershop.sql`

### Configuración en application.properties

Ajustar las credenciales de PostgreSQL en:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/papus_barbershop
spring.datasource.username=postgres
spring.datasource.password=tu_password
```

## 🔧 Instalación y Ejecución

1. **Clonar o navegar al directorio del proyecto:**
```bash
cd "Pagina BarberShop/Backend"
```

2. **Compilar el proyecto:**
```bash
mvn clean install
```

3. **Ejecutar la aplicación:**
```bash
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## 📚 Estructura del Proyecto

```
src/main/java/com/papusbarbershop/
├── config/              # Configuraciones (Security, etc.)
├── controller/          # Controladores REST
├── dto/                 # Data Transfer Objects
├── entity/              # Entidades JPA
├── exception/            # Excepciones personalizadas
├── repository/           # Repositorios JPA
├── security/             # Configuración de seguridad JWT
└── service/              # Lógica de negocio
```

## 🔐 Autenticación

### Usuario por Defecto

- **Username:** `admin`
- **Password:** `admin123`

### Generar Token JWT

1. Hacer POST a `/auth/login`:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

2. La respuesta incluirá un token JWT que debe usarse en el header:
```
Authorization: Bearer <token>
```

## 📡 Endpoints Principales

### Autenticación
- `POST /auth/login` - Iniciar sesión
- `POST /auth/register` - Registrar usuario (solo ADMIN)

### Barberos
- `GET /barberos` - Listar todos los barberos
- `GET /barberos/{id}` - Obtener barbero por ID

### Servicios (Cortes)
- `POST /servicios` - Crear servicio
- `GET /servicios` - Listar todos los servicios
- `GET /servicios/fecha/{fecha}` - Servicios por fecha
- `GET /servicios/resumen/diario` - Resumen diario
- `GET /servicios/resumen/mensual` - Resumen mensual
- `GET /servicios/resumen/barbero/{id}` - Resumen por barbero

### Productos
- `POST /productos` - Crear producto (solo ADMIN)
- `PUT /productos/{id}` - Actualizar producto (solo ADMIN)
- `GET /productos` - Listar todos los productos
- `GET /productos/{id}` - Obtener producto por ID

### Ventas de Productos
- `POST /ventas-productos` - Crear venta
- `GET /ventas-productos` - Listar todas las ventas
- `GET /ventas-productos/fecha/{fecha}` - Ventas por fecha

### Reportes
- `GET /reportes/diario` - Reporte diario
- `GET /reportes/mensual` - Reporte mensual
- `GET /reportes/fecha/{fecha}` - Reporte por fecha

## 👥 Roles y Permisos

### ADMIN
- Acceso completo a todas las funcionalidades
- Puede crear/editar productos
- Puede registrar usuarios

### BARBERO
- Puede registrar servicios (cortes)
- Puede registrar ventas de productos
- Puede consultar reportes
- No puede gestionar productos ni usuarios

## 📊 Datos Iniciales

El script SQL incluye:

### Barberos Precargados:
- **Carlos** - 55%
- **Alex** - 50%
- **Cesia** - 100%
- **Ediel** - 100%

### Productos de Ejemplo:
- Gel para Cabello
- Pomada
- Shampoo
- Acondicionador
- Cera para Barba

## 🔒 Seguridad

- Autenticación basada en JWT
- Contraseñas encriptadas con BCrypt
- Endpoints protegidos por roles
- CORS configurado para desarrollo

## 📝 Validaciones

- Validación de stock antes de vender productos
- No se permite stock negativo
- Validación de datos con `@Valid`
- Manejo global de excepciones

## 🧮 Cálculo de Pagos

El sistema calcula automáticamente:
- Total de servicios por barbero
- Total de ventas por barbero
- Pago según porcentaje del barbero
- Totales diarios y mensuales

## 🐛 Solución de Problemas

### Error de conexión a la base de datos
- Verificar que PostgreSQL esté corriendo
- Verificar credenciales en `application.properties`
- Verificar que la base de datos exista

### Error de compilación
- Verificar versión de Java (debe ser 17+)
- Ejecutar `mvn clean install`
- Verificar dependencias en `pom.xml`

## 📄 Licencia

Este proyecto es privado y está desarrollado para Papus BarberShop.

## 👨‍💻 Desarrollo

Para contribuir o hacer modificaciones:
1. Seguir la arquitectura limpia establecida
2. Documentar código nuevo
3. Mantener validaciones y manejo de errores
4. Probar endpoints antes de commit

---

**Desarrollado con ❤️ para Papus BarberShop**

