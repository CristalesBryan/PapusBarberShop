-- Script para agregar la columna comision a la tabla productos
-- La comisión debe ser un valor entre 1 y 100 (porcentaje)
-- Este script es seguro y no afectará los productos existentes

-- Verificar si la columna ya existe antes de agregarla
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'productos' 
        AND column_name = 'comision'
    ) THEN
        -- Agregar la columna como nullable con valor por defecto
        ALTER TABLE productos
        ADD COLUMN comision INTEGER DEFAULT 1;
        
        -- Actualizar productos existentes con un valor por defecto de 1
        UPDATE productos
        SET comision = 1
        WHERE comision IS NULL;
        
        -- Ahora hacer que la columna sea NOT NULL
        ALTER TABLE productos
        ALTER COLUMN comision SET NOT NULL;
        
        -- Agregar constraint para validar que la comisión esté entre 1 y 100
        ALTER TABLE productos
        ADD CONSTRAINT chk_comision_rango
        CHECK (comision >= 1 AND comision <= 100);
    END IF;
END $$;

