FROM amazoncorretto:21-alpine-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn

COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline


COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM amazoncorretto:8u472-alpine3.21-jre

WORKDIR /app

ARG JAR_NAME=order-service-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/${JAR_NAME} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]