# Detailed Architecture

## 1. Architecture patterns in use

| Pattern | Where / Why |
|---|---|
| **API Gateway** | Single entry point (Spring Cloud Gateway), routing, rate limiting, centralized security |
| **Service Discovery** | Eureka — services are discovered dynamically, no hard-coded IPs |
| **Externalized Configuration** | Spring Cloud Config Server — centralized config per environment (dev/prod) |
| **Database per Service** | Each business service owns its own database — no direct sharing |
| **Saga (simplified)** | Delivery-service uses Feign for the immediate shipment update and Kafka for downstream facts |
| **Circuit Breaker** | Resilience4j on inter-service REST calls (e.g. delivery-service → user-service) |
| **Light CQRS** | tracking-service only reads and aggregates events (no heavy business logic) |
| **Event-Driven Communication** | Kafka for all asynchronous state-change events |
| **Synchronous REST (Feign)** | When immediate response is needed (e.g. shipment-service verifying client exists) |
| **Distributed Tracing** | Zipkin + Micrometer Tracing (Brave) — correlates cross-service requests |

## 2. When to use REST vs Kafka

**Simple decision rule for development:**

- If service A needs an **immediate response** to continue processing
  → use **synchronous REST via OpenFeign**
  (e.g. shipment-service must verify the client exists and calls user-service)

- If service A emits a **fact of record** that others should eventually know,
  without blocking execution → use a **Kafka event**
  (e.g. shipment-service publishes `ShipmentStatusChanged` and tracking-service
  and notification-service consume it independently)

### Kafka topics

| Topic | Producer | Consumers | Payload |
|---|---|---|---|
| `shipment.created` | shipment-service | tracking-service, notification-service | shipmentId, customerId, createdAt |
| `shipment.status-changed` | shipment-service | tracking-service, notification-service | shipmentId, customerId, oldStatus, newStatus, changedAt |
| `delivery.assigned` | delivery-service | notification-service, tracking-service | shipmentId, customerId, agentId, assignedAt |

## 3. Microservice details

### 3.1 user-service
- CRUD for customers and delivery agents
- Simple authentication with JWT generation and validation
- Exposes: `POST /users`, `GET /users/{id}`, `GET /users/agents/available`, `PATCH /users/{id}/availability`
- DB: PostgreSQL (`users` with role and availability fields)

### 3.2 shipment-service
- Creates a shipment linked to a customer
- Manages shipment status lifecycle (CREATED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED)
- Calls user-service via REST/Feign to validate customer existence (with Circuit Breaker)
- Publishes `shipment.created` and `shipment.status-changed` events
- DB: PostgreSQL (`shipments`)

### 3.3 delivery-service
- Assigns an available delivery agent to a shipment
- Simulates shipment progress (scheduled job or manual endpoint changes status)
- Publishes `delivery.assigned` and triggers status updates via Feign to shipment-service
- DB: PostgreSQL (`deliveries`)

### 3.4 tracking-service
- Consumes all shipment-related Kafka events
- Builds a queryable history/timeline (`GET /tracking/{shipmentId}`)
- Read-optimized, no heavy business logic — pure read model
- DB: MongoDB (`tracking_events` collection, one document per shipment with event array)

### 3.5 notification-service
- Consumes Kafka events
- Simulates notification sending (log + optional call to Mailhog or webhook.site)
- Persists notification history
- DB: PostgreSQL (`notifications`)

## 4. Resilience — where to apply Resilience4j

| Call | Pattern applied | Fallback behavior |
|---|---|---|
| shipment-service → user-service (Feign) | Circuit Breaker + Retry | Returns degraded client data from cache or explicit 503 error |
| delivery-service → shipment-service (Feign) | Circuit Breaker | Logs failure and retries later via Kafka event |
| notification-service → external service | Retry + Timeout | Marks notification as `PENDING_RETRY` |

## 5. Security (simplified but realistic)

- Authentication is centralized at the **API Gateway** (JWT validation)
- Internal microservices trust the gateway (no full re-validation,
  except extracting `X-User-Id` from headers)
- Internal communication runs on a private Kubernetes network (not exposed externally except via the Gateway)

## 6. Observability

- **Logs**: structured JSON format (Logback + `logstash-logback-encoder`), ready for later aggregation
- **Tracing**: Zipkin, every cross-service request carries a common `traceId`
- **Metrics**: Actuator + Micrometer (`/actuator/prometheus`, Prometheus optional)
- **Health checks**: Actuator `/actuator/health` on each service, used by Kubernetes liveness/readiness probes
