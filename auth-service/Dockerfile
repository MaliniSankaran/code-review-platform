# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Install common-lib into local Maven repo
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN mvn install -f common-lib/pom.xml -DskipTests

# Build auth-service
COPY auth-service/pom.xml auth-service/pom.xml
RUN mvn dependency:resolve -f auth-service/pom.xml
COPY auth-service/src auth-service/src
RUN mvn package -f auth-service/pom.xml -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

