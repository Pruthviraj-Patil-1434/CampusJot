#
# Build stage: Use a Maven image that includes JDK 24
#
FROM maven:3-eclipse-temurin-24 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

#
# Package stage: Use a lightweight Java 24 JRE for the final image
#
FROM eclipse-temurin:24-jre
WORKDIR /app

# IMPORTANT: Replace 'your-build.jar' with the actual name of your JAR file from the /target directory
COPY --from=build /app/target/CampusJot-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
