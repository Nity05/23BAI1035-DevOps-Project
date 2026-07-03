# Use Java 21 Runtime
FROM eclipse-temurin:21-jdk

# Working directory inside container
WORKDIR /app

# Copy the generated JAR file
COPY target/hr_portal-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]