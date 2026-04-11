FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copiar pom.xml primeiro (cache de layers)
COPY pom.xml .
RUN apk add --no-cache maven && mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build
RUN mvn clean package -DskipTests

# Executar
EXPOSE 8080
CMD ["java", "-jar", "target/*.jar"]