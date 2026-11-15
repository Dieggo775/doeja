# ---- Build ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copia só o pom primeiro: as dependências ficam em cache e o build fica mais rápido
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- Execução (JRE, usuário sem privilégios) ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S doeja && adduser -S doeja -G doeja
COPY --from=builder /app/target/*.jar app.jar
USER doeja

# Ajustado para máquinas pequenas (ex.: 512 MB do plano grátis do Render)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
