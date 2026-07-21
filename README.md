# DevOps Cloud Reliability Platform — Version 1 (V1)

A practical **Cloud Reliability Platform & Operations Dashboard** designed for DevOps/SRE and support teams. Built on top of the existing application codebase, this V1 release turns support ticket handling and telemetry metrics into a unified operations center to monitor, triage, and understand application health in real time.

---

## 🚀 Key V1 Highlights

* **Integrated Operations Dashboard**: Real-time observability dashboard displaying request counters, response latency, error rate, pod replica health, and active incident summaries.
* **Support Request Lifecycle Management**: Dedicated triage queue for support requests with status progression (`Open` → `In Progress` → `Resolved`), priority tags (`P1 - Critical`, `P2 - High`, `P3 - Low`), and telemetry snapshots taken at submission.
* **OpenTelemetry Instrumentation & Prometheus Metrics**: Exposes standard Prometheus scrapable metrics at `/actuator/prometheus` and uses OpenTelemetry SDK for tracer/meter registration.
* **Grafana Integration**: Pre-provisioned Grafana dashboards (`cloud_reliability_dashboard.json`) visualizing HTTP throughput, p95 latency, JVM memory, and support ticket submission rates.
* **Multi-Stage Containerization**: Optimized Dockerfile with a builder stage (`maven:3.9.6-alpine`) and a minimal, secure runtime stage (`eclipse-temurin:21-jre-alpine`).
* **Kubernetes Ready**: Kubernetes manifests including Deployment with Liveness/Readiness probes (`/actuator/health`), NodePort Service, Prometheus ConfigMap/Deployment, and Grafana deployment.
* **GitHub Actions CI/CD Pipeline**: Continuous integration workflow validating Maven builds, running tests, building Docker container images, and linting Kubernetes manifests.

---

## 🛠️ V1 Technology Stack

* **Application**: Java 21, Spring Boot 3.5.4, Thymeleaf, HTML5/CSS3, JavaScript, Chart.js
* **Observability & Metrics**: OpenTelemetry SDK, Prometheus (`/actuator/prometheus`), Grafana
* **Container & Orchestration**: Docker, Docker Compose, Kubernetes
* **CI/CD Pipeline**: GitHub Actions

---

## 🏗️ End-to-End Architecture Flow

```text
Developer -> GitHub -> GitHub Actions CI/CD -> Docker image -> Kubernetes -> OpenTelemetry -> Prometheus -> Grafana -> Operations Dashboard
```

```mermaid
flowchart TD
    Dev[Developer] -->|Push Code| GH[GitHub Repository]
    GH -->|Trigger Workflow| GHA[GitHub Actions CI/CD]
    GHA -->|Package & Build| Docker[Docker Image]
    Docker -->|Deploy Manifests| K8s[Kubernetes Cluster]
    
    subgraph K8sCluster[Kubernetes Cluster]
        AppPod[hr-portal Pods]
        OTel[OpenTelemetry SDK]
        Actuator[/actuator/prometheus Endpoint]
        AppPod --- OTel
        AppPod --- Actuator
    end

    Prometheus[Prometheus Server] -->|Scrape metrics| Actuator
    Grafana[Grafana Dashboard] -->|Query metrics| Prometheus
    OpsUser[DevOps / Support Engineer] -->|Monitor & Triage| OpsDash[Operations Dashboard UI]
    OpsDash -->|API Query| AppPod
```

---

## 🤝 How Support Requests & Metrics Work Together

In traditional operations, support tickets and application telemetry live in disconnected silos. In this platform, **Support Requests and Metrics are tightly integrated**:

1. **Point-in-Time Metrics Snapshot**: When a user or automated monitoring system submits a support request (via `/contact`), the backend captures a snapshot of current JVM CPU load, memory utilization, request rate, and latency. This snapshot is stored directly alongside the support ticket.
2. **Reduced MTTR (Mean Time To Resolve)**: SRE and support engineers viewing the Operations Dashboard (`/dashboard`) or Support Queue (`/support-requests`) can immediately correlate reported issues with system health at the exact moment the ticket was created.
3. **Operational Telemetry Feedback**: Ticket submissions automatically increment Prometheus counters (`support_requests_submitted_total`), allowing Grafana to chart ticket volume alongside latency spikes or deployment changes.

---

## ⚙️ Quickstart Setup Guide

### Option 1: Run Locally (Maven + Spring Boot)

```bash
cd hr_portal
./mvnw spring-boot:run
```
Access the app at: `http://localhost:9090`
* **Ops Dashboard**: `http://localhost:9090/dashboard`
* **Support Ticket Queue**: `http://localhost:9090/support-requests`
* **Prometheus Metrics**: `http://localhost:9090/actuator/prometheus`
* **Liveness Probe**: `http://localhost:9090/actuator/health`

---

### Option 2: Run via Docker Compose (Full Stack)

To launch the application along with Prometheus and Grafana:

```bash
cd hr_portal
docker-compose up --build -d
```
Services exposed:
* **Application Operations Dashboard**: `http://localhost:9090/dashboard`
* **Prometheus Server**: `http://localhost:9091`
* **Grafana Dashboards**: `http://localhost:3000` (User: `admin`, Password: `admin`)

---

### Option 3: Deploy to Kubernetes

Apply the Kubernetes manifests in the `k8s/` directory:

```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/prometheus-k8s.yaml
kubectl apply -f k8s/grafana-k8s.yaml
```
Verify pod status:
```bash
kubectl get pods -l app=hr-portal
```

---

## 🧪 Verification & Testing

Run unit & integration tests using Maven:
```bash
./mvnw clean test
```
Or verify API endpoints via curl:
```bash
curl http://localhost:9090/api/ops/summary
curl http://localhost:9090/actuator/prometheus
```
