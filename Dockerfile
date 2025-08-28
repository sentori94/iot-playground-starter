# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# cache deps
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# build
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Variante 1 (la plus simple) :
#   si ton jar est un SNAPSHOT (ex: app-0.0.1-SNAPSHOT.jar)
#   -> décommente cette ligne :
COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar

# Variante 2 (robuste, quel que soit le nom du jar) :
#   dans ton workflow, passe --build-arg JAR_FILE=target/<ton-jar>.jar
# ARG JAR_FILE=target/*.jar
# COPY --from=build /app/${JAR_FILE} /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
