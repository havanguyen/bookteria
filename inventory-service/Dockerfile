FROM amazoncorretto:21-alpine-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn

COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline


COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

ARG JAR_NAME=inventory-service-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/${JAR_NAME} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]