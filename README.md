# Nexus — Courier Tracking System

A full-stack parcel delivery and tracking platform built with **Spring Boot 3** and **Thymeleaf**. Users can self-register, book parcels, and follow their deliveries in real time. Administrators manage the entire operation — customers, shipments, and tracking updates — from a single dashboard.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Architecture Overview](#architecture-overview)
3. [Entity Model](#entity-model)
4. [Security Model](#security-model)
5. [Prerequisites](#prerequisites)
6. [Database Setup](#database-setup)
7. [Configuration](#configuration)
8. [Running the Application](#running-the-application)
9. [Seeded Test Data](#seeded-test-data)
10. [Frontend Pages](#frontend-pages)
11. [User Flows](#user-flows)
12. [REST API Reference](#rest-api-reference)
13. [Error Handling](#error-handling)
14. [Project Structure](#project-structure)
15. [Author](#author)

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| UI | Thymeleaf (server-side rendering) |
| Security | Spring Security 6 |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Monitoring | Spring Boot Actuator |
| Fonts | DM Serif Display, Inter, JetBrains Mono (Google Fonts CDN) |
| Icons | Font Awesome 6.5 |
| Animation | GSAP 3.12.7 + ScrollTrigger |
| Build | Maven |

---

## Architecture Overview

```
Browser
  │
  ├── Thymeleaf MPA (Multi-Page Application)
  │     └── UI Controllers (@Controller)
  │           ├── HomeController       → /  /dashboard  /home
  │           ├── AuthController       → /register
  │           ├── CustomerUiController → /customers/**
  │           ├── ParcelUiController   → /parcels/**
  │           ├── ShipmentUiController → /shipments/**
  │           ├── TrackingUpdateUiController → /tracking/**
  │           ├── ProfileController    → /profile  /profile/edit
  │           └── SearchController     → /search
  │
  └── REST API (JSON)
        └── REST Controllers (@RestController)  /api/**
              ├── CustomerController
              ├── ParcelController
              ├── ShipmentController
              └── TrackingUpdateController

Service Layer (interfaces + implementations)
  ├── AuthService / AuthServiceImpl
  ├── CustomerService / CustomerServiceImpl
  ├── ParcelService / ParcelServiceImpl
  ├── ShipmentService / ShipmentServiceImpl
  └── TrackingUpdateService / TrackingUpdateServiceImpl

Repository Layer (Spring Data JPA)
  ├── AppUserRepository
  ├── CustomerRepository
  ├── ParcelRepository
  ├── ShipmentRepository
  └── TrackingUpdateRepository

Exception Handling (split by request type)
  ├── UiExceptionHandler   → @ControllerAdvice — renders HTML error pages
  └── GlobalExceptionHandler → @RestControllerAdvice(annotations=RestController.class) — returns JSON
```

---

## Entity Model

```
AppUser (1) ──────────────────── (0..1) Customer
   │                                       │
   │  (createdBy)                          │
   │                              (1) ─── (*) Parcel
   │                                       │
   │                                       │
   │                              (1) ─── (0..1) Shipment
   │                                       │
   │                                       │
   │                              (1) ─── (*) TrackingUpdate
   │
   └── roles: Set<String> {ROLE_USER, ROLE_ADMIN}
```

### AppUser
| Field | Type | Notes |
|-------|------|-------|
| `userId` | Long | PK, auto-increment |
| `username` | String | Unique, used for login |
| `password` | String | BCrypt-encoded |
| `roles` | `Set<String>` | `ROLE_USER` or `ROLE_ADMIN` |
| `customer` | Customer | One-to-one, nullable (admins have no customer) |

### Customer
| Field | Type | Notes |
|-------|------|-------|
| `customerId` | Long | PK |
| `customerName` | String | Required |
| `email` | String | Required, valid email |
| `phone` | String | Required |
| `address` | String | Required |
| `createdBy` | AppUser | FK → AppUser — who created this customer |

### Parcel
| Field | Type | Notes |
|-------|------|-------|
| `parcelId` | Long | PK |
| `customer` | Customer | FK |
| `receiverPhone` | String | Auto-populated from customer's phone |
| `weight` | BigDecimal | Required, ≥ 0.1 |
| `sourceAddress` | String | Pickup location |
| `destinationAddress` | String | Delivery location |
| `bookingDate` | LocalDate | Defaults to today |
| `createdBy` | AppUser | FK → AppUser |
| `shipment` | Shipment | One-to-one, nullable |

### Shipment
| Field | Type | Notes |
|-------|------|-------|
| `shipmentId` | Long | PK |
| `parcel` | Parcel | FK (one-to-one) |
| `trackingNumber` | String | Auto-generated `TRK-{8-char UUID}` |
| `shipmentDate` | LocalDate | Set at creation |
| `currentLocation` | String | Initialized from parcel source address |
| `estimatedDeliveryDate` | LocalDate | Set to `shipmentDate + 3 days` |
| `createdBy` | AppUser | FK |

### TrackingUpdate
| Field | Type | Notes |
|-------|------|-------|
| `updateId` | Long | PK |
| `shipment` | Shipment | FK |
| `location` | String | Required |
| `deliveryStatus` | String | One of: In Transit, Out for Delivery, Delivered, Delayed, Pending |
| `remarks` | String | Optional |
| `timestamp` | LocalDateTime | Auto-set on creation |

---

## Security Model

Spring Security 6 with BCrypt password encoding and session-based authentication.

### Roles

| Role | Who | Access |
|------|-----|--------|
| `ROLE_USER` | Self-registered customers | Own parcels, own shipments (view), own profile, booking |
| `ROLE_ADMIN` | Admin accounts | All customers, all parcels, all shipments, tracking management |

### URL Authorization Matrix

| URL Pattern | Access |
|-------------|--------|
| `/login`, `/register`, `/home`, `/css/**`, `/js/**`, `/favicon.ico` | Public |
| `/`, `/dashboard` | Authenticated |
| `/profile`, `/profile/edit` | Authenticated |
| `/parcels`, `/parcels/new`, `/parcels/save`, `/parcels/{id}` | Authenticated |
| `/parcels/{id}/edit`, `/parcels/{id}/update`, `/parcels/{id}/delete` | Authenticated (own data) |
| `/customers/**` | `ROLE_ADMIN` only |
| `/shipments/new`, `/shipments/save`, `/shipments/{id}/edit`, `/shipments/{id}/update`, `/shipments/{id}/delete`, `/shipments/start/**` | `ROLE_ADMIN` only |
| `/shipments`, `/shipments/{id}` | Authenticated (data isolation) |
| `/tracking/shipment/{id}/new`, `/tracking/shipment/{id}/save`, `/tracking/update/{id}/delete` | `ROLE_ADMIN` only |
| `/tracking`, `/tracking/shipment/{id}` | Authenticated |
| `/api/**` | `ROLE_ADMIN` (POST/PUT/DELETE), Authenticated (GET) |
| `/swagger-ui/**`, `/v3/api-docs/**` | Authenticated |
| `/actuator/**` | `ROLE_ADMIN` |
| `/search` | Authenticated |

### Data Isolation (USER role)

- **Parcels:** Users see only parcels where `createdBy.username = currentUser`
- **Shipments:** Users see only shipments for their own parcels
- **Tracking:** Users can view tracking history for their own shipments
- **Profile:** Users manage their own Customer record via `/profile`

### Registration Flow

```
POST /register
  → AuthServiceImpl.register()
  → Creates AppUser (ROLE_USER, BCrypt password)
  → Creates linked Customer entity
  → Both saved atomically
  → User can log in immediately
```

---

## Prerequisites

- **Java 17** or later
- **Maven 3.6+**
- **MySQL 8** running locally
- An IDE (IntelliJ IDEA or Eclipse recommended)

---

## Database Setup

```sql
CREATE DATABASE courier_tracking_system;
```

> If you change the database name, update `application.properties` accordingly.

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/courier_tracking_system
spring.datasource.username=root
spring.datasource.password=your_password

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Swagger UI
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

---

## Running the Application

### Clone and run

```bash
git clone https://github.com/rohithvarma73/couriertrackingsystem-cp.git
cd couriertrackingsystem-cp
mvn spring-boot:run
```

### Or run from your IDE

Run the main class: `com.wip.couriertrackingsystem.CouriertrackingsystemApplication`

The application starts on **`http://localhost:8080`**.

---

## Seeded Test Data

On first startup, `DatabaseSeederConfig` automatically creates the following accounts if they don't exist:

| Username | Password | Role | Notes |
|----------|----------|------|-------|
| `admin` | `admin123` | ADMIN | No customer profile |
| `user` | `user123` | USER | Linked to customer "John Doe" with 2 seeded parcels |
| `jane` | `jane123` | USER | Linked to customer "Jane Smith" with 1 seeded parcel |

> Seeded data is idempotent — re-running the app will not duplicate records.

---

## Frontend Pages

### Design System

- **Color palette:** Yoghurt cream (`#FAF3E4`) background · Vivid Orange (`#FFA102`) accent · Her Highness purple (`#432E6F`) navbar
- **Typography:** DM Serif Display (brand/hero) · Inter (body) · JetBrains Mono (tracking numbers/IDs)
- **Animations:** GSAP 3.12.7 — page transitions, ScrollTrigger table row stagger, stat counter count-up, hover micro-interactions
- **Error display:** All errors render as HTML pages or inline form boxes — no raw JSON ever reaches the browser

### Public Pages

| URL | Template | Description |
|-----|----------|-------------|
| `/` or `/home` | `home.html` | Landing page with hero and feature cards |
| `/login` | `login.html` | Sign-in form |
| `/register` | `register.html` | Self-registration (creates USER + Customer) |

### Dashboard

| URL | Template | Description |
|-----|----------|-------------|
| `/dashboard` | `index.html` | Personalized dashboard with animated stat cards |

Admin sees: Total customers, Total parcels, Active shipments  
User sees: My parcels count, My shipments count

### Profile (USER role)

| URL | Template | Description |
|-----|----------|-------------|
| `/profile` | `profile/view.html` | View own contact details |
| `/profile/edit` | `profile/edit.html` | Edit own name, email, phone, address |

### Customer Management (ADMIN only)

| URL | Template | Description |
|-----|----------|-------------|
| `/customers` | `customer/list.html` | All customers table |
| `/customers/new` | `customer/form.html` | Add customer form |
| `/customers/{id}` | `customer/details.html` | Customer detail view |
| `/customers/{id}/edit` | `customer/form.html` | Edit customer |
| `/customers/{id}/delete` | POST → redirect | Delete with error if parcels exist |

### Parcel Management

| URL | Template | Access |
|-----|----------|--------|
| `/parcels` | `parcel/list.html` | Authenticated (data-isolated) |
| `/parcels/new` | `parcel/form.html` | Authenticated |
| `/parcels/{id}` | `parcel/details.html` | Authenticated (own only) |
| `/parcels/{id}/edit` | `parcel/form.html` | Authenticated (own only) |
| `/parcels/by-customer/{customerId}` | `parcel/by-customer.html` | Authenticated |

### Shipment Management

| URL | Template | Access |
|-----|----------|--------|
| `/shipments` | `shipment/list.html` | Authenticated (data-isolated) |
| `/shipments/new` | `shipment/form.html` | ADMIN only |
| `/shipments/{id}` | `shipment/details.html` | Authenticated (own only) |
| `/shipments/{id}/edit` | `shipment/form.html` | ADMIN only |
| `/shipments/by-parcel/{parcelId}` | `shipment/start.html` | ADMIN — start or view existing shipment |

> If a shipment already exists for a parcel, the start page shows a smart card linking to the existing shipment — no duplicate creation possible.

### Tracking

| URL | Template | Access |
|-----|----------|--------|
| `/tracking` | `tracking/list.html` | Authenticated — pick a shipment |
| `/tracking/shipment/{id}` | `tracking/details.html` | Authenticated — animated timeline |
| `/tracking/shipment/{id}/new` | `tracking/form.html` | ADMIN only — add update |

### Search

| URL | Template | Description |
|-----|----------|-------------|
| `/search?q={keyword}` | `search/results.html` | Global search across parcels, shipments, tracking |

### Error Pages

| Template | Rendered when |
|----------|--------------|
| `error/not-found.html` | Any `ResourceNotFoundException` from a UI controller |
| `error/bad-request.html` | Any `IllegalStateException` from a UI controller |
| `error/general.html` | Any unexpected exception from a UI controller |
| `error/404.html` | Spring's built-in 404 |
| `error/access-denied.html` | Accessing a URL without required role |

---

## User Flows

### Self-Registration → First Parcel

```
1. Open /register → fill name, username, password, email, phone, address
2. Submit → AppUser (ROLE_USER) + Customer created atomically
3. Redirected to /login → sign in
4. Dashboard shows "My Dashboard" with personal stats
5. Click "Book a parcel" → /parcels/new
   - Customer ID auto-filled from authenticated user (hidden)
   - Fill weight, addresses, date → submit
6. Parcel saved → redirected to /parcels/{id} (detail page)
7. View /tracking to follow shipment once admin dispatches it
```

### Admin: Dispatch a Parcel

```
1. Admin logs in → Overview dashboard (all system stats)
2. /customers → view all registered customers
3. /parcels → view all parcels
4. Open parcel → click "Start shipment" → /shipments/by-parcel/{id}
   - If shipment exists: page shows warning + link to existing shipment
   - If not: confirmation card with "Start shipment" button
5. POST /shipments/start/{parcelId}
   → Tracking number generated: TRK-{UUID}
   → Estimated delivery = today + 3 days
   → Current location = parcel source address
6. Redirected to /shipments/{id}
7. Add tracking updates via /tracking/shipment/{id}/new
```

### Tracking Timeline (Customer View)

```
1. User visits /tracking
2. Sees list of shipments for their parcels
3. Clicks a shipment → /tracking/shipment/{id}
4. Vertical animated timeline shows each update:
   - Date, location, status badge, remarks
5. Most recent update shown at top
```

---

## REST API Reference

Base URL: `http://localhost:8080/api`

> Full interactive docs: **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Customer API `/api/customers`

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/getAll` | Authenticated | List all customers |
| `GET` | `/{id}` | Authenticated | Get customer by ID |
| `POST` | `/addCust` | ADMIN | Create customer |
| `PUT` | `/{id}` | ADMIN | Update customer |
| `DELETE` | `/{id}` | ADMIN | Delete customer |

### Parcel API `/api/parcels`

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/getAll` | Authenticated | List all parcels |
| `GET` | `/{id}` | Authenticated | Get parcel by ID |
| `POST` | `/addParcel` | ADMIN | Create parcel |
| `PUT` | `/{id}` | ADMIN | Update parcel |
| `DELETE` | `/{id}` | ADMIN | Delete parcel |

### Shipment API `/api/shipments`

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/getAll` | Authenticated | List all shipments |
| `GET` | `/{id}` | Authenticated | Get shipment by ID |
| `GET` | `/tracking/{trackingNumber}` | Authenticated | Find by tracking number |
| `POST` | `/addShipment/{parcelId}` | ADMIN | Create shipment for parcel |
| `PUT` | `/{id}/location` | ADMIN | Update current location (query param `currentLocation`) |
| `PUT` | `/{id}` | ADMIN | Update shipment details |
| `DELETE` | `/{id}` | ADMIN | Delete shipment |

### Tracking Update API

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/api/tracking-updates` | Authenticated | All tracking updates |
| `GET` | `/api/tracking-updates/{id}` | Authenticated | Get by ID |
| `GET` | `/api/shipments/{shipmentId}/tracking-updates` | Authenticated | All updates for a shipment |
| `POST` | `/api/shipments/{shipmentId}/tracking-updates` | ADMIN | Add tracking update |
| `PUT` | `/api/tracking-updates/{id}` | ADMIN | Edit tracking update |
| `DELETE` | `/api/tracking-updates/{id}` | ADMIN | Delete update |

### Sample Request Bodies

**Create Customer**
```json
{
  "customerName": "Rahul Sharma",
  "email": "rahul@example.com",
  "phone": "9876543210",
  "address": "12 MG Road, Bangalore"
}
```

**Create Parcel**
```json
{
  "customerId": 1,
  "weight": 2.5,
  "sourceAddress": "New Delhi Pickup Hub",
  "destinationAddress": "Mumbai Delivery Center",
  "bookingDate": "2026-06-18"
}
```

**Add Tracking Update**
```json
{
  "location": "Mumbai Sorting Facility",
  "deliveryStatus": "In Transit",
  "remarks": "Package arrived at sorting hub"
}
```

**Delivery Status values:** `In Transit` · `Out for Delivery` · `Delivered` · `Delayed` · `Pending`

---

## Error Handling

### UI Requests (Thymeleaf pages)

`UiExceptionHandler` (`@ControllerAdvice`) catches all exceptions from `@Controller` beans and renders HTML error pages — no raw JSON is ever shown in the browser.

| Exception | Template rendered |
|-----------|------------------|
| `ResourceNotFoundException` | `error/not-found.html` |
| `IllegalStateException` | `error/bad-request.html` |
| `AccessDeniedException` | `error/access-denied.html` |
| Any other `Exception` | `error/general.html` |

Forms also display inline error boxes when service calls fail (e.g. customer not found when booking a parcel).

### REST API Requests

`GlobalExceptionHandler` (`@RestControllerAdvice(annotations=RestController.class)`) handles exceptions from REST controllers and returns structured JSON:

```json
{
  "timestamp": "2026-06-18T18:00:00.123",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request data",
  "fieldErrors": {
    "email": "must be a well-formed email address",
    "weight": "Weight must be greater than 0"
  }
}
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/wip/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java          # Spring Security, URL auth, BCrypt
│   │   │   ├── OpenApiConfig.java           # Swagger/OpenAPI 3 setup
│   │   │   ├── CorsConfig.java              # CORS for REST API
│   │   │   └── DatabaseSeederConfig.java    # Startup data seeder
│   │   ├── controller/
│   │   │   ├── HomeController.java          # /, /dashboard, /home
│   │   │   ├── AuthController.java          # /register
│   │   │   ├── CustomerUiController.java    # /customers/**
│   │   │   ├── ParcelUiController.java      # /parcels/**
│   │   │   ├── ShipmentUiController.java    # /shipments/**
│   │   │   ├── TrackingUpdateUiController.java # /tracking/**
│   │   │   ├── ProfileController.java       # /profile, /profile/edit
│   │   │   ├── SearchController.java        # /search
│   │   │   ├── ErrorPageController.java     # /error/**
│   │   │   ├── CustomerController.java      # REST /api/customers
│   │   │   ├── ParcelController.java        # REST /api/parcels
│   │   │   ├── ShipmentController.java      # REST /api/shipments
│   │   │   └── TrackingUpdateController.java # REST /api/tracking-updates
│   │   ├── service/
│   │   │   ├── AuthService.java / AuthServiceImpl.java
│   │   │   ├── CustomerService.java / CustomerServiceImpl.java
│   │   │   ├── ParcelService.java / ParcelServiceImpl.java
│   │   │   ├── ShipmentService.java / ShipmentServiceImpl.java
│   │   │   └── TrackingUpdateService.java / TrackingUpdateServiceImpl.java
│   │   ├── repository/
│   │   │   ├── AppUserRepository.java
│   │   │   ├── CustomerRepository.java
│   │   │   ├── ParcelRepository.java
│   │   │   ├── ShipmentRepository.java
│   │   │   └── TrackingUpdateRepository.java
│   │   ├── entity/
│   │   │   ├── AppUser.java
│   │   │   ├── Customer.java
│   │   │   ├── Parcel.java
│   │   │   ├── Shipment.java
│   │   │   └── TrackingUpdate.java
│   │   ├── dto/
│   │   │   ├── RegisterDto.java
│   │   │   ├── CustomerDto.java
│   │   │   ├── ParcelDto.java
│   │   │   ├── ShipmentDto.java
│   │   │   └── TrackingUpdateDto.java
│   │   ├── exception/
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── GlobalExceptionHandler.java  # REST JSON errors
│   │   │   └── UiExceptionHandler.java      # Thymeleaf HTML error pages
│   │   ├── security/
│   │   │   ├── CustomUserDetails.java
│   │   │   ├── CustomUserDetailsService.java
│   │   │   └── CurrentUserUtil.java
│   │   └── util/
│   │       └── TrackingNumberGenerator.java
│   └── resources/
│       ├── application.properties
│       ├── static/
│       │   ├── css/style.css                # Full design system (~950 lines)
│       │   └── js/app.js                    # GSAP animations, toasts, nav
│       └── templates/
│           ├── fragments/
│           │   ├── navbar.html              # Sticky purple navbar
│           │   ├── footer.html
│           │   └── scripts.html            # Fonts, FA icons, GSAP, app.js
│           ├── home.html                    # Landing page
│           ├── login.html
│           ├── register.html
│           ├── index.html                   # Dashboard
│           ├── customer/                    # list, form, details, delete
│           ├── parcel/                      # list, form, details, by-customer
│           ├── shipment/                    # list, form, details, start
│           ├── tracking/                    # list, form, details
│           ├── profile/                     # view, edit
│           ├── search/results.html
│           └── error/                       # 404, access-denied, not-found, bad-request, general
└── test/
    └── java/com/wip/service/                # Unit tests for all service impls
```

---

## Monitoring

| Endpoint | Description |
|----------|-------------|
| `http://localhost:8080/actuator/health` | Application health status |
| `http://localhost:8080/actuator/info` | App info |
| `http://localhost:8080/swagger-ui.html` | Interactive API documentation |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON spec |

---

## Author

| | |
|--|--|
| **Name** | Rohith Varma K |
| **Email** | rohith.varmak@wipro.com |
| **Name** | Dharshan K S |
| **Email** | dharshan.ks@wipro.com |
| **Project** | Courier Tracking System |
| **Type** | Capstone Project |