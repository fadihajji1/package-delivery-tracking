# Package Delivery Tracking System — Overview

## 1. Context

This is a solo developer project designed to demonstrate a complete production-grade microservices architecture for a real-world use case: **package delivery tracking**.

The goal is educational and technical: cover the full skill set requested
(Spring Boot / Spring Cloud, API Gateway, Service Discovery, async messaging,
resilience, Docker, Kubernetes, distributed tracing) within a deliberately limited
business scope so it remains feasible for one person in a reasonable time frame
(6 to 10 weeks part-time).

## 2. Business use case

A customer places a package delivery order. The system:
1. Registers the customer and the package
2. Creates a shipment and assigns an initial status
3. Assigns or simulates a delivery agent
4. Advances the package through statuses (CREATED → PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED / FAILED)
5. Notifies the customer for each status change (simulated email/SMS or webhook)
6. Provides a public tracking endpoint to monitor package state in real time

This scenario naturally supports:
- service-specific data boundaries (polyglot persistence)
- event-driven asynchronous communication (status change as an event)
- resilience (notification service can be slow/unavailable)
- distributed tracing (a request crosses multiple services)

## 3. Scope — what is included and what is not

**Included (realistic MVP for one developer):**
- 5 business microservices plus 3 infrastructure services (Gateway, Discovery, Config)
- synchronous REST communication and asynchronous Kafka events
- one PostgreSQL database per transactional service, one MongoDB database for tracking/history
- Circuit Breaker on critical calls
- Kubernetes deployment (local with Minikube/Kind or cloud if available)
- distributed tracing with Zipkin
- integration tests using Testcontainers

**Deliberately excluded (out of scope to stay realistic for a solo developer):**
- complex multi-tenant authentication/authorization (only simple JWT)
- real payment processing
- real SMS/Email integration (mocked or via Mailhog/webhook.site)
- multi-region / multi-cluster Kubernetes
- complete CI/CD pipeline (optional bonus if time allows — simple GitHub Actions)

## 4. The 5 business microservices

| # | Service | Responsibility | Database |
|---|---------|----------------|----------|
| 1 | **user-service** | Manages customers and delivery agents (CRUD, auth) | PostgreSQL |
| 2 | **shipment-service** | Creates and manages shipments/packages | PostgreSQL |
| 3 | **tracking-service** | Builds shipment history and event timeline | MongoDB |
| 4 | **notification-service** | Sends notifications (simulated email/SMS) | PostgreSQL (notification logs) |
| 5 | **delivery-service** | Assigns delivery agents and simulates routing | PostgreSQL |

## 5. Infrastructure services

| Service | Role |
|---|---|
| **config-server** | Centralized configuration (Spring Cloud Config) |
| **discovery-server** | Service discovery (Netflix Eureka) |
| **api-gateway** | Single entry point (Spring Cloud Gateway) |
| **Kafka** | Asynchronous event bus (shipment status events) |
| **Zipkin** | Distributed tracing |
| **Resilience4j** | Circuit breaker / retry / bulkhead (built into services) |

## 6. Simplified logical diagram

```
                     ┌─────────────┐
                     │   Client    │
                     └──────┬──────┘
                            │ HTTPS
                     ┌──────▼──────┐
                     │ API Gateway │◄────────┐
                     └──────┬──────┘         │
                            │                │ registers
        ┌───────────────────┼──────────────┐ │
        │                   │              │ │
 ┌──────▼─────┐  ┌──────────▼───┐  ┌───────▼─┴────┐
 │user-service│  │shipment-svc  │  │delivery-svc  │
 └──────┬─────┘  └──────┬───────┘  └───────┬──────┘
        │               │                   │
        │        ┌──────▼──────┐            │
        │        │    Kafka    │◄───────────┘
        │        │  (topics:   │
        │        │  shipment.  │
        │        │  status)    │
        │        └──────┬──────┘
        │               │
        │        ┌──────▼───────────┐   ┌─────────────────┐
        │        │ tracking-service │   │notification-svc │
        │        └──────────────────┘   └─────────────────┘
        │                                       ▲
        └───────────────────────────────────────┘
                   (Kafka consumer)

   Discovery Server (Eureka): all services register here
   Config Server: all services fetch configuration from here
   Zipkin: traces all requests
```

## 7. Next steps

See the following files:
- `01-architecture.md` — architecture patterns and data flows
- `02-stack-technique.md` — chosen technologies and rationale
- `03-routing.md` — step-by-step implementation roadmap
- `04-deploiement-kubernetes.md` — manifests and deployment strategy
