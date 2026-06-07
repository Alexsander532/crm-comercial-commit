# ================================================================
# STAGE 1: BUILD
# ================================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar dependências primeiro (cache de layers)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte e compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ================================================================
# STAGE 2: RUN
# ================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Criar usuário não-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar JAR do build stage
COPY --from=build /app/target/*.jar app.jar

# Configurar usuário não-root
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=5 CMD wget -qO- http://localhost:8080/api/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
