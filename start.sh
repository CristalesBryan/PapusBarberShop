#!/bin/bash

# Script autónomo para construir y ejecutar PapusBarberShop
# Optimizado para Railway y entornos Linux sin dependencias previas

set -e  # Salir si hay algún error

echo "=========================================="
echo "  PAPUS BARBERSHOP - BUILD & START"
echo "=========================================="
echo ""

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir mensajes
print_step() {
    echo -e "${BLUE}[PASO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# Directorios temporales para instalaciones
TMP_DIR="/tmp/papusbarbershop-setup"
mkdir -p "$TMP_DIR"

# ==========================================
# VERIFICAR Y INSTALAR MAVEN
# ==========================================
check_and_install_maven() {
    print_step "Verificando Maven..."
    
    if command -v mvn &> /dev/null; then
        MAVEN_VERSION=$(mvn -version | head -n 1)
        print_success "Maven ya está instalado: $MAVEN_VERSION"
        return 0
    fi
    
    print_warning "Maven no encontrado. Verificando mvnw en Backend..."
    
    if [ -f "Backend/mvnw" ]; then
        chmod +x Backend/mvnw
        print_success "Usando Maven Wrapper (mvnw)"
        export MVN_CMD="./mvnw"
        return 0
    fi
    
    print_warning "Instalando Maven..."
    
    # Descargar Maven 3.9.5
    MAVEN_VERSION="3.9.5"
    MAVEN_URL="https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
    
    cd "$TMP_DIR"
    wget -q "$MAVEN_URL" -O maven.tar.gz || curl -L "$MAVEN_URL" -o maven.tar.gz
    tar -xzf maven.tar.gz
    export PATH="$TMP_DIR/apache-maven-${MAVEN_VERSION}/bin:$PATH"
    export MVN_CMD="mvn"
    
    print_success "Maven ${MAVEN_VERSION} instalado"
    cd - > /dev/null
}

# ==========================================
# VERIFICAR Y INSTALAR NODE.JS
# ==========================================
check_and_install_node() {
    print_step "Verificando Node.js y npm..."
    
    if command -v node &> /dev/null && command -v npm &> /dev/null; then
        NODE_VERSION=$(node -v)
        NPM_VERSION=$(npm -v)
        print_success "Node.js ya está instalado: $NODE_VERSION (npm $NPM_VERSION)"
        return 0
    fi
    
    print_warning "Node.js no encontrado. Instalando Node.js 20.x..."
    
    # Instalar Node.js usando nvm o descarga directa
    if command -v nvm &> /dev/null; then
        export NVM_DIR="$HOME/.nvm"
        [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
        nvm install 20
        nvm use 20
        print_success "Node.js instalado via nvm"
        return 0
    fi
    
    # Instalar Node.js directamente
    NODE_VERSION="20.11.0"
    NODE_ARCH=$(uname -m)
    
    case "$NODE_ARCH" in
        x86_64)
            NODE_ARCH="x64"
            ;;
        aarch64|arm64)
            NODE_ARCH="arm64"
            ;;
        *)
            NODE_ARCH="x64"
            ;;
    esac
    
    NODE_URL="https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-${NODE_ARCH}.tar.xz"
    
    cd "$TMP_DIR"
    wget -q "$NODE_URL" -O node.tar.xz || curl -L "$NODE_URL" -o node.tar.xz
    tar -xf node.tar.xz
    export PATH="$TMP_DIR/node-v${NODE_VERSION}-linux-${NODE_ARCH}/bin:$PATH"
    
    print_success "Node.js ${NODE_VERSION} instalado"
    cd - > /dev/null
}

# ==========================================
# VERIFICAR ESTRUCTURA DEL PROYECTO
# ==========================================
print_step "Verificando estructura del proyecto..."

if [ ! -d "Backend" ] || [ ! -d "Frontend" ] || [ ! -d "Vista-Clientes" ]; then
    print_error "Este script debe ejecutarse desde la raíz del proyecto PapusBarberShop"
    print_error "Directorios requeridos: Backend, Frontend, Vista-Clientes"
    exit 1
fi

print_success "Estructura del proyecto verificada"

# ==========================================
# INSTALAR DEPENDENCIAS
# ==========================================
check_and_install_maven
check_and_install_node

# Verificar Java
print_step "Verificando Java..."
if ! command -v java &> /dev/null; then
    print_error "Java no está instalado. Por favor, instala Java 17 o superior"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1)
print_success "Java encontrado: $JAVA_VERSION"

# ==========================================
# CONSTRUIR BACKEND
# ==========================================
print_step "Construyendo Backend con Maven..."

cd Backend

# Asegurar que mvnw sea ejecutable
if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    MVN_CMD="./mvnw"
else
    MVN_CMD="mvn"
fi

# Construir el backend
print_step "Ejecutando: $MVN_CMD clean package -DskipTests"
$MVN_CMD clean package -DskipTests

# Buscar el archivo JAR generado
JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" -not -name "*-original.jar" 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
    print_error "No se encontró el archivo JAR generado en target/"
    exit 1
fi

print_success "Backend construido exitosamente: $JAR_FILE"
cd ..

# ==========================================
# PREPARAR DIRECTORIO STATIC
# ==========================================
print_step "Preparando directorio static en Backend..."
STATIC_DIR="Backend/src/main/resources/static"

# Crear directorio static si no existe
mkdir -p "$STATIC_DIR"

# Limpiar directorios anteriores si existen
if [ -d "$STATIC_DIR/admin" ]; then
    print_warning "Limpiando directorio admin anterior..."
    rm -rf "$STATIC_DIR/admin"
fi

if [ -d "$STATIC_DIR/clientes" ]; then
    print_warning "Limpiando directorio clientes anterior..."
    rm -rf "$STATIC_DIR/clientes"
fi

mkdir -p "$STATIC_DIR/admin"
mkdir -p "$STATIC_DIR/clientes"

# ==========================================
# CONSTRUIR FRONTEND (Admin/Barbero)
# ==========================================
print_step "Construyendo Frontend (Admin/Barbero)..."
cd Frontend

# Verificar que existe package.json
if [ ! -f "package.json" ]; then
    print_error "No se encontró package.json en el directorio Frontend"
    exit 1
fi

# Instalar dependencias
print_step "Instalando dependencias de Frontend..."
npm install --legacy-peer-deps 2>&1 | grep -v "npm WARN" || npm install

# Construir el frontend
print_step "Compilando Frontend (Admin/Barbero)..."
npm run build --if-present || npm run build:prod || npm run build

# Verificar que se creó el directorio dist
if [ ! -d "dist" ]; then
    print_error "No se encontró el directorio dist después de la compilación de Frontend"
    exit 1
fi

# Copiar el build a Backend/src/main/resources/static/admin
print_step "Copiando build de Frontend a static/admin..."

# Buscar el directorio de salida (puede ser dist/ o dist/nombre-proyecto/)
DIST_CONTENT=""
if [ -d "dist" ]; then
    # Buscar el primer subdirectorio o usar dist directamente
    DIST_SUBDIR=$(find dist -maxdepth 1 -type d ! -path dist | head -n 1)
    if [ -n "$DIST_SUBDIR" ]; then
        DIST_CONTENT="$DIST_SUBDIR"
    else
        DIST_CONTENT="dist"
    fi
fi

if [ -n "$DIST_CONTENT" ] && [ -d "$DIST_CONTENT" ]; then
    cp -r "$DIST_CONTENT"/* "../../$STATIC_DIR/admin/" 2>/dev/null || {
        # Si falla, intentar copiar archivos individualmente
        find "$DIST_CONTENT" -type f -exec cp --parents {} "../../$STATIC_DIR/admin/" \;
    }
    print_success "Frontend (Admin) copiado exitosamente a static/admin"
else
    print_error "No se pudo encontrar el contenido del build de Frontend"
    exit 1
fi

cd ..

# ==========================================
# CONSTRUIR VISTA-CLIENTES
# ==========================================
print_step "Construyendo Vista-Clientes..."
cd Vista-Clientes

# Verificar que existe package.json
if [ ! -f "package.json" ]; then
    print_error "No se encontró package.json en el directorio Vista-Clientes"
    exit 1
fi

# Instalar dependencias
print_step "Instalando dependencias de Vista-Clientes..."
npm install --legacy-peer-deps 2>&1 | grep -v "npm WARN" || npm install

# Construir el frontend
print_step "Compilando Vista-Clientes..."
npm run build --if-present || npm run build:prod || npm run build

# Verificar que se creó el directorio dist
if [ ! -d "dist" ]; then
    print_error "No se encontró el directorio dist después de la compilación de Vista-Clientes"
    exit 1
fi

# Copiar el build a Backend/src/main/resources/static/clientes
print_step "Copiando build de Vista-Clientes a static/clientes..."

# Buscar el directorio de salida
DIST_CONTENT=""
if [ -d "dist" ]; then
    DIST_SUBDIR=$(find dist -maxdepth 1 -type d ! -path dist | head -n 1)
    if [ -n "$DIST_SUBDIR" ]; then
        DIST_CONTENT="$DIST_SUBDIR"
    else
        DIST_CONTENT="dist"
    fi
fi

if [ -n "$DIST_CONTENT" ] && [ -d "$DIST_CONTENT" ]; then
    cp -r "$DIST_CONTENT"/* "../../$STATIC_DIR/clientes/" 2>/dev/null || {
        # Si falla, intentar copiar archivos individualmente
        find "$DIST_CONTENT" -type f -exec cp --parents {} "../../$STATIC_DIR/clientes/" \;
    }
    print_success "Vista-Clientes copiado exitosamente a static/clientes"
else
    print_error "No se pudo encontrar el contenido del build de Vista-Clientes"
    exit 1
fi

cd ..

# ==========================================
# LIMPIAR ARCHIVOS TEMPORALES
# ==========================================
print_step "Limpiando archivos temporales..."
rm -rf "$TMP_DIR" 2>/dev/null || true

# ==========================================
# EJECUTAR BACKEND
# ==========================================
print_step "Iniciando Backend..."
cd Backend

# Buscar el archivo JAR
JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" -not -name "*-original.jar" 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
    print_error "No se encontró el archivo JAR para ejecutar"
    exit 1
fi

echo ""
echo "=========================================="
echo -e "${GREEN}  ✓ CONSTRUCCIÓN COMPLETADA${NC}"
echo "=========================================="
echo ""
echo -e "${BLUE}Aplicaciones disponibles en:${NC}"
echo "  - Admin/Barbero: http://localhost:8080/admin"
echo "  - Clientes: http://localhost:8080/clientes"
echo "  - API Backend: http://localhost:8080/api"
echo ""
echo -e "${GREEN}Ejecutando Backend...${NC}"
echo ""

# Ejecutar el backend
# Railway y otros servicios cloud pueden usar PORT env var
if [ -n "$PORT" ]; then
    print_warning "Variable PORT detectada: $PORT"
    print_warning "Asegúrate de que application.properties use: server.port=\${PORT:8080}"
fi

exec java -jar "$JAR_FILE"
