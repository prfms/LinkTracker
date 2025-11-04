FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8081
ENV SERVER_PORT=8081

ENTRYPOINT ["java", "-jar", "app.jar"]
