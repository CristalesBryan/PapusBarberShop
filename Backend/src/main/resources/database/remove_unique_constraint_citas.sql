-- Script para eliminar la restricción UNIQUE de la tabla citas
-- Esto permite que se puedan crear nuevas citas en horas previamente canceladas
-- Ejecutar este script en la base de datos existente

-- Primero, identificar el nombre de la restricción
-- En PostgreSQL, las restricciones UNIQUE se nombran automáticamente como: citas_barbero_id_fecha_hora_key
-- O pueden tener otro nombre. Verificar con:
-- SELECT constraint_name FROM information_schema.table_constraints 
-- WHERE table_name = 'citas' AND constraint_type = 'UNIQUE';

-- Eliminar la restricción única (ajustar el nombre según tu base de datos)
ALTER TABLE citas DROP CONSTRAINT IF EXISTS citas_barbero_id_fecha_hora_key;

-- Si el nombre de la restricción es diferente, usar:
-- ALTER TABLE citas DROP CONSTRAINT IF EXISTS citas_barbero_id_fecha_hora_uk;
-- O buscar el nombre exacto con la consulta anterior

