FROM maven:3.9-amazoncorretto-21-alpine as build

WORKDIR /app

COPY mvnw pom.xml ./

RUN mvn dependency:go-offline


COPY src ./src

RUN mvn clean package -DskipTests

FROM amazoncorretto:8u472-alpine3.21-jre

WORKDIR /app

ARG JAR_NAME=notification-service-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/${JAR_NAME} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]