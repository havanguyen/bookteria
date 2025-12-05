# Bookteria - Microservices E-commerce Platform

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
[![Documentation](https://img.shields.io/badge/Wiki-DeepWiki-blue)](https://deepwiki.com/havanguyen/bookteria)

## Introduction

**Bookteria** is a high-performance, distributed e-commerce platform designed for scalability and resilience. Built on top of the Spring Boot ecosystem, it employs a microservices architecture to handle complex business domains including inventory management, order processing, and secure payments.

## 🚀 Quick Links

- **Frontend Application**: [bookteria.click](https://bookteria.click)
- **API Gateway**: [api.bookteria.click](https://api.bookteria.click)
- **Project Wiki & Documentation**: [deepwiki.com/havanguyen/bookteria](https://deepwiki.com/havanguyen/bookteria)
- - **Monitor by dozzle **: [dozzle]((http://44.228.171.35:8888/))

## 🏗 Architecture

The Bookteria platform follows a domain-driven design (DDD) approach, decomposing the application into self-contained services.

### High-Level Design

```mermaid
graph TD
    User((User)) -->|HTTPS| Frontend[Frontend App\nbookteria.click]
    User -->|HTTPS| Gateway[API Gateway\napi.bookteria.click]
    
    subgraph "Edge Layer"
        Gateway
    end
    
    subgraph "Core Services"
        Gateway --> Identity[Identity Service\n(Auth & User Mgmt)]
        Gateway --> Product[Product Service\n(Catalog & Search)]
        Gateway --> Order[Order Service\n(Order Lifecycle)]
        Gateway --> Cart[Cart Service\n(Shopping Cart)]
        Gateway --> Profile[Profile Service\n(User Preferences)]
    end
    
    subgraph "Support Services"
        Order --> Inventory[Inventory Service\n(Stock Mgmt)]
        Order --> Payment[Payment Service\n(Transactons)]
        Payment --> Notification[Notification Service\n(Email/SMS)]
        Gateway --> File[File Service\n(Media Storage)]
    end
    
    subgraph "Data & Infra"
        Identity -.-> Keycloak[Keycloak/OAuth2]
        Product -.-> Elastic[Elasticsearch]
        Services -.-> Kafka[Kafka Event Bus]
        Services -.-> Zipkin[Zipkin Tracing]
    end
```

### Key Architectural Patterns
- **API Gateway Pattern**: The `api-gateway` acts as the single entry point, handling routing, rate limiting, and security.
- **Service Discovery**: All services register with `discovery-server` (Eureka) for dynamic load balancing.
- **Event-Driven Architecture**: Asynchronous communication via **Kafka** and **RabbitMQ** ensures loose coupling between services (e.g., Order Placed -> Inventory Reserved -> Email Sent).
- **Centralized Configuration**: Configuration is managed centrally (implied via Spring Cloud Config or env vars).
- **Distributed Tracing**: Integrated with **Zipkin** for tracking requests across microservices.

## 🛠 Technology Stack

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Cloud Native**: Spring Cloud (Gateway, Eureka, OpenFeign, Resilience4j)
- **Security**: OAuth2, OIDC, JWT (Identity Service)

### Data & Messaging
- **Relational DBs**: MySQL, PostgreSQL (Service specific)
- **NoSQL**: MongoDB (Product/Catalog), Neo4j (Recommendations/Social), Redis (Caching)
- **Message Brokers**: Apache Kafka, RabbitMQ
- **Search Engine**: Elasticsearch 8.x

### DevOps & Infrastructure
- **Containerization**: Docker, Docker Compose
- **CI/CD**: GitHub Actions
- **Monitoring**: Prometheus, Grafana

## 📦 Services Overview

| Service | Port | Key Responsibilities |
|---------|------|----------------------|
| **Discovery Server** | 8761 | Service registry (Eureka). |
| **API Gateway** | 9000 | Routing, SSL termination, Auth headers. |
| **Identity Service** | 8080 | User registration, login, token generation. |
| **Profile Service** | 8081 | User profile data, address management. |
| **Product Service** | 8082 | Book catalog, categories, ratings. |
| **Search Service** | 8083 | Full-text search, filtering, aggregation. |
| **Inventory Service** | 8084 | Real-time stock tracking, reservations. |
| **Cart Service** | 8085 | Temporary cart storage (Redis backed). |
| **Order Service** | 8086 | Order state machine, saga orchestration. |
| **Payment Service** | 8087 | Payment gateway integration. |
| **Notification Service** | 8088 | Async email/push notifications. |
| **File Service** | 8089 | S3/Local file storage for book covers. |

## 🏁 Getting Started

### Prerequisites
- **Java 17** SDK
- **Docker Desktop** (or Docker Engine + Compose)
- **Maven** 3.8+

### Local Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/havanguyen/bookteria.git
   cd bookteria
   ```

2. **Environment Configuration**
   The project uses a `.env` file for managing secrets and ports.
   ```bash
   # Ensure .env exists in the root
   cat .env
   ```

3. **Start with Docker Compose**
   This will spin up all microservices and infrastructure dependencies (Databases, Brokers, etc.).
   ```bash
   docker-compose up -d
   ```

4. **Access the Application**
   - **Frontend**: [http://localhost:3000](http://localhost:3000) (if running locally) or [bookteria.click](https://bookteria.click)
   - **API Gateway**: [http://localhost:9000](http://localhost:9000)
   - **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)

## 📚 API Documentation

The API is documented using OpenAPI/Swagger. You can access the aggregated documentation via the Gateway or individual services.

- **Aggregated Swagger UI**: [http://localhost:9000/webjars/swagger-ui/index.html](http://localhost:9000/webjars/swagger-ui/index.html)
- **Public API Endpoint**: `https://api.bookteria.click`

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
