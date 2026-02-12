#!/bin/bash

# ======================================================
# PAPUS BARBERSHOP - BUILD & START
# Optimizado para Railway (sin wget/curl, usa mvnw y Node.js del contenedor)
# ======================================================

set -e  # Salir si hay algún error

# Colores para mensajes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_step()    { echo -e "${BLUE}[PASO]${NC} $1"; }
print_success() { echo -e "${GREEN}[✓]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[!]${NC} $1"; }
print_error()   { echo -e "${RED}[✗]${NC} $1"; }

# ==========================================
# Verificar estructura del proyecto
# ==========================================
print_step "Verificando estructura del proyecto..."

if [ ! -d "Backend" ] || [ ! -d "Frontend" ] || [ ! -d "Vista-Clientes" ]; then
    print_error "Este script debe ejecutarse desde la raíz del proyecto PapusBarberShop"
    print_error "Directorios requeridos: Backend, Frontend, Vista-Clientes"
    exit 1
fi
print_success "Estructura del proyecto verificada"

# ==========================================
# Verificar Maven Wrapper o Maven
# ==========================================
print_step "Verificando Maven Wrapper (mvnw)..."
if [ -f "Backend/mvnw" ]; then
    chmod +x Backend/mvnw
    MVN_CMD="./mvnw"
    print_success "Usando mvnw para construir backend"
else
    MVN_CMD="mvn"
    print_warning "No se encontró mvnw, se usará mvn disponible en contenedor"
fi

# ==========================================
# Verificar Node.js y npm
# ==========================================
print_step "Verificando Node.js..."
if ! command -v node &> /dev/null; then
    print_error "Node.js no encontrado en contenedor Railway. Usa un contenedor con Node 20.x"
    exit 1
fi
print_success "Node.js encontrado: $(node -v) (npm $(npm -v))"

# ==========================================
# Verificar Java
# ==========================================
print_step "Verificando Java..."
if ! command -v java &> /dev/null; then
    print_error "Java no encontrado. Railway necesita Java 17+ para Spring Boot"
    exit 1
fi
print_success "Java encontrado: $(java -version 2>&1 | head -n 1)"

# ==========================================
# Construir Backend
# ==========================================
print_step "Construyendo Backend con Maven..."
cd Backend
$MVN_CMD clean package -DskipTests

# Buscar archivo JAR generado
JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" -not -name "*-original.jar" 2>/dev/null | head -n 1)
if [ -z "$JAR_FILE" ]; then
    print_error "No se encontró el archivo JAR en target/"
    exit 1
fi
print_success "Backend construido exitosamente: $JAR_FILE"
cd ..

# ==========================================
# Preparar directorio static
# ==========================================
STATIC_DIR="Backend/src/main/resources/static"
mkdir -p "$STATIC_DIR/admin" "$STATIC_DIR/clientes"
rm -rf "$STATIC_DIR/admin"/* "$STATIC_DIR/clientes"/*

# ==========================================
# Construir Frontend (Admin/Barbero)
# ==========================================
print_step "Construyendo Frontend (Admin/Barbero)..."
cd Frontend

if [ ! -f "package.json" ]; then
    print_error "No se encontró package.json en Frontend"
    exit 1
fi

npm install --legacy-peer-deps
npm run build --if-present || npm run build:prod || npm run build

DIST_CONTENT=$(find dist -maxdepth 1 -type d ! -path dist | head -n 1)
[ -z "$DIST_CONTENT" ] && DIST_CONTENT="dist"

cp -r "$DIST_CONTENT"/* "../../$STATIC_DIR/admin/"
print_success "Frontend (Admin) copiado a static/admin"
cd ..

# ==========================================
# Construir Vista-Clientes
# ==========================================
print_step "Construyendo Vista-Clientes..."
cd Vista-Clientes

if [ ! -f "package.json" ]; then
    print_error "No se encontró package.json en Vista-Clientes"
    exit 1
fi

npm install --legacy-peer-deps
npm run build --if-present || npm run build:prod || npm run build

DIST_CONTENT=$(find dist -maxdepth 1 -type d ! -path dist | head -n 1)
[ -z "$DIST_CONTENT" ] && DIST_CONTENT="dist"

cp -r "$DIST_CONTENT"/* "../../$STATIC_DIR/clientes/"
print_success "Vista-Clientes copiado a static/clientes"
cd ..

# ==========================================
# Ejecutar Backend
# ==========================================
print_step "Iniciando Backend..."
cd Backend

# Usar variable PORT de Railway si está disponible
if [ -n "$PORT" ]; then
    print_warning "Variable PORT detectada: $PORT"
    print_warning "Asegúrate de que application.properties use: server.port=\${PORT:8080}"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}✓ CONSTRUCCIÓN COMPLETADA${NC}"
echo "=========================================="
echo ""
echo -e "${BLUE}Aplicaciones disponibles:${NC}"
echo "  - Admin/Barbero: http://localhost:8080/admin"
echo "  - Clientes: http://localhost:8080/clientes"
echo "  - API Backend: http://localhost:8080/api"
echo ""
echo -e "${GREEN}Ejecutando Backend...${NC}"
echo ""

exec java -jar "$JAR_FILE"
