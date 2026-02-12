# Configuración de Variables de Entorno

Este documento explica cómo configurar las variables de entorno necesarias para ejecutar la aplicación Spring Boot de forma segura.

## Variables de Entorno Requeridas

### AWS Credentials

- `AWS_ACCESS_KEY_ID`: Tu Access Key ID de AWS
- `AWS_SECRET_ACCESS_KEY`: Tu Secret Access Key de AWS

## Configuración en Windows

### Opción 1: PowerShell (Temporal - Solo para la sesión actual)

Abre PowerShell y ejecuta:

```powershell
$env:AWS_ACCESS_KEY_ID="tu-access-key-id-aqui"
$env:AWS_SECRET_ACCESS_KEY="tu-secret-access-key-aqui"
```

### Opción 2: PowerShell (Permanente - Usuario actual)

```powershell
[System.Environment]::SetEnvironmentVariable('AWS_ACCESS_KEY_ID', 'tu-access-key-id-aqui', 'User')
[System.Environment]::SetEnvironmentVariable('AWS_SECRET_ACCESS_KEY', 'tu-secret-access-key-aqui', 'User')
```

**Nota:** Cierra y vuelve a abrir PowerShell para que los cambios surtan efecto.

### Opción 3: Interfaz Gráfica de Windows

1. Presiona `Win + R` y escribe `sysdm.cpl`, luego presiona Enter
2. Ve a la pestaña **"Opciones avanzadas"**
3. Haz clic en **"Variables de entorno"**
4. En **"Variables de usuario"**, haz clic en **"Nuevo"**
5. Agrega:
   - **Nombre de variable:** `AWS_ACCESS_KEY_ID`
   - **Valor de variable:** Tu Access Key ID
6. Repite el proceso para `AWS_SECRET_ACCESS_KEY`
7. Haz clic en **"Aceptar"** en todas las ventanas
8. Reinicia cualquier aplicación que necesite estas variables

### Opción 4: Usando un archivo .env (Recomendado para desarrollo)

1. Crea un archivo `.env` en la raíz del proyecto Backend
2. Agrega las siguientes líneas:

```
AWS_ACCESS_KEY_ID=tu-access-key-id-aqui
AWS_SECRET_ACCESS_KEY=tu-secret-access-key-aqui
```

3. Usa una librería como `dotenv` o configura tu IDE para cargar el archivo `.env`

**Nota:** Asegúrate de que `.env` esté en el `.gitignore` (ya está incluido).

## Configuración en Linux/macOS

### Opción 1: Terminal (Temporal - Solo para la sesión actual)

Abre una terminal y ejecuta:

```bash
export AWS_ACCESS_KEY_ID="tu-access-key-id-aqui"
export AWS_SECRET_ACCESS_KEY="tu-secret-access-key-aqui"
```

### Opción 2: Permanente (Usuario actual)

Edita el archivo `~/.bashrc` o `~/.zshrc` (dependiendo de tu shell):

```bash
nano ~/.bashrc
```

Agrega al final del archivo:

```bash
export AWS_ACCESS_KEY_ID="tu-access-key-id-aqui"
export AWS_SECRET_ACCESS_KEY="tu-secret-access-key-aqui"
```

Guarda el archivo y ejecuta:

```bash
source ~/.bashrc
```

### Opción 3: Permanente (Sistema completo)

Edita el archivo `/etc/environment`:

```bash
sudo nano /etc/environment
```

Agrega:

```
AWS_ACCESS_KEY_ID="tu-access-key-id-aqui"
AWS_SECRET_ACCESS_KEY="tu-secret-access-key-aqui"
```

**Nota:** Requiere reiniciar el sistema o hacer logout/login.

### Opción 4: Usando un archivo .env (Recomendado para desarrollo)

1. Crea un archivo `.env` en la raíz del proyecto Backend
2. Agrega las siguientes líneas:

```
AWS_ACCESS_KEY_ID=tu-access-key-id-aqui
AWS_SECRET_ACCESS_KEY=tu-secret-access-key-aqui
```

3. Usa una librería como `dotenv` o configura tu IDE para cargar el archivo `.env`

## Verificación

Para verificar que las variables están configuradas correctamente:

### Windows (PowerShell):
```powershell
echo $env:AWS_ACCESS_KEY_ID
echo $env:AWS_SECRET_ACCESS_KEY
```

### Linux/macOS:
```bash
echo $AWS_ACCESS_KEY_ID
echo $AWS_SECRET_ACCESS_KEY
```

## Configuración en IDE

### IntelliJ IDEA

1. Ve a **Run** → **Edit Configurations**
2. Selecciona tu configuración de Spring Boot
3. En **Environment variables**, haz clic en el ícono de carpeta
4. Agrega las variables:
   - `AWS_ACCESS_KEY_ID=tu-access-key-id`
   - `AWS_SECRET_ACCESS_KEY=tu-secret-access-key`
5. Haz clic en **OK**

### Eclipse

1. Click derecho en el proyecto → **Run As** → **Run Configurations**
2. Selecciona tu configuración de Spring Boot
3. Ve a la pestaña **Environment**
4. Haz clic en **New** y agrega cada variable
5. Haz clic en **Apply** y luego **Run**

### VS Code

1. Crea un archivo `.vscode/launch.json` en la raíz del proyecto
2. Agrega la configuración:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Spring Boot",
            "request": "launch",
            "mainClass": "com.papusbarbershop.PapusBarbershopApplication",
            "env": {
                "AWS_ACCESS_KEY_ID": "tu-access-key-id-aqui",
                "AWS_SECRET_ACCESS_KEY": "tu-secret-access-key-aqui"
            }
        }
    ]
}
```

## Configuración en Producción

Para producción, configura las variables de entorno en tu plataforma de despliegue:

### Heroku
```bash
heroku config:set AWS_ACCESS_KEY_ID=tu-access-key-id
heroku config:set AWS_SECRET_ACCESS_KEY=tu-secret-access-key
```

### AWS Elastic Beanstalk
Usa la consola de AWS o el archivo `.ebextensions` para configurar las variables.

### Docker
En tu `docker-compose.yml`:
```yaml
services:
  backend:
    environment:
      - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
      - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
```

O en el Dockerfile:
```dockerfile
ENV AWS_ACCESS_KEY_ID=tu-access-key-id
ENV AWS_SECRET_ACCESS_KEY=tu-secret-access-key
```

## Seguridad

⚠️ **IMPORTANTE:**
- Nunca subas tus credenciales AWS al repositorio
- No compartas tus credenciales con nadie
- Usa variables de entorno en lugar de hardcodear valores
- Rota tus credenciales periódicamente
- Considera usar IAM roles en lugar de credenciales cuando sea posible (especialmente en AWS)

## Solución de Problemas

### Error: "Could not resolve placeholder 'AWS_ACCESS_KEY_ID'"

Esto significa que Spring Boot no puede encontrar la variable de entorno. Verifica:
1. Que las variables estén configuradas correctamente
2. Que hayas reiniciado tu IDE/terminal después de configurarlas
3. Que estés usando el nombre correcto de la variable (case-sensitive en Linux/macOS)

### Las variables no persisten después de reiniciar

En Windows, asegúrate de usar la opción "Permanente" o configurarlas en la interfaz gráfica.
En Linux/macOS, asegúrate de agregarlas a `~/.bashrc` o `~/.zshrc` y ejecutar `source`.

