FROM eclipse-temurin:17-jdk-jammy

LABEL maintainer="Rohith Varma","Dharshan K S"
LABEL version="1.0"
LABEL description="Nexus Courier Tracking System"

# Create a non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring

WORKDIR /app
COPY target/couriertrackingsystem-0.0.1-SNAPSHOT.jar app.jar

# Set ownership to the non-root user
RUN chown spring:spring /app/app.jar

# Run as non-root user
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]