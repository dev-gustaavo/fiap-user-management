# Etapa de build
FROM gradle:8.11.1-jdk17 AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle build --no-daemon

# Etapa de runtime
FROM openjdk:17-slim

WORKDIR /app

ENV APP_NAME=user-management-application
ENV JAR_FILE=${APP_NAME}-0.0.1-SNAPSHOT.jar

COPY --from=build /app/build/libs/${JAR_FILE} /app/${APP_NAME}.jar

ENTRYPOINT ["java", "-jar", "/app/user-management-application.jar"]

EXPOSE 8082
