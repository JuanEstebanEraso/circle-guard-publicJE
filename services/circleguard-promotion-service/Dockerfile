FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY services/circleguard-promotion-service/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
