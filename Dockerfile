FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/weather-service-0.1.0.jar ./weather-service.jar
EXPOSE 8086
CMD ["java", "-jar", "weather-service.jar"]
