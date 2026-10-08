FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew

COPY src src

RUN ./gradlew clean bootJar --no-daemon


FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup --system tripmatch \
    && adduser --system --ingroup tripmatch tripmatch

WORKDIR /app

COPY --from=builder \
    --chown=tripmatch:tripmatch \
    /workspace/build/libs/*.jar \
    app.jar

USER tripmatch

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]