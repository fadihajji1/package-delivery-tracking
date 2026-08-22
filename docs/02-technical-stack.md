# Technical Stack — choices and rationale

## 1. Base language & framework

| Component | Choice | Rationale |
|---|---|---|
| Language | **Java 17 (LTS)** | Project baseline and Spring Boot 3.3 runtime |
| Framework | **Spring Boot 3.3.x** | Java microservices standard |
| Build | **Maven** (multi-module) | Single repo with one module per service = easier to manage solo than Gradle multi-repo |
| Cloud toolkit | **Spring Cloud 2023.0.x (Leyton)** | Compatible with Spring Boot 3.3, provides Gateway / Config / OpenFeign |

> Solo developer tip: use a **Maven multi-module monorepo** (one module per microservice)
> rather than eight separate repositories. It is simpler to build, version, and deploy alone.

## 2. Application infrastructure (Spring Cloud)

| Need | Technology | Alternative skipped | Why this choice |
|---|---|---|---|
| Service Discovery | **Netflix Eureka** (Spring Cloud Netflix) | Consul | Eureka integrates naturally with Spring Cloud and requires minimal extra configuration |
| API Gateway | **Spring Cloud Gateway** | Kong, Zuul | 100% Java/Spring, reactive (WebFlux), route configuration in YAML |
| Centralized config | **Spring Cloud Config Server** | Kubernetes ConfigMaps alone | Config Server supports dynamic refresh and remains useful outside Kubernetes |
| Inter-service REST calls | **OpenFeign** | RestTemplate, raw WebClient | Declarative, integrates with Eureka and Resilience4j easily |
| Resilience | **Resilience4j** | Hystrix (deprecated) | Official Hystrix successor, lightweight, well integrated with Spring Boot 3 |

## 3. Asynchronous messaging

**Chosen technology: Apache Kafka** (instead of RabbitMQ)

Rationale:
- The use case is **event-driven by nature** (status event stream, replayable state) → Kafka is better suited than a simple queue
- It demonstrates an event log pattern / light event sourcing for tracking-service
- Kafka skills are marketable and more distinctive for a demo project than RabbitMQ

Tools:
- **Spring Kafka** for producers and consumers
- **Kafka UI** (Docker image `provectuslabs/kafka-ui`) to view topics locally — huge debug speedup for solo developers

## 4. Databases (polyglot persistence)

| Service | DB | Driver / ORM |
|---|---|---|
| user-service | PostgreSQL 16 | Spring Data JPA |
| shipment-service | PostgreSQL 16 | Spring Data JPA |
| delivery-service | PostgreSQL 16 | Spring Data JPA |
| notification-service | PostgreSQL 16 | Spring Data JPA |
| tracking-service | MongoDB 7 | Spring Data MongoDB |

> A single PostgreSQL instance with **one database per service** is sufficient for a solo demo project
> (no need for four separate PostgreSQL instances unless you want to illustrate full physical isolation).

### Why MongoDB for `tracking-service`

- The tracking domain is event-driven: each shipment has a timeline of status events. Storing a shipment document with an embedded events array maps naturally to MongoDB's document model.
- MongoDB's flexible schema lets us evolve event payloads without frequent relational migrations, and it supports efficient read patterns for returning a shipment's full history.
- See `docs/05-contribution-guide.md` for connection instructions and tooling examples (MongoDB Compass, `mongosh`, IntelliJ Database tool).

## 5. Containerization & orchestration

| Component | Choice |
|---|---|
| Containerization | **Docker** + **Jib** (Maven plugin `com.google.cloud.tools:jib-maven-plugin`) to build images without manual Dockerfiles (faster, better cache). Manual Dockerfile is an alternative if fine-grained control is needed |
| Registry | Docker Hub (free) or local registry (Kind/Minikube) |
| Orchestration | **Kubernetes** — local cluster via **Kind** or **Minikube** (free, no cloud billing required) |
| Manifest management | Raw YAML at first, then **Kustomize** for dev/prod overlays (avoids Helm complexity for a solo project) |

## 6. Observability

| Need | Technology |
|---|---|
| Distributed tracing | **Zipkin** + **Micrometer Tracing (Brave), planned** |
| Metrics | **Spring Boot Actuator** + **Micrometer, planned** |
| Logs | Logback JSON (`logstash-logback-encoder`), viewable with `kubectl logs` locally (no ELK stack required) |

## 7. Tests

| Type | Tool |
|---|---|
| Unit tests | JUnit 5 + Mockito |
| Integration tests | **Testcontainers** (real PostgreSQL, MongoDB, Kafka in containers during tests) |
| API contract tests (optional) | Spring Cloud Contract (bonus if time allows) |
| Lightweight end-to-end tests | REST Assured against the API Gateway |

## 8. Recommended developer tools

- **IDE**: IntelliJ IDEA (Community edition is enough)
- **Docker Desktop** or **Podman** to run Kafka/Postgres/Mongo locally via `docker-compose`
- **Postman** or `.http` files (IntelliJ HTTP Client) to test endpoints
- **k9s**: terminal UI for Kubernetes, strongly recommended for solo developers
- **Lens** (optional): GUI Kubernetes dashboard if preferred

## 9. Quick summary

```
Java 17 + Spring Boot 3.3.4 + Spring Cloud 2023.0.6
   ├── Eureka (discovery)
   ├── Spring Cloud Gateway (API gateway)
   ├── Spring Cloud Config (centralized config)
   ├── OpenFeign + Resilience4j (REST + resilience)
   ├── Kafka + Spring Kafka (async)
   ├── PostgreSQL (4 services) + MongoDB (tracking)
   ├── Docker (Jib) + Kubernetes (Kind/Minikube) + Kustomize
   ├── Zipkin + Micrometer Tracing
   └── Testcontainers + JUnit 5
```
