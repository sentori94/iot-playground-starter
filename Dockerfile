# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# cache deps
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# build
COPY src ./src
RUN mvn -B -q -DskipTests package spring-boot:repackage

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copier le JAR depuis le build stage
COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

# Variables d'environnement pour X-Ray (optionnelles)
ENV AWS_XRAY_DAEMON_ADDRESS=127.0.0.1:2000
ENV SPRING_PROFILES_ACTIVE=aws

# Démarrage avec options JVM optimisées
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "/app/app.jar"]
