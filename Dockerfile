FROM amazoncorretto:21-alpine3.20-jdk
ARG JAR_FILE=build/libs/*.jar
ENTRYPOINT ["java", "-jar", "app.jar"]