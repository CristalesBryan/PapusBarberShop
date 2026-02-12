-- Script para agregar la columna s3_key a la tabla productos
-- Esta columna almacena la clave del objeto en S3 para la imagen del producto

ALTER TABLE productos 
ADD COLUMN IF NOT EXISTS s3_key VARCHAR(500) NULL;

-- Comentario para documentar la columna
COMMENT ON COLUMN productos.s3_key IS 'Clave del objeto en Amazon S3 para la imagen del producto';

