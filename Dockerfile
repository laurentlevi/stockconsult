# === Build (Java 8 / Spring Boot 2.7) ===
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -B -DskipTests clean package

# === Runtime ===
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/vulnstock-1.0.0.jar app.jar
# La base H2 (fichier) est ecrite dans /app/data -> montez un volume pour la persister
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
