# Kubernetes deployment

## 1. Choosing the Kubernetes environment

For a solo developer, there is no need for a paid cloud cluster:

| Option | Recommendation |
|---|---|
| **Kind** (Kubernetes in Docker) | ✅ Recommended — lightweight, fast to recreate, good multi-node simulation |
| **Minikube** | ✅ Valid alternative, useful built-in dashboard |
| GKE/EKS/AKS | ❌ Not necessary for the demo, costly and adds billing complexity |

Starting command (Kind):
```bash
kind create cluster --name delivery-tracking --config kind-config.yaml
```

The repository provides local development manifests, but Kind or Minikube must be installed and configured before deployment.

## 2. Manifest organization (Kustomize)

The current development manifests are available under `k8s/`:

```text
k8s/
├── base/
│   ├── apps.yaml              # eight application Deployments and Services
│   ├── databases.yaml         # four PostgreSQL databases and MongoDB
│   ├── kafka.yaml             # single-node Kafka KRaft with PVC
│   ├── zipkin.yaml
│   ├── configmap.yaml
│   ├── secrets.yaml
│   └── kustomization.yaml
└── overlays/dev/kustomization.yaml
```

These manifests are sized for local Kind or Minikube use. They are not production database operators or highly available Kafka.

Render the development overlay without connecting to a cluster:

```powershell
kubectl kustomize k8s/overlays/dev
```

After creating a cluster, load the local Jib images and apply the overlay as described in the deployment section below.

## 3. Example manifest — user-service

**Example user-service deployment**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 1
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
        - name: user-service
          image: <your-dockerhub>/user-service:latest
          ports:
            - containerPort: 8081
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "kubernetes"
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              value: "http://discovery-server:8761/eureka"
            - name: SPRING_CONFIG_IMPORT
              value: "configserver:http://config-server:8888"
            - name: SPRING_DATASOURCE_URL
              valueFrom:
                configMapKeyRef:
                  name: user-service-config
                  key: db-url
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: password
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8081
            initialDelaySeconds: 20
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8081
            initialDelaySeconds: 30
            periodSeconds: 15
          resources:
            requests:
              memory: "256Mi"
              cpu: "200m"
            limits:
              memory: "512Mi"
              cpu: "500m"
```

**`k8s/base/user-service/service.yaml`**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  selector:
    app: user-service
  ports:
    - port: 8081
      targetPort: 8081
```

The repository base uses the same pattern for all application services and overrides host-local URLs with Kubernetes service names.

Before applying the development overlay with Kind, load the locally built Jib images:

```powershell
kind load docker-image package-delivery/discovery-server:latest --name delivery-tracking
kind load docker-image package-delivery/config-server:latest --name delivery-tracking
kind load docker-image package-delivery/api-gateway:latest --name delivery-tracking
kind load docker-image package-delivery/user-service:latest --name delivery-tracking
kind load docker-image package-delivery/shipment-service:latest --name delivery-tracking
kind load docker-image package-delivery/tracking-service:latest --name delivery-tracking
kind load docker-image package-delivery/delivery-service:latest --name delivery-tracking
kind load docker-image package-delivery/notification-service:latest --name delivery-tracking
```

## 4. External infrastructure (Postgres / MongoDB / Kafka)

For a solo project, there are two practical approaches:

**Option A (recommended to start) — simple manifests**
- `Deployment` + `PersistentVolumeClaim` + `Service` for PostgreSQL and MongoDB
- A simple `StatefulSet` for Kafka (KRaft mode, without Zookeeper, to simplify)

**Option B (more robust if time allows) — official Helm charts**
```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install postgresql bitnami/postgresql
helm install kafka bitnami/kafka
helm install mongodb bitnami/mongodb
```
This option avoids writing StatefulSet manifests by hand — a significant time saver for a solo developer.

## 5. Exposing the API Gateway

**Simple local option — NodePort**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
spec:
  type: NodePort
  selector:
    app: api-gateway
  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 30080
```

**More realistic option — Ingress**
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: delivery-tracking-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
    - host: delivery-tracking.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: api-gateway
                port:
                  number: 8080
```
(This requires an Ingress Controller, e.g. `ingress-nginx`, installable with one command on Kind/Minikube.)

## 6. ConfigMaps and Secrets

- **ConfigMap**: non-sensitive connection URLs, Kafka topic names, feature flags
- **Secret**: DB passwords, JWT signing key

```bash
kubectl create secret generic postgres-secret \
  --from-literal=password=changeme123
```

## 7. Useful daily commands (solo dev)

```bash
kubectl apply -k k8s/overlays/dev          # full deployment
kubectl get pods -w                         # watch startup progress
kubectl logs -f deployment/shipment-service  # live logs
k9s                                          # interactive navigation (strongly recommended)
kubectl port-forward svc/api-gateway 8080:8080   # quick local access without Ingress
```

The base exposes the API Gateway as NodePort `30080` and defines `delivery-tracking.local` for an Ingress controller. With port-forwarding, use `http://localhost:8080`.

## 8. Recommended startup order (dependencies)

1. PostgreSQL, MongoDB, Kafka (Zipkin will be added with observability)
2. config-server
3. discovery-server
4. api-gateway
5. user-service, shipment-service, delivery-service, tracking-service, notification-service (can start in parallel once 1-4 are ready)

> Tip: use `initContainers` or strict `readinessProbe`s instead of manual wait scripts — Kubernetes handles restarts automatically for transient dependency issues.

## 9. Improvement opportunities (bonus, mention in the final report)

- **HorizontalPodAutoscaler** on shipment-service (demonstrate scalability)
- **NetworkPolicy** to isolate services from each other
- **CI/CD** with GitHub Actions: build Jib → push image → `kubectl apply`
- **Service Mesh** (Istio/Linkerd) — out of scope for solo work but good discussion material for the report
