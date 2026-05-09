FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY services/circleguard-identity-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
