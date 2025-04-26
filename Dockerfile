FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src ./src
COPY Procfile system.properties ./

RUN ./mvnw package -DskipTests
EXPOSE 8080

CMD ["java", "-jar", "target/UserBackend-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"] 