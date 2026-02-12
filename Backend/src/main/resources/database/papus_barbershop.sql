-- ===========================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS
-- PAPUS BARBERSHOP
-- PostgreSQL
-- ===========================================

-- Crear la base de datos (ejecutar como superusuario)
-- CREATE DATABASE papus_barbershop;
-- \c papus_barbershop;

-- ===========================================
-- ELIMINAR TABLAS SI EXISTEN (PARA REINICIO)
-- ===========================================

DROP TABLE IF EXISTS citas CASCADE;
DROP TABLE IF EXISTS tipos_corte CASCADE;
DROP TABLE IF EXISTS ventas_productos CASCADE;
DROP TABLE IF EXISTS servicios CASCADE;
DROP TABLE IF EXISTS horarios CASCADE;
DROP TABLE IF EXISTS productos CASCADE;
DROP TABLE IF EXISTS barberos CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- ===========================================
-- CREAR TABLAS
-- ===========================================

-- Tabla de Usuarios
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMIN', 'BARBERO')),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Barberos
CREATE TABLE barberos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    porcentaje_servicio DECIMAL(5,2) NOT NULL CHECK (porcentaje_servicio >= 0 AND porcentaje_servicio <= 100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Horarios
CREATE TABLE horarios (
    id BIGSERIAL PRIMARY KEY,
    barbero_id BIGINT NOT NULL,
    hora_entrada TIME NOT NULL,
    hora_salida TIME NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barbero_id) REFERENCES barberos(id) ON DELETE CASCADE,
    CHECK (hora_entrada < hora_salida)
);

-- Tabla de Productos
CREATE TABLE productos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    stock INTEGER NOT NULL CHECK (stock >= 0),
    precio_costo DECIMAL(10,2) NOT NULL CHECK (precio_costo >= 0),
    precio_venta DECIMAL(10,2) NOT NULL CHECK (precio_venta >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Tipos de Corte
CREATE TABLE tipos_corte (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    tiempo_minutos INTEGER NOT NULL CHECK (tiempo_minutos > 0),
    precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Mobiliario y Equipo
CREATE TABLE mobiliario_equipo (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(50) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    fecha_adquisicion DATE,
    valor DECIMAL(10,2) NOT NULL CHECK (valor >= 0),
    cantidad INTEGER NOT NULL CHECK (cantidad >= 0) DEFAULT 1,
    ubicacion VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Citas
CREATE TABLE citas (
    id BIGSERIAL PRIMARY KEY,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    barbero_id BIGINT NOT NULL,
    tipo_corte_id BIGINT NOT NULL,
    nombre_cliente VARCHAR(100) NOT NULL,
    correo_cliente VARCHAR(100) NOT NULL,
    telefono_cliente VARCHAR(20),
    comentarios TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'COMPLETADA')),
    correos_enviados TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barbero_id) REFERENCES barberos(id) ON DELETE RESTRICT,
    FOREIGN KEY (tipo_corte_id) REFERENCES tipos_corte(id) ON DELETE RESTRICT
    -- NOTA: Se eliminó la restricción UNIQUE (barbero_id, fecha, hora) para permitir
    -- que se puedan crear nuevas citas en horas previamente canceladas.
    -- La validación de disponibilidad se maneja en el código de la aplicación.
);

-- Tabla de Servicios (Cortes)
CREATE TABLE servicios (
    id BIGSERIAL PRIMARY KEY,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    barbero_id BIGINT NOT NULL,
    tipo_corte VARCHAR(100) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barbero_id) REFERENCES barberos(id) ON DELETE RESTRICT
);

-- Tabla de Ventas de Productos
CREATE TABLE ventas_productos (
    id BIGSERIAL PRIMARY KEY,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    barbero_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    importe DECIMAL(10,2) NOT NULL CHECK (importe >= 0),
    stock_antes INTEGER NOT NULL CHECK (stock_antes >= 0),
    stock_despues INTEGER NOT NULL CHECK (stock_despues >= 0),
    metodo_pago VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barbero_id) REFERENCES barberos(id) ON DELETE RESTRICT,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE RESTRICT
);

-- ===========================================
-- CREAR ÍNDICES PARA OPTIMIZACIÓN
-- ===========================================

CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_servicios_fecha ON servicios(fecha);
CREATE INDEX idx_servicios_barbero ON servicios(barbero_id);
CREATE INDEX idx_horarios_barbero ON horarios(barbero_id);
CREATE INDEX idx_ventas_productos_fecha ON ventas_productos(fecha);
CREATE INDEX idx_ventas_productos_barbero ON ventas_productos(barbero_id);
CREATE INDEX idx_ventas_productos_producto ON ventas_productos(producto_id);
CREATE INDEX idx_citas_fecha ON citas(fecha);
CREATE INDEX idx_citas_barbero ON citas(barbero_id);
CREATE INDEX idx_citas_tipo_corte ON citas(tipo_corte_id);
CREATE INDEX idx_tipos_corte_activo ON tipos_corte(activo);

-- ===========================================
-- INSERTAR DATOS INICIALES
-- ===========================================

-- Insertar barberos precargados
INSERT INTO barberos (nombre, porcentaje_servicio) VALUES
    ('Carlos', 55.00),
    ('Alex', 50.00),
    ('Cesia', 100.00),
    ('Ediel', 100.00);

-- Insertar usuarios por defecto
-- NOTA: Los usuarios se crean automáticamente al iniciar la aplicación
-- mediante DataInitializationService. Si necesitas crearlos manualmente,
-- usa el siguiente comando SQL después de que la aplicación haya iniciado
-- (el hash se genera automáticamente con BCrypt):
-- 
-- El servicio DataInitializationService creará los siguientes usuarios:
-- Usuario ADMIN:
--   Username: admin
--   Password: admin123 (hasheada automáticamente)
--   Rol: ADMIN
-- 
-- Usuario BARBERO:
--   Username: barbero
--   Password: barbero123 (hasheada automáticamente)
--   Rol: BARBERO

-- Insertar tipos de corte
INSERT INTO tipos_corte (nombre, descripcion, tiempo_minutos, precio, activo) VALUES
    ('Corte de Caballero', 'Corte clásico para caballero con acabado profesional', 30, 50.00, TRUE),
    ('Corte para Niño', 'Corte especial para niños con diseño moderno', 25, 40.00, TRUE),
    ('Arreglo de Barba', 'Arreglo y diseño de barba con acabado perfecto', 20, 30.00, TRUE),
    ('Corte y Barba', 'Corte completo con arreglo de barba incluido', 45, 70.00, TRUE);

-- Insertar algunos productos de ejemplo
INSERT INTO productos (nombre, stock, precio_costo, precio_venta) VALUES
    ('Gel para Cabello', 50, 15.00, 25.00),
    ('Pomada', 30, 20.00, 35.00),
    ('Shampoo', 40, 12.00, 22.00),
    ('Acondicionador', 35, 12.00, 22.00),
    ('Cera para Barba', 25, 18.00, 30.00);

-- ===========================================
-- COMENTARIOS Y DOCUMENTACIÓN
-- ===========================================

COMMENT ON TABLE usuarios IS 'Tabla de usuarios del sistema (ADMIN y BARBERO)';
COMMENT ON TABLE barberos IS 'Tabla de barberos con sus porcentajes de servicio';
COMMENT ON TABLE horarios IS 'Tabla de horarios de trabajo de los barberos';
COMMENT ON TABLE tipos_corte IS 'Tabla de tipos de corte disponibles con descripción, tiempo y precio';
COMMENT ON TABLE citas IS 'Tabla de citas agendadas con información del cliente';
COMMENT ON TABLE productos IS 'Tabla de productos del inventario';
COMMENT ON TABLE servicios IS 'Tabla de servicios (cortes) realizados';
COMMENT ON TABLE ventas_productos IS 'Tabla de ventas de productos con control de stock';

COMMENT ON COLUMN usuarios.rol IS 'Rol del usuario: ADMIN o BARBERO';
COMMENT ON COLUMN barberos.porcentaje_servicio IS 'Porcentaje que gana el barbero por cada servicio (0-100)';
COMMENT ON COLUMN horarios.hora_entrada IS 'Hora de inicio del turno del barbero';
COMMENT ON COLUMN horarios.hora_salida IS 'Hora de fin del turno del barbero';
COMMENT ON COLUMN horarios.activo IS 'Indica si el horario está activo (solo un horario activo por barbero)';
COMMENT ON COLUMN tipos_corte.tiempo_minutos IS 'Tiempo estimado en minutos para realizar el corte';
COMMENT ON COLUMN citas.estado IS 'Estado de la cita: PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA';
COMMENT ON COLUMN citas.correos_enviados IS 'Lista de correos a los que se envió la confirmación (separados por comas)';
COMMENT ON COLUMN ventas_productos.stock_antes IS 'Stock del producto antes de la venta';
COMMENT ON COLUMN ventas_productos.stock_despues IS 'Stock del producto después de la venta';

-- ===========================================
-- FIN DEL SCRIPT
-- ===========================================

