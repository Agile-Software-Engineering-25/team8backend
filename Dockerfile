FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

ENV MAVEN_OPTS="-Dhttp.proxyHost=proxy.example.com -Dhttp.proxyPort=8080"

# Copy pom.xml and download dependencies first 
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]

