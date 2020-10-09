FROM maven:3-openjdk-11 AS base

FROM base AS build

COPY src /build/src
COPY pom.xml /build/pom.xml
WORKDIR /build
RUN mvn package

FROM base AS deploy
COPY --from=build /build/target/odc-adapter-postgres-*-fat.jar /home/app/odc-adapter-postgres.jar
WORKDIR /home/app

ENTRYPOINT ["java", "-jar", "odc-adapter-postgres.jar"]
