# Courier Tracking System

A Spring Boot-based Courier Tracking System with REST APIs and a Thymeleaf frontend for managing customers, parcels, shipments, and tracking updates. The project includes validation, DTO-based data flow, global exception handling, Swagger/OpenAPI documentation, Actuator monitoring, and role-based security.

## Overview

This project is designed as a full-stack courier management application. The backend exposes secure REST APIs, while the frontend provides browser-based screens for common operations such as viewing customers, booking parcels, creating shipments, and tracking delivery history.

The application follows a layered architecture with controller, service, repository, entity, DTO, exception, and config packages. It is built to be easy to test, easy to extend, and suitable for capstone or academic evaluation.

## Key Features

- Customer management.
- Parcel booking and parcel history by customer.
- Shipment creation from parcels.
- Shipment tracking and tracking update history.
- Thymeleaf-based frontend pages.
- Role-based access control with USER and ADMIN roles.
- DTO-based request and response handling.
- Jakarta validation for clean input checks.
- Global exception handling for consistent API errors.
- Swagger/OpenAPI documentation.
- Spring Boot Actuator health monitoring.
- Bootstrap-based responsive UI.

## Tech Stack

- Java 17.
- Spring Boot 3.5.x.
- Spring Web.
- Spring Data JPA.
- Hibernate Validator.
- MySQL.
- Thymeleaf.
- Bootstrap 5.
- Spring Security.
- SpringDoc OpenAPI / Swagger.
- Spring Boot Actuator.

## Application Modules

### Backend
- REST APIs for customers, parcels, shipments, and tracking updates.
- Service layer for business logic.
- Repository layer for database access.
- DTOs for request and response mapping.
- Validation and exception handling.
- Security configuration for role-based access.

### Frontend
- Customer list, add, edit, and details screens.
- Parcel list, parcel details, customer-wise parcel list, and parcel form.
- Shipment list, shipment details, shipment edit, and shipment creation flow.
- Tracking list, tracking details, and tracking update screens.
- Search results page for searching across customers, parcels, shipments, and tracking updates.
- Shared navbar, footer, and scripts fragments.

## Project Structure

- `controller` - REST and UI controllers.
- `service` - service interfaces.
- `serviceimpl` - service implementations.
- `repository` - JPA repository interfaces.
- `entity` - database entities.
- `dto` - data transfer objects.
- `exception` - custom exceptions and global exception handling.
- `config` - security and Swagger/OpenAPI configuration.
- `templates` - Thymeleaf frontend pages.
- `static` - CSS, JavaScript, images, and favicon.

## Prerequisites

Before running the application, make sure you have:

- Java 17 or later.
- Maven.
- MySQL Server.
- An IDE such as IntelliJ IDEA or Eclipse.

## Database Setup

Create the database in MySQL:

```sql
CREATE DATABASE courier_tracking_system;
```

If you want a different database name, update the datasource URL in `application.properties`.

## Configuration

Update `src/main/resources/application.properties` with your local database and app settings.

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

## Security Model

### USER
- Can access GET endpoints.
- Can view lists and details.
- Can search records.

### ADMIN
- Can access GET, POST, PUT, and DELETE endpoints.
- Can create, update, and delete customers, parcels, shipments, and tracking updates.
- Can use all frontend actions including add, edit, and delete.

### Demo Credentials
- `user / user123`
- `admin / admin123`

## Running the Application

### Clone the repository

```bash
git clone "https://gitlab1.rpsconsulting.in/26SUB0726_U03/couriertrackingsystem-cp"
cd couriertrackingsystem
```

### Start with Maven

```bash
mvn spring-boot:run
```

### Or run the main class

Run `CouriertrackingsystemApplication` from your IDE.

## Frontend Pages

### Public and shared pages
- Home page.
- Login page.
- Search results page.
- Navbar, footer, and scripts fragments.

### Customer pages
- Customer list.
- Add customer.
- Edit customer.
- Customer details.
- View parcels for a customer.

### Parcel pages
- Parcel list.
- Parcel details.
- Customer-wise parcel list.
- Add parcel form.
- Edit parcel form.

### Shipment pages
- Shipment list.
- Shipment details.
- Start shipment from parcel.
- Edit shipment form.
- Shipment tracking view.

### Tracking pages
- Tracking list.
- Tracking details.
- Add tracking update form.
- Update tracking page.

## Main User Flow

### Customer to Parcel flow
1. Open customer details.
2. Click **View Parcels**.
3. Open the customer parcel list page.
4. Click **Add Parcel**.
5. Parcel form opens with the customer ID already filled and hidden.

### Parcel to Shipment flow
1. Open parcel details.
2. Start shipment for the parcel.
3. Shipment is created using the parcel information.

### Shipment to Tracking flow
1. Open tracking page.
2. View shipment tracking details.
3. Add tracking updates as admin.

## Swagger Documentation

Once the application is running, open:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Swagger helps test the REST APIs and inspect request/response structures.

## Actuator Endpoints

Useful monitoring endpoints include:

- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`

## REST API Endpoints

### Customer APIs

#### Admin only
- `POST /api/customers/addCust` - Create a customer.
- `PUT /api/customers/{id}` - Update a customer.
- `DELETE /api/customers/{id}` - Delete a customer.

#### User and Admin
- `GET /api/customers/getAll` - Get all customers.
- `GET /api/customers/{id}` - Get customer by ID.

### Parcel APIs

#### Admin only
- `POST /api/parcels/addParcel` - Create a parcel.
- `PUT /api/parcels/{id}` - Update a parcel.
- `DELETE /api/parcels/{id}` - Delete a parcel.

#### User and Admin
- `GET /api/parcels/getAll` - Get all parcels.
- `GET /api/parcels/{id}` - Get parcel by ID.

### Shipment APIs

#### Admin only
- `POST /api/shipments/addShipment/{parcelId}` - Create a shipment for a parcel.
- `PUT /api/shipments/{id}/location?currentLocation=Chennai` - Update shipment location.
- `PUT /api/shipments/{id}` - Update shipment by ID.
- `DELETE /api/shipments/{id}` - Delete shipment by ID.

#### User and Admin
- `GET /api/shipments/getAll` - Get all shipments.
- `GET /api/shipments/{id}` - Get shipment by ID.
- `GET /api/shipments/tracking/{trackingNumber}` - Get shipment by tracking number.

### Tracking History APIs

#### Admin only
- `POST /api/shipments/{shipmentId}/tracking-updates` - Add a tracking record.
- `PUT /api/tracking-updates/{id}` - Update a tracking record.

#### User and Admin
- `GET /api/shipments/{shipmentId}/tracking-updates` - Get tracking history for a shipment.
- `GET /api/tracking-updates/{id}` - Get tracking record by ID.
- `GET /api/tracking-updates` - Get all tracking records.

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

The application uses:
- `@Valid` in controllers.
- Jakarta validation annotations such as `@NotBlank`, `@NotNull`, `@Email`, and `@Positive`.
- A global exception handler for validation failures and custom exceptions.

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
- Exception handling tests.
- Security access tests.
- Frontend navigation and form submission checks.

Use `MockMvc` for web-layer tests and `@SpringBootTest` for full integration tests.

## Notes

- Delete APIs return `204 No Content`.
- Shipment current location is initialized from the parcel source address during shipment creation.
- Tracking history is stored per shipment.
- Each tracking update creates a new update record.
- If no tracking updates exist for a shipment, the UI can show a message like `No tracking updates yet`.
- The frontend uses Bootstrap for responsive layout and Thymeleaf fragments for common UI sections.

## Author

- Name: Rohith Varma K
- Email: [rohith.varmak@wipro.com](mailto:rohith.varmak@wipro.com)
- Name: Dharshan K S
- Email: [dharshan.ks@wipro.com](mailto:dharshan.ks@wipro.com)
- Project: Courier Tracking System
- Submission Type: Capstone Project