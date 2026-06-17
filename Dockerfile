# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first (for better layer caching)
COPY pom.xml .

# Copy source code
COPY src ./src

# Build application - skip 'go-offline' to avoid resolving SNAPSHOT transitive deps
# mvn package will download only the required stable versions
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/app.jar app.jar

# Expose port (Render will set PORT env variable)
EXPOSE ${PORT:-10000}

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
