# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY notification-service/pom.xml notification-service/pom.xml
RUN mvn dependency:resolve -f notification-service/pom.xml

COPY notification-service/src notification-service/src
RUN mvn package -f notification-service/pom.xml -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/notification-service/target/*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]