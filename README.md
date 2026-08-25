# Package Delivery Tracking

A Java Spring Cloud microservices demo for package delivery tracking. It demonstrates service discovery (Eureka), an API Gateway, centralized config, asynchronous events with Kafka, polyglot persistence (Postgres + MongoDB), and common production concerns (resilience, tracing, observability).

## Project structure

- `discovery-server` — Eureka service registry (port 8761)
- `config-server` — Spring Cloud Config server (port 8888)
- `api-gateway` — Spring Cloud Gateway (port 8080)
- `user-service` — user and agent management (port 8081)
- `shipment-service` — create/manage shipments; produces Kafka events (port 8082)
- `tracking-service` — consumes Kafka events and stores tracking timeline (port 8083)
- `delivery-service` — assigns agents and publishes delivery events (port 8084)
- `notification-service` — consumes business events and stores notification history (port 8085)
- `docker-compose.yml` — local infra: PostgreSQL, MongoDB, Kafka, pgAdmin, Kafka UI
- `docs/` — architecture, technical stack, roadmap, deployment, contribution guide

## Quick local setup

1. Start infrastructure (Docker required)

```bash
docker compose up -d
```

Wait until containers are healthy (Kafka may take ~15-20s).

2. Start core Spring services in order (open a terminal per service):

macOS / Linux

```bash
./mvnw -pl discovery-server spring-boot:run
./mvnw -pl config-server spring-boot:run
./mvnw -pl api-gateway spring-boot:run
./mvnw -pl user-service spring-boot:run
./mvnw -pl shipment-service spring-boot:run
./mvnw -pl tracking-service spring-boot:run
```

Windows (PowerShell)

```powershell
mvnw.cmd -pl discovery-server spring-boot:run
mvnw.cmd -pl config-server spring-boot:run
mvnw.cmd -pl api-gateway spring-boot:run
mvnw.cmd -pl user-service spring-boot:run
mvnw.cmd -pl shipment-service spring-boot:run
mvnw.cmd -pl tracking-service spring-boot:run
mvnw.cmd -pl delivery-service spring-boot:run
mvnw.cmd -pl notification-service spring-boot:run
```

3. Verify services

- Eureka: http://localhost:8761
- API Gateway: http://localhost:8080
- Kafka UI: http://localhost:8090
- pgAdmin: http://localhost:5050

## pgAdmin: add PostgreSQL servers

- Login: `admin@admin.com` / `admin`
- Add server `postgres-users` → Host: `postgres-users`, Port: `5432`, User: `postgres`, Password: `postgres`
- Add server `postgres-shipments` → Host: `postgres-shipments`, Port: `5432`, User: `postgres`, Password: `postgres`

> Note: The Postgres containers use named volumes (`postgres-users-data`, `postgres-shipments-data`) — database files persist. We also added `pgadmin-data` volume so saved servers persist across restarts.

The delivery and notification databases are available at host ports `5434` and `5435` respectively. Their container-side PostgreSQL port is `5432`.

## MongoDB access

The tracking data is stored in MongoDB. Use the following connection string (this is for tools and drivers, not a browser URL):

`mongodb://mongo:mongo@localhost:27017/tracking_db?authSource=admin`

Ways to use it:

1. **MongoDB Compass (GUI)** — paste the exact string into the connection dialog and click Connect to browse `tracking_db` and its collections.
2. **mongosh (CLI)** — run:

```bash
mongosh "mongodb://mongo:mongo@localhost:27017/tracking_db?authSource=admin"
```

3. **From inside Docker** — if you exec into the `mongo-tracking` container, `localhost:27017` is correct from inside the container. From your host, `localhost:27017` works only if the port is mapped in `docker-compose.yml`.
4. **IntelliJ Database tool** — View → Tool Windows → Database → + → Data Source → MongoDB, paste the same connection string.

Why MongoDB is used:

- The tracking data is an event-driven timeline per shipment; MongoDB's document model maps naturally to storing a shipment document with an embedded array of events.
- It provides flexible schema for evolving event structures and fast reads for aggregated history queries used by the `tracking-service`.

See `docs/05-contribution-guide.md` for more details on connecting tools to the running `mongo-tracking` container.

## Development tips

- HTTP request collections for manual API testing are at the root of each module:
  - `discovery-server/requests.http`
  - `config-server/requests.http`
  - `api-gateway/requests.http`
  - `user-service/requests.http`
  - `shipment-service/requests.http`
  - `tracking-service/requests.http`
  - `delivery-service/requests.http`
  - `notification-service/requests.http`

- Keep tooling and test assets out of `src/main/java`. Module root or `src/test/resources` is preferred.

## Build & tests

- Build all modules and run tests:

```bash
./mvnw clean package
```

- Build without tests:

```bash
./mvnw clean package -DskipTests
```

- Run the focused delivery-service unit tests:

```powershell
./mvnw.cmd -pl delivery-service -Dtest=DeliveryAssignmentServiceTest test
```

## Where to read more

See the `docs/` folder for architecture, technical stack, roadmap, Kubernetes deployment and contribution guide.
