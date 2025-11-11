FROM maven:3.9-amazoncorretto-21-alpine as build


WORKDIR /app


COPY pom.xml .

RUN mvn dependency:go-offline


COPY src ./src

RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

ARG JAR_NAME=api-gateway-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/${JAR_NAME} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]