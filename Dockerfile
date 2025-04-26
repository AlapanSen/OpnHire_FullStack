FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Fix permissions for the Maven wrapper
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src
COPY Procfile system.properties ./

RUN ./mvnw package -DskipTests

# Make sure to expose port 8080
EXPOSE 8080
ENV PORT=8080

# Use -Dserver.port to explicitly set the port
CMD ["java", "-Dserver.port=8080", "-jar", "target/UserBackend-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"] 