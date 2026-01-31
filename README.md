# SaaS Billing Platform

A production-grade SaaS Billing Platform backend built with Spring Boot, featuring subscription management, billing, secure APIs, analytics, and external integrations.

## 🚀 Features

- **Authentication & Authorization** - JWT-based auth with role-based access (ADMIN/USER)
- **Subscription Management** - Create, upgrade, downgrade, cancel subscriptions
- **Billing & Invoicing** - Automated invoice generation with tax calculations
- **Payment Processing** - Razorpay integration (sandbox/mock)
- **Usage Tracking** - Record and monitor resource consumption
- **File Storage** - Local/S3 file upload with validation
- **Analytics Dashboard** - Revenue, churn rate, plan popularity metrics
- **Rate Limiting** - Bucket4j-based API rate limiting
- **Caching** - Redis caching for plans and analytics

## 🛠 Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.2.2 |
| Database | PostgreSQL (H2 for dev) |
| ORM | JPA/Hibernate |
| Security | Spring Security + JWT |
| Cache | Redis |
| Rate Limiting | Bucket4j |
| Documentation | Springdoc OpenAPI |
| Build | Maven |

## 📦 Project Structure

```
src/main/java/com/project/saasbilling/
├── config/          # Configuration (Security, Redis, OpenAPI)
├── controller/      # REST Controllers
├── dto/             # Data Transfer Objects
├── exception/       # Custom exceptions & handlers
├── model/           # JPA Entities
├── repository/      # Spring Data JPA Repositories
├── security/        # JWT utilities & filters
├── service/         # Business logic
└── util/            # Utility classes
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (optional, H2 used by default)
- Redis (optional, for caching)

### Run with H2 (Development)
```bash
# Clone and navigate
cd saas-billing-platform

# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run
```

### Run with PostgreSQL (Production)
```bash
# Set environment variables
export DATABASE_URL=jdbc:postgresql://localhost:5432/saasbilling
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=yourpassword
export JWT_SECRET=your-256-bit-secret-key-here-make-it-long

mvn spring-boot:run
```

### 🐳 Run with Docker (Recommended)

```bash
# Start all services (PostgreSQL + Redis + App)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down

# With Redis Commander UI (development)
docker-compose --profile dev up -d
# Access Redis UI at http://localhost:8081
```

**Services Started:**
| Service | Port | Description |
|---------|------|-------------|
| App | 8080 | Spring Boot API |
| PostgreSQL | 5432 | Database |
| Redis | 6379 | Cache |
| Redis Commander | 8081 | Redis Web UI (dev only) |

## 📚 API Documentation

Once running, access Swagger UI at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

## 🔐 Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@saasbilling.com | Admin@123 |
| User | user@example.com | User@123 |

## 📋 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/refresh` | Refresh token |

### Plans (Public: GET, Admin: POST/PUT/DELETE)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/plans` | List active plans |
| GET | `/api/plans/{id}` | Get plan details |
| POST | `/api/plans` | Create plan (Admin) |
| PUT | `/api/plans/{id}` | Update plan (Admin) |

### Subscriptions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/subscriptions` | Create subscription |
| GET | `/api/subscriptions/my` | My subscriptions |
| POST | `/api/subscriptions/{id}/cancel` | Cancel subscription |
| POST | `/api/subscriptions/{id}/change-plan` | Change plan |

### Invoices & Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/invoices/my` | My invoices |
| POST | `/api/payments` | Process payment |
| GET | `/api/payments/my` | My payments |

### Analytics (Admin Only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analytics/dashboard` | Dashboard summary |
| GET | `/api/analytics/monthly-revenue` | Monthly revenue |
| GET | `/api/analytics/subscription-stats` | Subscription stats |

## ⚙️ Configuration

Key environment variables:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/saasbilling
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=secret

# JWT
JWT_SECRET=your-256-bit-secret
JWT_EXPIRATION=86400000

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Rate Limiting
RATE_LIMIT_CAPACITY=100
RATE_LIMIT_REFILL_TOKENS=100
RATE_LIMIT_REFILL_MINUTES=1
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## 📄 License

MIT License
