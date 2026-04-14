# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN mvn install -f common-lib/pom.xml -DskipTests

COPY gateway-service/pom.xml gateway-service/pom.xml
RUN mvn dependency:resolve -f gateway-service/pom.xml
COPY gateway-service/src gateway-service/src
RUN mvn package -f gateway-service/pom.xml -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/gateway-service/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]