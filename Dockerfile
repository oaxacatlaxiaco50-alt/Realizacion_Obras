# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /app/uploads /app/license
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
