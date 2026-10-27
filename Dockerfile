# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Отдельный слой с зависимостями — кэшируется, пока pom.xml не меняется
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package

# ---------- Stage 2: run ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Не запускаем приложение от root
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/notes-service-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
