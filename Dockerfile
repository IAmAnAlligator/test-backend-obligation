FROM eclipse-temurin:21-jre

ENV TZ=Europe/Moscow

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Europe/Moscow", "-jar", "app.jar"]