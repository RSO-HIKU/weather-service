# Weather Service - Technical Documentation

## Overview

The **Weather Service** is a lightweight microservice responsible for providing weather forecast data and current weather conditions within the HIKU hiking application. It integrates with external weather APIs to retrieve real-time and forecast data for geographic locations, enabling users to check weather conditions before hiking activities. The service does not manage persistent data but acts as a weather data aggregator and transformer, exposing clean REST endpoints for weather queries.

## Table of Contents

1. [Architecture](#architecture)
2. [Technology Stack](#technology-stack)
3. [API Endpoints](#api-endpoints)
4. [Authentication & Authorization](#authentication--authorization)
5. [Configuration](#configuration)
6. [Deployment](#deployment)
7. [Local Development](#local-development)
8. [Error Handling](#error-handling)
9. [Troubleshooting](#troubleshooting)

---

## Architecture

### Key Components

- **REST Controllers**: Handle HTTP requests for weather data (WeatherResource)
- **Weather Service**: Business logic for fetching and transforming weather data from external APIs
- **CORS Filter**: Enables cross-origin requests for frontend integration
- **Health Controller**: Provides service health check endpoint
- **External API Integration**: Communicates with third-party weather data providers

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Runtime** | Java (Eclipse Temurin) | 17+ |
| **Build Tool** | Maven | 3.9 |
| **Framework** | KumuluzEE | 4.1.0 |
| **Database** | PostgreSQL | 14+ (optional, for caching) |
| **Authentication** | MicroProfile JWT | 2.1 |
| **HTTP Client** | JAX-RS | (via KumuluzEE) |
| **Containerization** | Docker | - |
| **Orchestration** | Kubernetes (via Helm) | - |



## Configuration

### Application Configuration

Configuration file: `src/main/resources/config.yaml`

### JPA Configuration

Configuration file: `src/main/resources/META-INF/persistence.xml`

## Authentication & Authorization

### JWT-Based Authentication

The Peaks-Hikes Service uses **MicroProfile JWT** with Keycloak as the identity provider. All REST endpoints require `@RolesAllowed("user")` for authenticated access.

## Deployment

#### Database Migrator Image

**Dockerfile**: `Dockerfile.migrator`

Runs Flyway migrations as a Kubernetes Job.

---

### Kubernetes (Helm)

#### Chart Structure

```
helm/
├── Chart.yaml              # Chart metadata
├── values-dev.yaml         # Development values
└── templates/
    ├── _helpers.tpl        # Template helpers
    ├── deployment.yaml     # Main application deployment
    ├── service-clusterip.yaml  # Internal service
    ├── service-nodeport.yaml   # External service (dev)
    ├── migrate-job.yaml    # Database migration job
    ├── secret.yaml         # Database credentials
    └── secretsproviderclass.yaml  # Azure Key Vault integration
```

---

### CI/CD Pipelines

#### Test Environment Pipeline

**File**: `.github/workflows/test-build-deploy.yaml`

**Triggers**:
- Push to `test` branch

**Steps**:
1. Checkout code
2. Build Docker images (app + migrator)
3. Push to Azure Container Registry (ACR)
4. Deploy to AKS test environment using ArgoCD

---

#### Production Promotion Pipeline

**File**: `.github/workflows/prod-promote.yaml`

**Triggers**:
- Manual workflow dispatch with image tag selection

**Steps**:
1. Pull images from test ACR
2. Retag images for production
3. Push to production ACR
4. Deploy to AKS production environment

Secrets used in the GitHub Actions workflows are saved as secrets in our GitHub Organization. Secrets used for deployment on the Azure cluster are provided by our Azure Key Vault.

---

## Local Development

Building images and deployment for local development is handled by Skaffold. By running the command **skaffold dev** in the root folder of the repository in a terminal window will make Skaffold automatically build and deploy the service to your local Minikube cluster. Skaffold watches your local files and when you save a change, Skaffold automatically applies it.  

### Prerequisites

#### Required Tools & Services
- **Java 17+**
- **Maven 3.9+**
- **PostgreSQL 14+**
- **Docker Desktop**
- **Keycloak**
- **RabbitMQ**
- **Minikube**
- **Skaffold**

#### Other requirements
- Docker Desktop is running,
- Minikube cluster is running on Docker Desktop,
- The database is deployed on your local cluster,
- The Traefik ingress controller is deployed on your local cluster,
- Keycloak is deployed on your local cluster.

### Steps performed by Skaffold
- Builds docker image for microservice,
- Builds docker image for database migrations,
- Deploys both images,
- Portforwards NodePort to the default port setting.


## Error Handling

### Common HTTP Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| `200 OK` | Request successful | GET weather data |
| `204 No Content` | Success, no response body | - |
| `400 Bad Request` | Invalid request data | Missing latitude/longitude |
| `401 Unauthorized` | Missing or invalid JWT | No Authorization header |
| `403 Forbidden` | Insufficient permissions | - |
| `404 Not Found` | Resource not found | Invalid location |
| `500 Internal Server Error` | Server error | External API unavailable |
| `503 Service Unavailable` | Service unhealthy | Health check failed |

### Exception Handling

The service uses JAX-RS exception handling:

- **Validation errors**: Return `400 Bad Request` with error description
- **Missing parameters**: Return `400 Bad Request`
- **External API failures**: Return `503 Service Unavailable` or `500 Internal Server Error`
- **Authentication errors**: Return `401 Unauthorized`

## Contact

For questions or issues, contact the development team.


**Last Updated**: January 11, 2026  
**Version**: 0.1.0
