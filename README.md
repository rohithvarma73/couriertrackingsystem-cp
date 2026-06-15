# Courier Tracking System

A Spring Boot based Courier Tracking System REST API for managing customers, parcels, shipments, and tracking histories with validation, global exception handling, Swagger/OpenAPI documentation, Actuator monitoring, and role-based security.

## Project Overview

This project is built as a capstone-style REST API using Spring Boot, Spring Data JPA, and MySQL. It provides a complete backend solution for courier management with layered architecture, DTO-based requests and responses, validation, and clean API responses.

## Features

- Customer management.
- Parcel booking and management.
- Shipment creation and tracking.
- Tracking update history by shipment.
- DTO-based request and response handling.
- Request validation using Jakarta Validation.
- Global exception handling.
- Swagger/OpenAPI documentation.
- Actuator health monitoring.
- Role-based security with USER and ADMIN access.

## Tech Stack

- Java 17
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- Hibernate Validator
- MySQL
- SpringDoc OpenAPI / Swagger
- Spring Boot Actuator
- Spring Security

## Project Structure

- `controller` - REST API endpoints.
- `service` - business logic interfaces.
- `serviceimpl` - service implementations.
- `repository` - JPA repository interfaces.
- `entity` - database entities.
- `dto` - data transfer objects.
- `exception` - custom exceptions and global exception handling.
- `config` - security and Swagger/OpenAPI configuration.

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
springdoc.show-actuator=true

management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

## Security Access

### USER
Can access only GET endpoints.

### ADMIN
Can access all GET, POST, PUT, and DELETE endpoints.

### Demo credentials
- `user / user123`
- `admin / admin123`

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

#### Admin only
- `POST /api/customers/addCust` - Create a customer.
- `PUT /api/customers/{id}` - Update a customer by ID.
- `DELETE /api/customers/{id}` - Delete a customer by ID.

#### User and Admin
- `GET /api/customers/getAll` - Get all customers.
- `GET /api/customers/{id}` - Get a customer by ID.

### Parcel APIs

#### Admin only
- `POST /api/parcels/addParcel` - Create a parcel.
- `PUT /api/parcels/{id}` - Update a parcel by ID.
- `DELETE /api/parcels/{id}` - Delete a parcel by ID.

#### User and Admin
- `GET /api/parcels/getAll` - Get all parcels.
- `GET /api/parcels/{id}` - Get a parcel by ID.

### Shipment APIs

#### Admin only
- `POST /api/shipments/addShipment/{parcelId}` - Create a shipment for a parcel.
- `PUT /api/shipments/{id}/location?currentLocation=Chennai` - Update current shipment location.
- `PUT /api/shipments/{id}` - Update a shipment by ID.
- `DELETE /api/shipments/{id}` - Delete a shipment by ID.

#### User and Admin
- `GET /api/shipments/getAll` - Get all shipments.
- `GET /api/shipments/{id}` - Get a shipment by ID.
- `GET /api/shipments/tracking/{trackingNumber}` - Get shipment by tracking number.

### Tracking History APIs

#### Admin only
- `POST /api/shipments/{shipmentId}/tracking-updates` - Add a tracking history record.
- `PUT /api/tracking-updates/{id}` - Update a tracking history record.

#### User and Admin
- `GET /api/shipments/{shipmentId}/tracking-updates` - Get all tracking history for a shipment.
- `GET /api/tracking-updates/{id}` - Get a tracking history record by update ID.
- `GET /api/tracking-updates` - Get all tracking history records.

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

### Update Parcel
```json
{
  "receiverPhone": "9998887777",
  "weight": 3.0,
  "sourceAddress": "Chennai",
  "destinationAddress": "Hyderabad",
  "bookingDate": "2026-06-12",
  "customerId": 1
}
```

### Create Shipment
No request body is required.

### Update Shipment Location
```json
{
  "currentLocation": "Chennai"
}
```

### Create Tracking Update
```json
{
  "deliveryStatus": "In-Transit",
  "location": "Chennai Hub",
  "remarks": "Parcel reached sorting center"
}
```

### Update Tracking Update
```json
{
  "deliveryStatus": "Delivered",
  "location": "Chennai",
  "remarks": "Package handed over to the customer"
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
- Shipment current location is set from the parcel source address during shipment creation.
- Tracking history is stored per shipment ID.
- Each tracking update creates a new update ID.
- If no tracking updates exist for a shipment, the API can return a message like: `Update to be yet to updated`.

## Author

- Name: Rohith Varma K
- Email: [rohith.varmak@wipro.com](mailto:rohith.varmak@wipro.com)
- Name: Dharshan K S
- Email: [dharshan.ks@wipro.com](mailto:dharshan.ks@wipro.com)
- Project: Courier Tracking System
- Submission Type: Capstone Project