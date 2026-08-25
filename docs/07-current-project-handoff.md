# Current Project Handoff

## Completed Business Features

- User CRUD with `CUSTOMER` and `AGENT` roles.
- Available-agent listing and availability updates.
- Shipment creation with Feign customer validation and Resilience4j fallback logging.
- Shipment status updates with `shipment.status-changed` Kafka events.
- Tracking history for shipment creation, status changes, and delivery assignment.
- Delivery assignment with explicit agent validation or automatic available-agent selection.
- Delivery progression mapped to shipment progression.
- Notification service consuming `shipment.created`, `shipment.status-changed`, and `delivery.assigned`.
- Notification persistence and customer history endpoint.

## Service Ports

| Service | Port |
|---|---:|
| discovery-server | 8761 |
| config-server | 8888 |
| api-gateway | 8080 |
| user-service | 8081 |
| shipment-service | 8082 |
| tracking-service | 8083 |
| delivery-service | 8084 |
| notification-service | 8085 |

## Business Flow

```text
user-service
  -> shipment-service
  -> Kafka shipment.created
  -> tracking-service + notification-service

delivery-service
  -> user-service via Feign
  -> shipment-service via Feign
  -> Kafka delivery.assigned
  -> tracking-service + notification-service

shipment-service
  -> Kafka shipment.status-changed
  -> tracking-service + notification-service
```

## Startup

Start Docker infrastructure first:

```powershell
docker compose up -d
```

Then start services in this order:

1. `discovery-server`
2. `config-server`
3. `api-gateway`
4. `user-service`
5. `shipment-service`
6. `tracking-service`
7. `delivery-service`
8. `notification-service`

The Windows helper is `start-all.ps1`; it uses `mvnw.cmd` and launches all eight services as background jobs. A service cannot start if its port is already occupied.

## Verification

1. Create a customer and an available agent with `user-service/requests.http`.
2. Confirm `GET /users/agents/available` returns the agent.
3. Create a shipment with `shipment-service/requests.http`.
4. Assign a delivery with an explicit `agentId`, or omit it to auto-select an available agent.
5. Progress the delivery to `IN_PROGRESS` and `COMPLETED`.
6. Check `GET /tracking/{shipmentId}` for the complete timeline.
7. Check `GET /notifications/{customerId}` for customer notifications.

## Remaining Work

- Expand automated unit and integration tests, preferably with Testcontainers for PostgreSQL, MongoDB, and Kafka.
- Add Actuator health and metrics endpoints.
- Add Zipkin distributed tracing.
- Containerize services with Jib or Dockerfiles.
- Add Kubernetes manifests and Kustomize overlays.
- Add automated end-to-end tests and final report documentation.

## Automated Tests Added

The first focused unit test suite is available at:

`delivery-service/src/test/java/com/deliverytracking/deliveryservice/service/DeliveryAssignmentServiceTest.java`

It verifies:

- Explicit agent validation during assignment.
- Automatic selection of the first available agent.
- Failure when no agent is available.
- Mapping `COMPLETED` delivery status to `DELIVERED` shipment status.

Run it with:

```powershell
./mvnw.cmd -pl delivery-service -Dtest=DeliveryAssignmentServiceTest test
```

## Development Rules

- Keep event classes local to each consuming service.
- Use `ErrorHandlingDeserializer` with local JSON default types and disabled type headers.
- Keep the Spring Cloud BOM only in the root POM.
- Use host-mapped ports for host-run applications and container ports for Docker-to-Docker connections.
