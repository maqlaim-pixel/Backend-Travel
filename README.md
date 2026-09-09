# TravelVista — Java Spring Boot Backend

## Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **PostgreSQL 14+**

## Setup

### 1. Create PostgreSQL Database

```sql
CREATE DATABASE travel_website;
```

### 2. Configure Database

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/travel_website
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build & Run

```bash
cd backend-java

# Build
mvn clean install

# Run
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.

### 4. Auto-Seed Data

On first startup, the application automatically creates:
- **Roles**: super_admin, editor, contributor, sales
- **Admin user**: admin@travelvista.com / admin123
- **Editor user**: editor@travelvista.com / editor123
- **10 sample packages** (domestic + international)
- **8 testimonials**
- **6 FAQs**
- **11 site settings**

## API Endpoints

### Authentication
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/admin/login` | Public | Login and get JWT token |
| GET | `/api/admin/me` | Required | Get current user info |

### Packages
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/packages` | Public | List all packages |
| GET | `/api/packages?status=featured` | Public | Featured packages only |
| GET | `/api/packages/{id}` | Public | Get package by ID |
| GET | `/api/packages/slug/{slug}` | Public | Get package by slug |
| POST | `/api/packages` | Required | Create new package |
| PUT | `/api/packages/{id}` | Required | Update package |
| DELETE | `/api/packages/{id}` | Required | Delete package |

### Leads / Enquiries
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/leads/public/submit` | Public | Submit enquiry |
| GET | `/api/leads` | Required | List all leads |
| GET | `/api/leads?status=new` | Required | Filter by status |
| GET | `/api/leads/{id}` | Required | Get lead detail |
| PUT | `/api/leads/{id}/status` | Required | Update lead status |
| POST | `/api/leads/{id}/notes` | Required | Add note to lead |

### Dashboard
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/dashboard/stats` | Public | Dashboard statistics |
| GET | `/api/dashboard/recent-leads` | Required | Recent leads |

## JWT Authentication

1. Login at `POST /api/admin/login` with email + password
2. Receive a JWT token in response
3. Include in requests: `Authorization: Bearer <token>`
4. Token expires in 8 hours

## Frontend Connection

The Next.js frontend connects to this backend at `http://localhost:8080/api`.

Set the environment variable in `frontend/.env.local`:
```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA + Hibernate
- Spring Security + JWT (jjwt 0.12.5)
- PostgreSQL
- Lombok
- Maven
