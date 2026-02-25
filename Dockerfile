# Stage 1: Build
FROM gradle:jdk24-alpine AS build
# Create app directory
WORKDIR /app

# Copy jar into container
COPY build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java","-jar","app.jar"]