# Implementación de Envío Asíncrono de Correos Electrónicos

## 📋 Resumen

Se ha implementado un sistema completo de envío asíncrono de correos electrónicos que **NO bloquea** las respuestas del servidor. El correo se envía en segundo plano usando `ExecutorService`.

---

## 🏗️ Arquitectura

```
┌─────────────────┐
│  CitaService    │  (Lógica de negocio)
│  (Controller)   │
└────────┬────────┘
         │
         │ enviarConfirmacionCitaAsync()
         ▼
┌─────────────────┐
│ EmailAsyncService│  (Coordina el envío)
└────────┬────────┘
         │
         │ ejecutarEnvioAsincrono()
         ▼
┌─────────────────┐
│  EmailExecutor   │  (ExecutorService singleton)
└────────┬────────┘
         │
         │ submit() - NO bloquea
         ▼
┌─────────────────┐
│  Resend (SDK)   │  (Resend API - Envía los correos)
└─────────────────┘
```

Al agendar una cita se envían automáticamente **3 correos** (en segundo plano):
1. **Al cliente:** confirmación con datos de la cita (fecha, hora, barbero, tipo de corte).
2. **Al barbero seleccionado:** notificación de nueva cita con datos del cliente.
3. **Al admin de la barbería:** notificación general de nueva cita (configurar `resend.admin.email`).

---

## 📁 Archivos Creados

### 1. `EmailExecutor.java`
**Ubicación:** `src/main/java/com/papusbarbershop/service/EmailExecutor.java`

**Responsabilidades:**
- Gestiona un `ExecutorService` singleton con pool fijo de 5 hilos
- Proporciona método `ejecutarEnvioAsincrono()` para ejecutar tareas sin bloquear
- Maneja el cierre graceful al detener la aplicación
- Las excepciones se capturan dentro del hilo asíncrono

**Características:**
- ✅ Singleton (una instancia para toda la aplicación)
- ✅ Pool de hilos reutilizable
- ✅ Cierre graceful con `@PreDestroy`
- ✅ Manejo de excepciones no bloqueante

### 2. `EmailAsyncService.java`
**Ubicación:** `src/main/java/com/papusbarbershop/service/EmailAsyncService.java`

**Responsabilidades:**
- Coordina el envío asíncrono de correos
- Usa `EmailExecutor` para ejecutar tareas en segundo plano
- Contiene la lógica de construcción y envío de correos
- Maneja errores sin propagarlos al hilo principal

**Métodos principales:**
- `enviarConfirmacionCitaAsync()` - Envía los 3 correos (cliente, barbero, admin)
- `enviarCorreoAsync()` - Método genérico para enviar cualquier correo

---

## 🔄 Integración en el Flujo Actual

### Antes (Síncrono - Bloqueante):
```java
// En CitaService.crearCita()
emailService.enviarConfirmacionCita(...);  // ❌ BLOQUEA la respuesta
return convertirADTO(citaGuardada);        // Espera a que termine el envío
```

### Ahora (Asíncrono - No Bloqueante):
```java
// En CitaService.crearCita()
emailAsyncService.enviarConfirmacionCitaAsync(...);  // ✅ NO bloquea
return convertirADTO(citaGuardada);                   // Respuesta inmediata
```

---

## 💻 Ejemplo de Uso desde un Servlet (Referencia)

Aunque el proyecto usa Spring Boot, aquí está cómo se usaría desde un Servlet tradicional:

```java
@WebServlet("/crearCita")
public class CrearCitaServlet extends HttpServlet {
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private EmailAsyncService emailAsyncService;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Obtener datos del formulario
        String nombreCliente = request.getParameter("nombreCliente");
        String correo = request.getParameter("correo");
        // ... otros parámetros
        
        // 2. Guardar en base de datos (NO bloquea)
        CitaDTO cita = citaService.crearCita(citaCreateDTO);
        
        // 3. Enviar correo de forma ASÍNCRONA (NO bloquea)
        emailAsyncService.enviarConfirmacionCitaAsync(
            Arrays.asList(correo),
            nombreCliente,
            fecha,
            hora,
            barberoNombre,
            tipoCorteNombre,
            comentarios
        );
        
        // 4. Respuesta inmediata al usuario
        response.sendRedirect("citas.jsp?exito=true");
        // ✅ El usuario recibe la respuesta INMEDIATAMENTE
        // ✅ El correo se envía en segundo plano
    }
}
```

---

## 📝 Ejemplo de Uso desde Spring Boot Controller

```java
@RestController
@RequestMapping("/api/citas")
public class CitaController {
    
    @Autowired
    private CitaService citaService;
    
    @PostMapping
    public ResponseEntity<CitaDTO> crearCita(@RequestBody CitaCreateDTO citaDTO) {
        // 1. Guardar en base de datos
        CitaDTO citaCreada = citaService.crearCita(citaDTO);
        
        // 2. El envío de correo se hace ASÍNCRONO dentro de CitaService
        //    No necesitamos hacer nada aquí - ya está integrado
        
        // 3. Respuesta inmediata
        return ResponseEntity.ok(citaCreada);
        // ✅ El usuario recibe la respuesta INMEDIATAMENTE
        // ✅ El correo se envía en segundo plano
    }
}
```

---

## 🎯 Ventajas de la Implementación

### ✅ No Bloquea la Respuesta
- El usuario recibe la respuesta inmediatamente
- El correo se envía en segundo plano

### ✅ Escalable
- Pool de 5 hilos puede manejar múltiples envíos simultáneos
- Se puede ajustar el tamaño del pool según necesidad

### ✅ Manejo de Errores Robusto
- Las excepciones se capturan dentro del hilo asíncrono
- No afectan la respuesta al usuario
- Se registran en los logs para monitoreo

### ✅ Desacoplado
- `EmailAsyncService` está separado de la lógica de negocio
- Fácil de reutilizar en otros servicios

### ✅ Cierre Graceful
- Al detener la aplicación, espera a que los correos en proceso terminen
- Evita pérdida de correos pendientes

---

## 🔧 Configuración

El sistema usa **Resend** para el envío de correos. Configuración en `application.properties` o variables de entorno:
- `RESEND_API_KEY` - API Key de Resend (https://resend.com/api-keys)
- `RESEND_FROM_EMAIL` - Remitente (ej: `Citas Papus BarberShop <citas@papusbarbershop.com>`)
- `RESEND_ADMIN_EMAIL` - Email del admin para notificaciones de nuevas citas (opcional)

El sistema usa:
- `ExecutorService` con pool fijo de 5 hilos
- SDK Resend (resend-java) para enviar los correos
- Logging automático de todas las operaciones

---

## 📊 Flujo Completo

```
1. Usuario crea una cita
   ↓
2. CitaService.crearCita() guarda en BD
   ↓
3. emailAsyncService.enviarConfirmacionCitaAsync() se llama
   ↓
4. EmailExecutor.ejecutarEnvioAsincrono() envía tarea al pool
   ↓
5. CitaService retorna respuesta INMEDIATAMENTE ✅
   ↓
6. (En segundo plano) Hilo del pool ejecuta el envío
   ↓
7. Resend envía los 3 correos (cliente, barbero, admin)
   ↓
8. Logs registran el resultado
```

---

## 🚀 Uso Genérico

Para enviar cualquier correo de forma asíncrona:

```java
@Autowired
private EmailAsyncService emailAsyncService;

// Enviar correo genérico
emailAsyncService.enviarCorreoAsync(
    "cliente@example.com",
    "Asunto del correo",
    "Cuerpo del mensaje"
);
```

---

## ⚠️ Notas Importantes

1. **No modifica la lógica existente**: El guardado en BD sigue igual
2. **No cambia las vistas**: Las JSP/HTML no se modifican
3. **No bloquea**: Todas las llamadas son no bloqueantes
4. **Manejo de errores**: Los errores se registran pero no afectan la respuesta

---

## 📈 Monitoreo

Los logs muestran claramente:
- Cuándo se envía una tarea al pool asíncrono
- Cuándo se completa el envío
- Cualquier error que ocurra (sin afectar al usuario)

Ejemplo de logs:
```
INFO  - Tarea de envío de correo enviada al pool asíncrono
INFO  - Iniciando envío asíncrono de correos...
INFO  - ✓ Correo enviado exitosamente a: cliente@example.com
```

---

## ✅ Objetivo Cumplido

✅ Envío asíncrono implementado  
✅ No bloquea la respuesta del servidor  
✅ Usa ExecutorService (no Thread.sleep ni hilos manuales)  
✅ Clase dedicada para manejo asíncrono  
✅ Desacoplado del controlador  
✅ Singleton reutilizable  
✅ Manejo de excepciones dentro del hilo asíncrono  
✅ Usuario recibe respuesta inmediata  
✅ Correo se envía en segundo plano de forma segura y escalable

