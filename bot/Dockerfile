FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8082
ENV SERVER_PORT=8082

ENTRYPOINT ["/bin/sh", "-c", "sleep 20 && java -jar app.jar"]
