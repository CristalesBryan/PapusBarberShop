# ---------- ETAPA 1: Build ----------

FROM maven:3.9.6-eclipse-temurin-17 AS build

# Instalar Node 20
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs

WORKDIR /app

# Copiar proyecto
COPY . .

# ===== Build Frontend Admin =====
WORKDIR /app/Frontend
RUN npm install --legacy-peer-deps
RUN npm run build

# ===== Build Frontend Clientes =====
WORKDIR /app/Vista-Clientes
RUN npm install --legacy-peer-deps
RUN npm run build

# Crear carpetas static
RUN mkdir -p /app/Backend/src/main/resources/static/admin
RUN mkdir -p /app/Backend/src/main/resources/static/clientes

# Copiar dist al backend
RUN cp -r dist/* /app/Backend/src/main/resources/static/clientes || true
WORKDIR /app/Frontend
RUN cp -r dist/* /app/Backend/src/main/resources/static/admin || true

# ===== Build Backend =====
WORKDIR /app/Backend
RUN mvn clean package -DskipTests

# ---------- ETAPA 2: Runtime ----------

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=build /app/Backend/target/*.jar app.jar

EXPOSE 8080

ENV PORT=8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]

