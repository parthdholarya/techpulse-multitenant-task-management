# Stage 1: Build the application using Maven
FROM maven:3.8.3-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final runtime image using Eclipse Temurin Java 17
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp

# Copy the built jar file from the build stage
COPY --from=build /target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app.jar"]