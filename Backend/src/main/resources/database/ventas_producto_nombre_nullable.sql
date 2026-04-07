-- =============================================================================
-- Migración: agregar producto_nombre a ventas_productos
-- =============================================================================
-- La entidad VentaProducto tiene el campo productoNombre (mapeado a producto_nombre)
-- para conservar el nombre del producto cuando se elimina del catálogo.
-- Ejecutar este script en la base de datos de producción.
-- Compatible con PostgreSQL 9.5+ (ADD COLUMN IF NOT EXISTS).
-- =============================================================================

-- 1. Agregar la columna producto_nombre (VARCHAR(200), nullable)
--    Coincide con: @Column(name = "producto_nombre", nullable = true, length = 200)
ALTER TABLE ventas_productos
ADD COLUMN IF NOT EXISTS producto_nombre VARCHAR(200) NULL;

-- 2. Rellenar producto_nombre en filas existentes con el nombre actual del producto
UPDATE ventas_productos vp
SET producto_nombre = p.nombre
FROM productos p
WHERE vp.producto_id = p.id
  AND (vp.producto_nombre IS NULL OR vp.producto_nombre = '');

-- 3. Permitir producto_id NULL (para ventas cuyo producto ya fue eliminado)
ALTER TABLE ventas_productos
ALTER COLUMN producto_id DROP NOT NULL;
