-- Script para agregar columna barbero_id a la tabla tipos_corte
-- Esto permite asignar un barbero específico a cada tipo de corte

-- Agregar columna barbero_id (opcional, puede ser NULL)
ALTER TABLE tipos_corte 
ADD COLUMN IF NOT EXISTS barbero_id BIGINT;

-- Agregar foreign key constraint
ALTER TABLE tipos_corte 
ADD CONSTRAINT fk_tipos_corte_barbero 
FOREIGN KEY (barbero_id) REFERENCES barberos(id) ON DELETE SET NULL;

-- Crear índice para mejorar las consultas
CREATE INDEX IF NOT EXISTS idx_tipos_corte_barbero_id ON tipos_corte(barbero_id);

