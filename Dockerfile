FROM eclipse-temurin:17-jdk-jammy

LABEL maintainer="Rohith Varma, Dharshan K S"
LABEL version="1.0"
LABEL description="Nexus Courier Tracking System"

RUN addgroup --system spring && adduser --system --ingroup spring spring

WORKDIR /app

COPY --chown=spring:spring target/couriertrackingsystem-0.0.1-SNAPSHOT.jar /app/app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]