# Phase A Implementation Export

## Overview

Phase A adds `delivery-service` to the Package Delivery Tracking system. The business features now include agent roles and availability, delivery progression, status tracking, and notification history. A delivery assignment follows this flow:

1. Select an available agent, or validate the requested agent, through `user-service` using Feign.
2. Save the delivery assignment in the delivery database.
3. Update the shipment status through `shipment-service` using Feign.
4. Publish a `delivery.assigned` Kafka event.
5. Add the assignment to the shipment timeline in `tracking-service`.

The existing project versions remain unchanged: Java 17, Spring Boot 3.3.4, and Spring Cloud 2023.0.6.

## New Delivery Service

The new module is located at `delivery-service/` and runs on port `8084`.

### Main components

- `DeliveryServiceApplication.java` starts the service and enables Feign clients.
- `Delivery.java` stores `shipmentId`, `agentId`, `assignedAt`, and delivery status.
- `DeliveryStatus.java` defines `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, and `FAILED`.
- `DeliveryRepository.java` provides PostgreSQL persistence.
- `V1__create_deliveries_table.sql` creates the `deliveries` table with Flyway.
- `DeliveryController.java` exposes the delivery REST API.
- `DeliveryAssignmentService.java` coordinates validation, persistence, shipment updates, and event publishing.

### REST endpoints

```text
POST http://localhost:8084/deliveries
GET  http://localhost:8084/deliveries/{id}
```

Example assignment request:

```json
{
  "shipmentId": 1,
  "agentId": 2
}
```

The `user-service` model distinguishes `CUSTOMER` and `AGENT` users. When `agentId` is omitted, delivery-service selects the first available agent from `GET /users/agents/available`.

## Feign Communication

Two Feign boundaries were added:

- `UserServiceClient` calls `GET /users/{id}` to validate the assigned agent.
- `ShipmentServiceClient` calls `PATCH /shipments/{id}/status` to mark the shipment `OUT_FOR_DELIVERY`.

Both calls use Resilience4j circuit breakers. Their fallback methods log the original exception type, message, and stack trace before throwing a delivery-specific exception.

## Shipment Service Changes

`shipment-service` now supports:

```text
PATCH http://localhost:8082/shipments/{id}/status
```

Request body:

```json
{
  "status": "OUT_FOR_DELIVERY"
}
```

The endpoint validates the status, updates the shipment, and publishes the existing `shipment.status-changed` event.

The status-change event includes `customerId`, allowing downstream notification history to remain associated with the customer.

## Kafka Event

`delivery-service` publishes the following event to the `delivery.assigned` topic:

```json
{
  "shipmentId": 1,
  "agentId": 2,
  "assignedAt": "2026-08-20T12:00:00"
}
```

The producer uses `shipmentId` as the Kafka message key.

## Tracking Service Changes

`tracking-service` now has its own local `DeliveryAssignedEvent` class and a second Kafka listener for `delivery.assigned`.

When the event is consumed, tracking-service appends an `OUT_FOR_DELIVERY` event to the shipment's MongoDB tracking record:

```text
Delivery assigned to agent {agentId}
```

The listener uses a listener-level JSON default type so it can deserialize `delivery.assigned` independently from `shipment.created`.

Tracking also consumes `shipment.status-changed` and appends each status transition to the shipment timeline.

## Notification Service

`notification-service` runs on port `8085`, consumes all current business topics, persists simulated notifications in PostgreSQL, and exposes:

```text
GET http://localhost:8085/notifications/{userId}
```

Status-change and delivery-assignment events carry `customerId`, so customer notification history can be queried correctly.

## Delivery Progression

Delivery status can be updated with:

```text
PATCH http://localhost:8084/deliveries/{id}/status
```

| Delivery status | Shipment status |
|---|---|
| `ASSIGNED` | `OUT_FOR_DELIVERY` |
| `IN_PROGRESS` | `IN_TRANSIT` |
| `COMPLETED` | `DELIVERED` |
| `FAILED` | `FAILED` |

## Docker Infrastructure

A new PostgreSQL service was added to `docker-compose.yml`:

| Service | Database | Host port | Container port |
|---|---|---:|---:|
| `postgres-deliveries` | `deliveries_db` | `5434` | `5432` |

The database uses the named volume `postgres-deliveries-data`.

Start infrastructure before starting Spring Boot services:

```powershell
docker compose up -d
```

## HTTP Request Files

Runnable HTTP request collections now exist for every service module:

- `discovery-server/requests.http`
- `config-server/requests.http`
- `api-gateway/requests.http`
- `user-service/requests.http`
- `shipment-service/requests.http`
- `tracking-service/requests.http`
- `delivery-service/requests.http`

The notification service also has a request collection at `notification-service/requests.http`.

The collections include direct service calls and API Gateway discovery routes where applicable. The shipment request collection also contains the current user-edited customer ID and formatting.

## Suggested Verification Flow

Start services in this order after Docker infrastructure is running:

1. `discovery-server`
2. `config-server`
3. `api-gateway`
4. `user-service`
5. `shipment-service`
6. `tracking-service`
7. `delivery-service`
8. `notification-service`

Then execute:

1. Create a customer with `user-service`.
2. Create an agent user with `user-service`.
3. Create a shipment with `shipment-service`.
4. Assign the shipment through `delivery-service`.
5. Read the shipment tracking record from `tracking-service`.

Expected tracking history includes both the shipment creation and delivery assignment events.

## Validation Completed

The following checks passed during implementation:

- Shipment-service compilation.
- Delivery-service compilation.
- Tracking-service compilation.
- Full eight-module Maven reactor compilation.
- Docker Compose configuration validation.
- Editor diagnostics for changed Java files.
- Docker infrastructure startup, including `postgres-deliveries`.

The complete live end-to-end flow still requires all Spring Boot services to be running at the same time.
