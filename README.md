# 📦 Subscription Management Platform

A production-grade subscription management system built with **Spring Boot** and **MongoDB**.

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green)
![Razorpay](https://img.shields.io/badge/Payment-Razorpay-blue)

## 🎯 Project Overview

This platform enables businesses to manage subscription-based services with features like:
- User registration and authentication
- Subscription plan management
- Payment processing via Razorpay
- Email notifications
- Analytics and reporting

## 📁 Architecture

```
src/main/java/com/example/subscription_platform/
├── controller/      # REST API endpoints
├── service/         # Business logic layer
├── repository/      # Data access layer (MongoDB)
├── model/           # Entity classes
├── dto/             # Data Transfer Objects
│   ├── auth/        # Authentication DTOs
│   ├── payment/     # Payment DTOs
│   ├── plan/        # Plan DTOs
│   ├── subscription/# Subscription DTOs
│   └── user/        # User DTOs
├── config/          # Configuration classes
├── security/        # JWT & security config
├── exception/       # Global exception handling
└── util/            # Utility classes
```

## ✅ Features Implemented

### Core Requirements

| Feature | Status | Description |
|---------|--------|-------------|
| User Registration | ✅ | Email-based registration with validation |
| User Login | ✅ | JWT token authentication |
| Role-Based Access | ✅ | ADMIN/USER roles with endpoint restrictions |
| CRUD Operations | ✅ | Full CRUD for Plans, Subscriptions, Users |
| Pagination | ✅ | All list endpoints support pagination |
| Sorting | ✅ | Configurable sort order |
| Filtering | ✅ | Filter by status, date range, plan |

### Advanced Features

| Feature | Status | Description |
|---------|--------|-------------|
| Complex Queries | ✅ | MongoDB aggregations and filters |
| Caching | ✅ | @Cacheable on frequently accessed data |
| File Upload | ✅ | Document upload with S3-style storage |
| Email Notification | ✅ | Welcome, confirmation, reminder emails |
| API Rate Limiting | ✅ | Bucket4j rate limiter (100 req/min) |
| Analytics APIs | ✅ | Revenue, subscription, user stats |
| Global Exception Handling | ✅ | Consistent error responses |
| Input Validation | ✅ | Jakarta Validation annotations |
| Swagger Documentation | ✅ | OpenAPI 3.0 spec |

### Integration

| Integration | Status | Description |
|-------------|--------|-------------|
| Razorpay Payment Gateway | ✅ | Order creation & payment verification |
| Email SMTP | ✅ | JavaMailSender with Thymeleaf templates |

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| Backend | Spring Boot 3.2.x |
| Database | MongoDB (Atlas) |
| Authentication | JWT (JSON Web Tokens) |
| API Style | REST |
| Validation | Jakarta Validation |
| Documentation | Swagger/OpenAPI |
| Payment | Razorpay |
| Email | Spring Mail + Thymeleaf |
| Caching | Spring Cache |
| Rate Limiting | Bucket4j |

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MongoDB (local or Atlas)
- Razorpay account (test mode)

### Configuration

1. Clone the repository:
```bash
git clone https://github.com/AbhiGandhi02/Spring_Subscription_Platform.git
cd subscription-platform
```

2. Configure `application.yml`:
```yaml
spring:
  data:
    mongodb:
      uri: your-mongodb-uri

razorpay:
  key-id: your-razorpay-key
  key-secret: your-razorpay-secret

jwt:
  secret: your-jwt-secret
```

3. Run the application:
```bash
mvn spring-boot:run
```

4. Access:
   - API: `http://localhost:8080`
   - Swagger: `http://localhost:8080/swagger-ui.html`
   - Frontend: Open `frontend/index.html`

## 📖 API Documentation

### Authentication APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Subscription Plan APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/plans` | Get all active plans |
| POST | `/api/plans` | Create new plan (Admin) |
| PUT | `/api/plans/{id}` | Update plan (Admin) |
| DELETE | `/api/plans/{id}` | Delete plan (Admin) |

### Subscription APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/subscriptions` | Subscribe to a plan |
| GET | `/api/subscriptions/active` | Get active subscription |
| GET | `/api/subscriptions/history` | Get subscription history |
| DELETE | `/api/subscriptions/{id}` | Cancel subscription |

### Payment APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments/create` | Create payment order |
| POST | `/api/payments/verify` | Verify payment |
| GET | `/api/payments/history` | Get payment history |

### Admin APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users (Admin) |
| GET | `/api/subscriptions/admin/stats` | Subscription stats |
| GET | `/api/payments/admin/revenue-stats` | Revenue stats |

## 🔐 Security

- **JWT Authentication**: All protected endpoints require Bearer token
- **Role-Based Access Control**: 
  - `USER`: Can manage own subscriptions
  - `ADMIN`: Full access to all resources
- **Password Encryption**: BCrypt hashing
- **Rate Limiting**: 100 requests/minute per IP

## 📧 Email Templates

Located in `src/main/resources/templates/`:
- `welcome-email.html` - New user welcome
- `subscription-confirmation.html` - Subscription activated
- `expiry-reminder.html` - Subscription expiring soon
- `payment-receipt.html` - Payment confirmation

## 📊 Analytics

The platform provides the following analytics:
- **Subscription Stats**: Active, pending, total counts
- **Revenue Stats**: Total revenue, monthly revenue
- **User Stats**: Total users, new registrations

## 🧪 Testing

### Test Credentials
```
Email: test@example.com
Password: password123
```

### Razorpay Test UPI
```
success@razorpay  (Payment succeeds)
failure@razorpay  (Payment fails)
```

## 👥 Team Members

- **Abhi Gandhi** - Full Stack Developer

## 📝 License

This project is for educational purposes as part of the Backend Engineering with Spring Boot course.

---

**Made with ❤️ using Spring Boot**
