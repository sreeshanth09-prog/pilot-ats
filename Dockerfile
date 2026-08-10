# Stage 1: Build with Maven + Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the entire project
COPY . .

# Build the JAR (frontend is copied into JAR by maven-resources-plugin)
RUN cd backend && mvn clean package -DskipTests

# Stage 2: Run with slim Java 17 image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/backend/target/resume-ai-0.0.1-SNAPSHOT.jar app.jar

# Expose the port (Render injects PORT env var at runtime)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Xmx400m", "-Xms128m", "-jar", "app.jar"]
