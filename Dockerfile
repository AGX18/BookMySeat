FROM bellsoft/liberica-openjdk-alpine:25

# Set working directory
WORKDIR /app

# Copy built jar
COPY target/*.jar app.jar

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]