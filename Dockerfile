FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/pokemon-tcg-collection-2.0.0.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=9012

EXPOSE 9012

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
