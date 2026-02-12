-- Script de corrección para restaurar productos existentes
-- Si la columna comision ya existe pero está causando problemas, este script la corrige

-- Si la columna existe pero no tiene valores, actualizar todos los productos
UPDATE productos
SET comision = 1
WHERE comision IS NULL;

-- Si la columna no existe, agregarla
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
        
        -- Actualizar productos existentes
        UPDATE productos
        SET comision = 1
        WHERE comision IS NULL;
        
        -- Hacer que la columna sea NOT NULL
        ALTER TABLE productos
        ALTER COLUMN comision SET NOT NULL;
        
        -- Agregar constraint
        ALTER TABLE productos
        DROP CONSTRAINT IF EXISTS chk_comision_rango;
        
        ALTER TABLE productos
        ADD CONSTRAINT chk_comision_rango
        CHECK (comision >= 1 AND comision <= 100);
    ELSE
        -- Si la columna ya existe, solo asegurarse de que todos los productos tengan un valor
        UPDATE productos
        SET comision = 1
        WHERE comision IS NULL OR comision < 1 OR comision > 100;
        
        -- Asegurarse de que la constraint existe
        ALTER TABLE productos
        DROP CONSTRAINT IF EXISTS chk_comision_rango;
        
        ALTER TABLE productos
        ADD CONSTRAINT chk_comision_rango
        CHECK (comision >= 1 AND comision <= 100);
    END IF;
END $$;

