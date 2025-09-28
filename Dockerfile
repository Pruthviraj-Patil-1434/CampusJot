#
# Build stage: Use a Maven image that includes JDK 24
#
FROM maven:3-eclipse-temurin-24 AS build
WORKDIR /app
COPY . .

# Change the working directory to where pom.xml is located
WORKDIR /app/CampusJot

# Run the Maven build from the correct directory
RUN mvn clean install -DskipTests

#
# Package stage: Use a lightweight Java 24 JRE for the final image
#
FROM eclipse-temurin:24-jre
WORKDIR /app

# Update the path to copy the JAR from the correct build location
COPY --from=build /app/CampusJot/target/CampusJot-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]