# Roadmap — step-by-step implementation

Total estimated effort: **6 to 10 weeks** part-time (10-15h per week) for a solo developer.
Each phase is designed to finish with a **working, demonstrable milestone**, not a "big bang" at the end.

---

## Phase 0 — Preparation (2-3 days)

- [x] Create the Git repository (Maven multi-module monorepo)
- [ ] Directory structure:
```
package-delivery-tracking/
├── config-server/
├── discovery-server/
├── api-gateway/
├── user-service/
├── shipment-service/
├── delivery-service/
├── tracking-service/
├── notification-service/
├── docker-compose.yml          (local infra: postgres, mongo, kafka, zipkin)
├── k8s/                        (Kubernetes manifests)
└── docs/                       (these markdown files)
```
- [x] Write a `docker-compose.yml` for PostgreSQL, MongoDB, Kafka + KRaft, Kafka UI
- [x] Verify local infrastructure starts successfully (`docker compose up`)

**End of phase deliverable**: local infra is up and the repository structure is in place.

---

## Phase 1 — Spring Cloud foundation (week 1)

1. **config-server**
   - Create the Spring Boot module with `spring-cloud-config-server`
   - Store configs in a local folder (`native` profile) or a separate Git repo
   - Config files per service: `user-service.yml`, `shipment-service.yml`, etc.

2. **discovery-server**
   - Spring Boot module with `spring-cloud-starter-netflix-eureka-server`
   - Eureka dashboard available at `http://localhost:8761`

3. **api-gateway**
   - Spring Boot module with `spring-cloud-starter-gateway`
   - Configure routes to each service using `lb://SERVICE-NAME` via Eureka
   - Add a global logging filter for debugging

**End of phase deliverable**: the three infrastructure services run, register in Eureka, and the Gateway routes correctly (even to temporary stub services).

---

## Phase 2 — user-service (week 2)

- [x] JPA user entity with customer/agent roles and availability
- [x] CRUD and availability endpoints: `POST /users`, `GET /users/{id}`, `GET /users/agents/available`
- [ ] Simple authentication: `POST /auth/login` → issues a JWT (using `jjwt`)
- [x] Connect to PostgreSQL, run migrations with **Flyway**
- [x] Register with Eureka and fetch config from Config Server
- [ ] Unit tests for service layer + integration tests with Testcontainers PostgreSQL

**Deliverable**: user service is functional, tested, registered in Eureka, and reachable via the Gateway.

---

## Phase 3 — shipment-service (week 3)

- [x] `Shipment` entity with status enum: CREATED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED
- [x] Endpoint `POST /shipments` with Feign customer validation
- [x] Integrate **OpenFeign** + **Resilience4j** circuit breaker
- [x] Endpoint `PATCH /shipments/{id}/status` to change shipment status
- [x] Publish `shipment.created` and `shipment.status-changed` events
- [ ] Integration tests with Testcontainers (PostgreSQL + Kafka)

**Deliverable**: end-to-end shipment creation with resilient user-service validation and Kafka event publishing.

---

## Phase 4 — tracking-service (week 4)

- [x] Connect to MongoDB using Spring Data MongoDB
- [x] Consume `shipment.created`, `shipment.status-changed`, and `delivery.assigned`
- [x] Build a `TrackingRecord` document per shipment with event history array
- [x] Endpoint `GET /tracking/{shipmentId}` returns full shipment timeline
- [ ] Integration tests with Testcontainers (MongoDB + Kafka)

**Deliverable**: real-time shipment history tracking using Kafka events.

---

## Phase 5 — delivery-service (week 5)

- [x] `Delivery` entity (assigns agent to shipment)
- [x] Endpoint `POST /deliveries`: selects or validates an agent and publishes `delivery.assigned`
- [x] Manual progression endpoint updates shipment status via Feign
- [x] Circuit breaker on calls to shipment-service/user-service

**Deliverable**: complete scenario from shipment creation to delivery assignment to status progress and tracking update.

---

## Phase 6 — notification-service (weeks 5-6)

- [x] Kafka consumer for all current business topics
- [x] Simulate sending notifications through application logs
- [x] Persist notification history in PostgreSQL
- [x] Endpoint `GET /notifications/{userId}` to view notification history

**Deliverable**: customer notifications for each status change, with persistent history.

---

## Phase 7 — Observability (weeks 6-7)

- [ ] Add **Zipkin** + `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave` across services
- [ ] Verify trace propagation across a complete scenario (Gateway → shipment → Kafka → tracking)
- [ ] Expose `/actuator/health` and `/actuator/metrics` everywhere
- [ ] (Bonus) Add Prometheus + Grafana if time allows

**Deliverable**: full end-to-end trace visible in Zipkin for a multi-service request.

---

## Phase 8 — Containerization (week 7)

- [x] Add **Jib** plugin to each Maven module
- [x] Build all images: `mvn compile jib:dockerBuild`
- [ ] Verify each service runs correctly in its container
- [ ] Push images to Docker Hub or a local Kind registry

**Deliverable**: eight Docker images built and runnable (five business services + gateway + discovery + config-server).

---

## Phase 9 — Kubernetes deployment (week 8)

See `04-kubernetes-deployment.md` for full details. Summary:

- [ ] Install **Kind** or **Minikube**
- [ ] Write `Deployment` and `Service` manifests for each microservice
- [ ] Use `StatefulSet` or simple deployments for PostgreSQL/MongoDB/Kafka, or reuse Helm charts for infrastructure
- [ ] Use `ConfigMap` / `Secret` for sensitive values
- [ ] Expose the API Gateway via `Ingress` or `NodePort`
- [ ] Use liveness/readiness probes based on Actuator
- [ ] Organize with **Kustomize** (base + dev overlay)

**Deliverable**: the full system runs on a local Kubernetes cluster and is accessible via Ingress/NodePort.

---

## Phase 10 — full integration testing & final documentation (weeks 9-10)

- [ ] End-to-end test scenario using REST Assured or Postman/Newman:
      create customer → create shipment → assign delivery → track progress → verify notifications
- [ ] Final report (`docs/rapport-final.md`):
      - objectives and scope
      - chosen architecture with diagrams
      - technical decisions and rationale
      - issues encountered and solutions
      - screenshots (Eureka, Zipkin, Kafka UI, k9s)
      - improvement opportunities (CI/CD, Helm, autoscaling HPA, service mesh)

**Deliverable**: complete, tested, documented project ready to demo.

---

## Priority order if time is limited

If scope must be reduced, follow this priority from most important to least important:
1. Config Server + Discovery + Gateway (essential foundation)
2. user-service + shipment-service (core business + Feign + Circuit Breaker)
3. Kafka + tracking-service (demonstrates async flow)
4. Docker + basic Kubernetes deployment
5. delivery-service + notification-service (can be simplified if needed)
6. Zipkin (bonus but recommended; not too hard to add)
7. Full integration tests (build them continuously rather than only at the end)
