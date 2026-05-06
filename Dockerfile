FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/app.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
