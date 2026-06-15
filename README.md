# Courier Tracking System

A Spring Boot based Courier Tracking System REST API for managing customers, parcels, shipments, and tracking updates with validation, global exception handling, Swagger documentation, and Actuator monitoring.

## Project Overview

This project is built as a capstone-style REST API using Spring Boot, Spring Data JPA, and MySQL. It provides a complete backend solution for courier management with layered architecture and clean API responses.

## Features

- Customer management.
- Parcel booking and management.
- Shipment creation and tracking.
- Tracking update history.
- DTO-based request and response handling.
- Request validation using Jakarta Validation.
- Global exception handling.
- Swagger/OpenAPI documentation.
- Actuator health monitoring.

## Tech Stack

- Java 17
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- Hibernate Validator
- MySQL
- SpringDoc OpenAPI / Swagger
- Spring Boot Actuator

## Project Structure

- `controller` - REST API endpoints.
- `service` - business logic interfaces.
- `serviceimpl` - service implementations.
- `repository` - JPA repository interfaces.
- `entity` - database entities.
- `dto` - data transfer objects.
- `exception` - custom exceptions and global exception handling.
- `config` - Swagger/OpenAPI configuration.

## Prerequisites

Before running the project, make sure you have:

- Java 17 or later installed.
- Maven installed.
- MySQL Server running.
- An IDE such as IntelliJ IDEA or Eclipse.

## Database Setup

Create a MySQL database for the project:

```sql
CREATE DATABASE courier_tracking_system;
```

If your database name is different, update it in `application.properties`.

## Configuration

Update `src/main/resources/application.properties` with your MySQL credentials.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/courier_tracking_system
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs

management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

## How to Run

### Clone the repository

```bash
git clone "https://gitlab1.rpsconsulting.in/26SUB0726_U03/couriertrackingsystem-cp"
cd couriertrackingsystem
```

### Run the application

Using Maven:

```bash
mvn spring-boot:run
```

Or run the main class:

```bash
CouriertrackingsystemApplication
```

## Swagger Documentation

Once the application starts, open:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Swagger is useful for testing and exploring all API endpoints in one place.

## Actuator Endpoints

The application exposes useful actuator endpoints for monitoring.

- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`

## API Endpoints

### Customer APIs

- `POST /api/customers` - Create a customer.
- `GET /api/customers` - Get all customers.
- `GET /api/customers/{id}` - Get a customer by ID.
- `PUT /api/customers/{id}` - Update a customer by ID.
- `DELETE /api/customers/{id}` - Delete a customer by ID.

### Parcel APIs

- `POST /api/parcels` - Create a parcel.
- `GET /api/parcels` - Get all parcels.
- `GET /api/parcels/{id}` - Get a parcel by ID.
- `PUT /api/parcels/{id}` - Update a parcel by ID.
- `DELETE /api/parcels/{id}` - Delete a parcel by ID.

### Shipment APIs

- `POST /api/shipments` - Create a shipment.
- `GET /api/shipments` - Get all shipments.
- `GET /api/shipments/{id}` - Get a shipment by ID.
- `PUT /api/shipments/{id}` - Update a shipment by ID.
- `DELETE /api/shipments/{id}` - Delete a shipment by ID.

### Tracking Update APIs

- `POST /api/tracking-updates` - Add a tracking update.
- `GET /api/tracking-updates` - Get all tracking updates.
- `GET /api/tracking-updates/{id}` - Get a tracking update by ID.
- `PUT /api/tracking-updates/{id}` - Update a tracking update by ID.
- `DELETE /api/tracking-updates/{id}` - Delete a tracking update by ID.

## Sample Request Bodies

### Create Customer
```json
{
  "customerName": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "phone": "9876543210",
  "address": "Bangalore"
}
```

### Create Parcel
```json
{
  "receiverPhone": "9988776655",
  "weight": 2.5,
  "sourceAddress": "Delhi",
  "destinationAddress": "Mumbai",
  "bookingDate": "2026-06-12",
  "customerId": 1
}
```

### Create Shipment
```json
{
  "trackingNumber": "TRK1001",
  "shipmentDate": "2026-06-12",
  "currentLocation": "Delhi Hub",
  "estimatedDeliveryDate": "2026-06-15",
  "parcelId": 1
}
```

### Create Tracking Update
```json
{
  "deliveryStatus": "In Transit",
  "location": "Chennai Hub",
  "remarks": "Parcel reached sorting center",
  "updatedTime": "2026-06-12T18:00:00",
  "shipmentId": 1
}
```

## Validation and Error Handling

The project uses:
- `@Valid` in controllers.
- Jakarta validation annotations like `@NotBlank`, `@NotNull`, `@Email`, and `@Positive`.
- A global exception handler for validation and custom exceptions.

Example error response:

```json
{
  "timestamp": "2026-06-12T18:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request data",
  "fieldErrors": {
    "email": "Email is required"
  }
}
```

## Testing

Recommended test areas:
- Controller tests.
- Service tests.
- Validation error tests.
- Resource not found exception tests.

Spring Boot supports web-layer testing with `MockMvc` and application tests with `@SpringBootTest`.

## Notes

- Delete APIs return `204 No Content`.
- Related records must exist before creating dependent records.
- Swagger UI can be used to test all APIs interactively.

## Author

- Name: Rohith Varma K
- Project: Courier Tracking System
- Submission Type: Capstone Project