# Bookteria - Enterprise Microservices E-commerce Platform

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](#)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](#)

**Bookteria** is a robust, cloud-native e-commerce application built with a microservices architecture. Designed for high availability and scalability, it leverages the power of Spring Boot, Spring Cloud, and modern containerization technologies to deliver a seamless online bookstore experience.

---

## 📑 Table of Contents
- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Services Ecosystem](#-services-ecosystem)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Monitoring](#-monitoring)
- [Project Structure](#-project-structure)
- [License](#-license)

---

## ✨ Features

- **User Authentication**: Secure signup and login with JWT and OAuth2 support.
- **Product Catalog**: Browse and search books with advanced filtering (powered by Elasticsearch).
- **Shopping Cart**: Real-time cart management using Redis.
- **Order Management**: Complete order lifecycle from placement to payment and shipping.
- **Inventory Tracking**: Accurate stock management with concurrency handling.
- **Secure Payments**: Integrated payment processing system.
- **Notifications**: Automated email and SMS blocks for order updates.
- **Admin Dashboard**: Comprehensive management of users, products, and orders.

---

## 🏗 Architecture

Bookteria follows a **Domain-Driven Design (DDD)** approach, isolating business logic into distinct, loosely coupled services.

### Key Patterns
- **API Gateway**: Single entry point using Spring Cloud Gateway.
- **Service Discovery**: Netflix Eureka for dynamic service registration.
- **Circuit Breaker**: Resilience4j for fault tolerance.
- **Event-Driven**: Asynchronous communication via Kafka and RabbitMQ.

---

## 🛠 Technology Stack

| Category | Technologies |
|----------|--------------|
| **Backend** | Java 17, Spring Boot 3, Spring Cloud |
| **Frontend** | Vue 3, TailwindCSS |
| **Databases** | MySQL, MongoDB, Redis, PostgreSQL |
| **Messaging** | Apache Kafka, RabbitMQ |
| **Search** | Elasticsearch 8.x |
| **DevOps** | Docker, Docker Compose, GitHub Actions |
| **Monitoring** | Spring Boot Actuator, Dozzle |

---

## 📦 Services Ecosystem

| Service Name | Port | Description |
|--------------|------|-------------|
| **Discovery Server** | 8761 | Service registry & health monitoring |
| **API Gateway** | 9000 | Centralized routing & security |
| **Identity Service** | 8080 | AuthN & AuthZ (OIDC/OAuth2) |
| **Product Service** | 8082 | Catalog management |
| **Search Service** | 8083 | Advanced search capabilities |
| **Inventory Service**| 8084 | Stock management |
| **Cart Service** | 8085 | Session-based shopping carts |
| **Order Service** | 8086 | Order processing saga |
| **Payment Service** | 8087 | Payment gateway handling |
| **Notification** | 8088 | Email/SMS alerts |
| **File Service** | 8089 | Media asset storage |

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+**
- **Docker & Docker Compose**
- **Maven 3.8+**
- **Node.js 18+** (for frontend)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/havanguyen/bookteria.git
   cd bookteria
   ```

2. **Configure Environment**
   Create a `.env` file in the root directory. You can copy the template if available, or ensure the following variables are set (especially `DOCKER_USERNAME` for image pulling):
   ```bash
   # Example .env content
   DOCKER_USERNAME=your_dockerhub_username
   # Add other secrets (DB passwords, API keys)
   ```

3. **Start Infrastructure & Services**
   Use Docker Compose to bring up the entire stack.
   ```bash
   docker-compose up -d
   ```
   *Note: This may take a few minutes to download images and start all containers.*

4. **Verify Deployment**
   - **Frontend**: [http://localhost:3000](http://localhost:3000)
   - **API Gateway**: [http://localhost:9000](http://localhost:9000)
   - **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)

---

## 📚 API Documentation

Centralized API documentation is available via Swagger UI through the API Gateway:

- **Swagger UI**: [http://localhost:9000/webjars/swagger-ui/index.html](http://localhost:9000/webjars/swagger-ui/index.html)
- **Live API**: `https://api.bookteria.click`

---

## 🔍 Monitoring

- **Container Logs (Dozzle)**: [http://localhost:8888](http://localhost:8888)
- **Health Checks**: Each service exposes `/actuator/health` and `/actuator/info`.

---

## 📂 Project Structure

```bash
bookteria/
├── .github/              # CI/CD Workflows
├── api-gateway/          # Spring Cloud Gateway
├── discovery-server/     # Eureka Server
├── identity-service/     # Auth Service
├── product-service/      # Product Catalog
├── ...                   # Other microservices
├── docker-compose.yml    # Orchestration
└── README.md             # Documentation
```

---

## 📄 License

This project is licensed under the **MIT License**.
