# Contribution Guide

This guide explains how to run the project locally and how a new developer can start contributing.

## 1. Prerequisites

- Java 17 or newer installed
- Docker Desktop running
- Git installed

## 2. Project structure

The repository is a Maven multi-module Spring Boot project. Key service modules are:

- `discovery-server`
- `config-server`
- `api-gateway`
- `user-service`
- `shipment-service`
- `tracking-service`

Each service has its own `application.yml` and is configured to register with Eureka.

## 3. Start infrastructure

From the repository root:

```bash
docker compose up -d
```

This starts the local infrastructure used by the services:

- `postgres-users` (PostgreSQL for `user-service`)
- `postgres-shipments` (PostgreSQL for `shipment-service`)
- `mongo-tracking` (MongoDB for `tracking-service`)
- `pgadmin` (optional database UI)
- `kafka`
- `kafka-ui`

Wait for the containers to be healthy. Kafka can take 15–20 seconds to initialize.

Verify with:

```bash
docker ps
```

## 4. Start Spring Boot services in order

The services must be started in this order:

1. `discovery-server` (port `8761`)
2. `config-server` (port `8888`)
3. `api-gateway` (port `8080`)
4. `user-service` (port `8081`)
5. `shipment-service` (port `8082`)
6. `tracking-service` (port `8083`)

Each module is started from the root with the Maven wrapper.

### macOS / Linux

```bash
./mvnw -pl discovery-server spring-boot:run
./mvnw -pl config-server spring-boot:run
./mvnw -pl api-gateway spring-boot:run
./mvnw -pl user-service spring-boot:run
./mvnw -pl shipment-service spring-boot:run
./mvnw -pl tracking-service spring-boot:run
```

### Windows

```powershell
mvnw.cmd -pl discovery-server spring-boot:run
mvnw.cmd -pl config-server spring-boot:run
mvnw.cmd -pl api-gateway spring-boot:run
mvnw.cmd -pl user-service spring-boot:run
mvnw.cmd -pl shipment-service spring-boot:run
mvnw.cmd -pl tracking-service spring-boot:run
```

> Tip: open a separate terminal for each service so you can monitor logs independently.

## 5. What each service does

- `discovery-server`: Eureka service registry for all modules.
- `config-server`: Centralized Spring Cloud Config server.
- `api-gateway`: API entry point and routing layer.
- `user-service`: user and agent management.
- `shipment-service`: shipment creation, status progression and Kafka event production.
- `tracking-service`: consumes Kafka events and exposes shipment tracking data.

## 6. Verify the system

- Eureka dashboard: `http://localhost:8761`
- API gateway: `http://localhost:8080`
- Kafka UI: `http://localhost:8090`
- pgAdmin: `http://localhost:5050`

### Connect to PostgreSQL with pgAdmin

1. Open `http://localhost:5050` in your browser.
2. Log in with:
   - Email: `admin@admin.com`
   - Password: `admin`
3. Click `Add New Server`.
4. In the `General` tab, set:
   - Name: `postgres-users`
5. In the `Connection` tab, set:
   - Host name/address: `postgres-users`
   - Port: `5432`
   - Username: `postgres`
   - Password: `postgres`
6. Click `Save`.

Repeat the same steps to add the shipment PostgreSQL server:

- Name: `postgres-shipments`
- Host name/address: `postgres-shipments`
- Port: `5432`
- Username: `postgres`
- Password: `postgres`

> Note: Use the Docker service names (`postgres-users` and `postgres-shipments`) as the host names because pgAdmin runs in a container on the same Docker network.

## Connect to MongoDB (tracking-service)

The `tracking-service` stores shipment history in MongoDB. Use this connection string in tools or drivers (do not open it in a browser):

`mongodb://mongo:mongo@localhost:27017/tracking_db?authSource=admin`

Options to connect:

1. **MongoDB Compass** — paste the connection string into the connection dialog and click Connect. You will see the `tracking_db` database and collections.
2. **mongosh (CLI)** — on your host, run:

```bash
mongosh "mongodb://mongo:mongo@localhost:27017/tracking_db?authSource=admin"
```

3. **From inside the container** — if you `docker exec -it mongo-tracking mongosh`, `localhost:27017` is correct inside the container; from your host, `localhost:27017` works only if the port is exposed in `docker-compose.yml`.
4. **IntelliJ Database tool** — add a MongoDB data source and paste the same connection string.

Why MongoDB:

- The tracking store is an appendable event timeline per shipment. MongoDB's document model and flexible schema make it easy to store a shipment document containing an array of events and to evolve that schema without heavy migrations.
- Fast read patterns for aggregated history queries make MongoDB a pragmatic choice for the `tracking-service` demo.


## 7. Build before running changes

To compile all modules and run tests locally:

```bash
./mvnw clean package
```

To build without tests:

```bash
./mvnw clean package -DskipTests
```

## 8. Common development workflow

1. Pull the latest branch.
2. Start the infrastructure with `docker compose up -d`.
3. Start `discovery-server` and `config-server` first.
4. Start `api-gateway`.
5. Start backend modules in dependency order.
6. Make code changes in the target module.
7. Run the module locally with `spring-boot:run` or build the project.
8. Verify behavior through the gateway or direct service endpoints.

## 9. Notes for contributors

- Services use local database and Kafka addresses from `application.yml`.
- If you change ports or service names, update the corresponding config files.
- If a service fails to start, check the logs for connection issues to Eureka, Config Server, Kafka, or PostgreSQL/MongoDB.
- Use the service-specific `application.yml` files to confirm ports and external dependencies.
