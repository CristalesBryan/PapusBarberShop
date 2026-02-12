-- Script para insertar tipos de corte en la base de datos
-- Ejecutar este script en PostgreSQL si los tipos de corte no están en la base de datos

-- Verificar si ya existen tipos de corte
SELECT COUNT(*) FROM tipos_corte;

-- Si la tabla está vacía o quieres insertar los tipos de corte, ejecuta:
INSERT INTO tipos_corte (nombre, descripcion, tiempo_minutos, precio, activo) VALUES
    ('Corte de Caballero', 'Corte clásico para caballero con acabado profesional', 30, 50.00, TRUE),
    ('Corte para Niño', 'Corte especial para niños con diseño moderno', 25, 40.00, TRUE),
    ('Arreglo de Barba', 'Arreglo y diseño de barba con acabado perfecto', 20, 30.00, TRUE),
    ('Corte y Barba', 'Corte completo con arreglo de barba incluido', 45, 70.00, TRUE)
ON CONFLICT DO NOTHING;

-- Verificar que se insertaron correctamente
SELECT * FROM tipos_corte WHERE activo = TRUE;

