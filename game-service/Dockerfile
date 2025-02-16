FROM gradle:jdk23-alpine AS warmup

WORKDIR /app

COPY ./gradle ./gradle
COPY ./build.gradle ./build.gradle
COPY ./settings.gradle ./settings.gradle
COPY ./gradlew ./

RUN ./gradlew clean

FROM warmup AS builder

COPY ./src ./src
RUN ./gradlew bootJar

FROM openjdk:23 AS runner

WORKDIR /app

COPY --from=builder /app/build/libs/game-service-1.0-SNAPSHOT.jar ./app.jar
