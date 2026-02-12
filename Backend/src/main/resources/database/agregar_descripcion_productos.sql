-- Script para agregar la columna descripcion a la tabla productos
-- La descripción es opcional (nullable) y puede contener texto largo
-- Este script es seguro y no afectará los productos existentes

-- Verificar si la columna ya existe antes de agregarla
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'productos' 
        AND column_name = 'descripcion'
    ) THEN
        -- Agregar la columna como nullable (opcional)
        ALTER TABLE productos
        ADD COLUMN descripcion TEXT;
    END IF;
END $$;

