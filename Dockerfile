FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/weather-service-1.0.0.jar ./weather-service.jar
EXPOSE 8086
CMD ["java", "-jar", "weather-service.jar"]
