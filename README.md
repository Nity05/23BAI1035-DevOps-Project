# Internal Service Health & Operations Dashboard

A production-style internal observability portal built with Spring Boot and Thymeleaf, packaged for Docker containers, deployed on Kubernetes, and monitored with Graphite, Grafana, and Nagios.

This project demonstrates end-to-end DevOps capability:
- Application development with clean MVC layering
- Containerization with Docker
- Orchestration with Kubernetes
- CI/CD automation with Jenkins
- Observability with metrics (Graphite/Grafana) and availability checks (Nagios)

---

## 1. Project Highlights

- Java 21 + Spring Boot 3.5.4 application
- Server-side rendered UI with Thymeleaf
- Live CPU, memory, and request metrics calculated from JVM MXBeans and a servlet filter
- Support form submissions are captured in the backend with a point-in-time metrics snapshot
- Real-time service socket health checks on startup of each page load
- Actuator endpoints exposed for operational visibility
- Micrometer metrics exported to Graphite automatically every 10 seconds
- Grafana dashboard for metrics visualization using Graphite as data source
- Nagios availability checks for HTTP health of the application
- Kubernetes deployment with 2 replicas and NodePort service
- Jenkins pipeline for build, container image creation, and deployment

---

## 2. Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.4 |
| UI Templating | Thymeleaf |
| Build | Maven |
| Container | Docker |
| Orchestration | Kubernetes |
| CI/CD | Jenkins |
| Metrics Push | Micrometer → Graphite |
| Metrics Visualization | Grafana (queries Graphite) |
| Uptime Monitoring | Nagios (HTTP check_http) |

---

## 3. Complete Architecture and Runtime Flow

Graphite and Nagios are not frontend data sources. The Thymeleaf frontend is rendered by Spring Boot, while Graphite/Grafana and Nagios run beside the app as external observability systems.

```text
Browser
  |
  | GET /, /dashboard, /services, /contact
  v
Spring Boot app on port 9090
  |
  |-- HomeController renders Thymeleaf pages
  |-- PortalService calculates live dashboard values
  |-- MetricTrackingFilter counts requests and response time
  |-- Micrometer collects JVM, HTTP, and custom support metrics
  |
  | every 10 seconds
  v
GraphiteMeterRegistry
  |
  | plaintext metrics over TCP port 2003
  v
Graphite container
  |
  | stores time-series data in Whisper files
  v
Grafana container
  |
  | queries Graphite and renders charts
  v
Grafana UI on http://localhost:3000

Nagios container
  |
  | check_http -H host.docker.internal -p 9090
  | every 5 minutes
  v
Spring Boot app health/HTTP availability
```

### Frontend to Backend Flow

The frontend is server-side rendered. When the browser opens a route, the request goes to `HomeController`, the controller asks `PortalService` for the latest data, and Thymeleaf renders HTML.

```text
Browser requests /dashboard
  |
  v
HomeController.dashboard()
  |
  v
PortalService.getMetrics()
  |
  |-- CPU: OperatingSystemMXBean.getCpuLoad()
  |-- Memory: Runtime total/free memory
  |-- Request rate: MetricTrackingFilter total request count
  |-- Average response time: MetricTrackingFilter accumulated duration
  |-- Pod count: HOSTNAME environment variable
  v
Model attribute: metrics
  |
  v
dashboard.html renders metric cards
```

The dashboard page does not directly call Graphite. It displays live values calculated by the application. Graphite stores the Micrometer time-series version of those metrics for historical charts and Grafana dashboards.

### Graphite and Grafana Flow

Micrometer is the metrics bridge inside Spring Boot. The custom `GraphiteConfiguration` creates a `GraphiteMeterRegistry` with:

| Setting | Value |
|---|---|
| Protocol | Plaintext |
| Target host | `graphite`, with local fallback to `localhost` |
| Target port | `2003` |
| Push interval | 10 seconds |
| Metric prefix | `hr_portal` |

```text
Spring Boot metrics
  |
  | JVM, HTTP, Tomcat, system, and custom support counters
  v
Micrometer MeterRegistry
  |
  | every 10 seconds
  v
GraphiteMeterRegistry
  |
  | TCP plaintext protocol
  v
Graphite :2003
  |
  | writes Whisper time-series files under monitoring/graphite/storage
  v
Graphite Web :8082
  |
  v
Grafana :3000 queries Graphite as a data source
```

Important metric examples:

| Metric Area | Example Path |
|---|---|
| JVM memory | `hr_portal.jvm.memory.used.*` |
| System CPU | `hr_portal.system.cpu.usage` |
| HTTP requests | `hr_portal.http.server.requests.*` |
| Support submissions | `hr_portal.support.requests.submitted.*` |

### Nagios Flow

Nagios performs availability monitoring, not metrics storage. It reads `monitoring/nagios/hr-portal.cfg` and checks whether the HR Portal is reachable.

```text
Nagios service check
  |
  v
check_http -H host.docker.internal -p 9090
  |
  v
Spring Boot app on port 9090
  |
  | HTTP 200
  v
Nagios status: OK

If the app is stopped, slow, or unreachable:
Nagios status: CRITICAL
```

Configured behavior:

| Property | Value |
|---|---|
| Host name | `hr-portal` |
| Address | `host.docker.internal` |
| Check command | `check_http -H host.docker.internal -p 9090` |
| Check interval | 5 minutes |
| Retry interval | 1 minute |
| Max attempts | 5 |

### Support Request Backend Flow

When a user submits the support form on `/contact`, the form posts to the backend. The backend validates the request, captures the live metrics at that exact moment, attaches those metrics to the request object, saves the request in backend memory, and increments the Graphite support counter.

```text
User fills support form
  |
  | POST /contact
  v
HomeController.submitSupportRequest()
  |
  v
Validate SupportRequest
  |
  | invalid
  v
Redirect back to /contact with formError

Validate SupportRequest
  |
  | valid
  v
Normalize category
  |
  v
PortalService.getMetrics()
  |
  | captures CPU, memory, request rate, average response time,
  | pod count, deployment version, and availability
  v
Attach snapshot to SupportRequest
  |
  | request.setSubmittedAt(LocalDateTime.now())
  | request.setMetricsSnapshot(metricsAtSubmission)
  v
PortalService.saveSupportRequest(request)
  |
  v
Increment Micrometer counter support.requests.submitted
  |
  v
Graphite receives hr_portal.support.requests.submitted
  |
  v
Redirect back to /contact with formSuccess
```

Backend data captured per support request:

| Field | Source |
|---|---|
| Name | Contact form |
| Email | Contact form |
| Category | Contact form, defaults to `general` if blank |
| Message | Contact form |
| Submitted time | Backend `LocalDateTime.now()` |
| Metrics snapshot | `PortalService.getMetrics()` at submission time |

Current storage note: submitted support requests are retained in backend memory through `PortalService`. They remain available while the application process is running. For permanent storage, the next step would be adding Spring Data JPA with a database such as H2, PostgreSQL, or MySQL.

---

## 4. Application Routes

| Route | Description |
|---|---|
| `/` | Home dashboard: system status overview, KPI cards, and live stats bar |
| `/services` | Real-time service health table with socket latency probes |
| `/dashboard` | Live JVM metrics: CPU, memory, request rate, response time, and pod count |
| `/maintenance` | Scheduled maintenance windows |
| `/incidents` | Operations incident feed |
| `/contact` | Support directory and support request form; submissions are saved in backend memory with current metrics |
| `/actuator` | Spring Boot actuator base path |
| `/actuator/health` | Health endpoint monitored by Nagios |
| `/actuator/metrics` | List of Micrometer metric names |
| `/actuator/metrics/{name}` | Individual metric details |

---

## 5. Repository Structure

```text
hr_portal/
|-- src/main/java/com/example/hr_portal/
|   |-- controller/
|   |   `-- HomeController.java          # Route handlers and support form submission
|   |-- service/
|   |   `-- PortalService.java           # Live metrics, service checks, support request storage
|   |-- model/
|   |   |-- ServiceStatusInfo.java       # Service health model
|   |   |-- MaintenanceWindow.java       # Maintenance schedule model
|   |   |-- Incident.java                # Incident feed model
|   |   |-- SupportRequest.java          # Support form payload plus captured metrics snapshot
|   |   `-- MetricDetail.java            # Dashboard metric model
|   `-- config/
|       |-- GraphiteConfiguration.java   # Pushes Micrometer metrics to Graphite every 10 seconds
|       `-- MetricTrackingFilter.java    # Counts application requests and response latency
|-- src/main/resources/
|   |-- templates/
|   |   |-- index.html                   # Home dashboard
|   |   |-- services.html                # Service status table
|   |   |-- dashboard.html               # JVM metric cards
|   |   |-- maintenance.html             # Maintenance schedule
|   |   |-- incidents.html               # Incident feed
|   |   `-- contact.html                 # Support directory and support form
|   |-- static/
|   |   |-- css/style.css                # UI styling
|   |   `-- js/script.js                 # Clock, nav state, status badges, form UI helpers
|   `-- application.properties          # App, actuator, and Graphite settings
|-- monitoring/
|   |-- graphite/storage/                # Graphite Whisper storage volume
|   `-- nagios/
|       `-- hr-portal.cfg                # Nagios host and HTTP service checks
|-- k8s/
|   |-- deployment.yaml                  # Kubernetes deployment
|   `-- service.yaml                     # Kubernetes NodePort service
|-- Dockerfile                           # Application image definition
|-- docker-compose.yml                   # Graphite, Grafana, and Nagios stack
|-- Jenkinsfile                          # CI/CD pipeline
`-- pom.xml                              # Spring Boot and Micrometer dependencies
```

---

## 6. Local Development Run

### Prerequisites

- Java 21
- Maven 3.9+
- Docker Desktop (for the monitoring stack)
- Kubernetes cluster (Docker Desktop Kubernetes or Minikube) — optional
- Jenkins — optional for CI/CD demo

### Step 1 — Start the Monitoring Stack (Graphite + Grafana + Nagios)

```powershell
docker compose up -d
```

Verify all three containers are running:

```powershell
docker ps
```

Expected output: `graphite`, `grafana`, `nagios` containers with status `Up`.

| Service | URL | Default Login |
|---|---|---|
| Graphite | http://localhost:8082 | none required |
| Grafana | http://localhost:3000 | admin / admin |
| Nagios | http://localhost:8083/nagios | nagiosadmin / nagios |

### Step 2 — Run the Spring Boot Application

```powershell
.\mvnw.cmd spring-boot:run
```

Application URL: http://localhost:9090

### Step 3 — Verify Metric Export to Graphite

Within 30 seconds of the app starting, open http://localhost:8082 in your browser.

In the left panel, expand: **Metrics → hr_portal**

You will see folders for:
- `hr_portal.jvm.memory.*` — Heap and non-heap memory
- `hr_portal.system.cpu.*` — Process and system CPU load
- `hr_portal.http.server.requests.*` — Request counts by endpoint
- `hr_portal.tomcat.*` — Thread pool and session metrics

Click any metric to see a live chart rendered by Graphite.

### Step 4 — Connect Grafana to Graphite

1. Open http://localhost:3000 → Login as `admin / admin`
2. Go to **Connections → Data Sources → Add data source**
3. Choose **Graphite**
4. Set URL to: `http://graphite` (inside Docker network)
5. Click **Save & Test** — it should show "Data source is working"
6. Create a new Dashboard panel with a Graphite metric query such as:
   ```
   hr_portal.jvm.memory.used.heap
   ```

### Step 5 — Verify Nagios HTTP Check

1. Open http://localhost:8083/nagios → Login as `nagiosadmin / nagios`
2. Go to **Services** in the left panel
3. You will see **hr-portal → HR Portal HTTP** with status **OK** (green) if the app is running

If the status shows PENDING, wait 1–2 minutes for Nagios to run the first check cycle.

### Step 6 — Verify Live Dashboard Metrics

Open http://localhost:9090/dashboard — the metric cards now show:
- **CPU %** calculated from `OperatingSystemMXBean.getCpuLoad()`
- **Memory %** calculated from `Runtime.getRuntime()`
- **Request Rate** counted by `MetricTrackingFilter`
- **Avg Response Time** accumulated by `MetricTrackingFilter`

Reload the page several times then revisit `/dashboard` — the request rate and response time will update with real measurements.

---

## 7. Graphite Not Showing Metrics — Troubleshooting

The most common reason metrics don't appear in Graphite when running locally:

**Problem:** The app's `application.properties` sets `graphite.host=graphite`. The hostname `graphite` only resolves inside the Docker Compose bridge network. When running with `spring-boot:run` on your local machine, the hostname is unresolvable.

**Solution (already implemented):** `GraphiteConfiguration.java` catches the `UnknownHostException` and automatically falls back to `localhost`, which routes to the Docker-exposed port 2003.

**Verify the fix is working:**

```powershell
# Check if port 2003 is accepting connections
Test-NetConnection -ComputerName localhost -Port 2003
```

Should show `TcpTestSucceeded: True`.

If Graphite still shows no metrics:

```powershell
# Confirm graphite container is running and port is bound
docker ps --filter name=graphite
```

Wait 20–30 seconds after app startup, then refresh http://localhost:8082.

---

## 8. Docker Workflow

Build application JAR:

```powershell
.\mvnw.cmd clean package
```

Build Docker image:

```powershell
docker build -t hr-portal:latest .
```

Run container (connects to same Docker network as Graphite):

```powershell
docker run --rm -p 9090:9090 --network hr_portal_monitoring --name hr-portal hr-portal:latest
```

> When running the app inside Docker on the `monitoring` network, the hostname `graphite` resolves directly — no localhost fallback needed.

---

## 9. Kubernetes Deployment

Apply manifests:

```powershell
kubectl apply -f k8s\deployment.yaml
kubectl apply -f k8s\service.yaml
```

Check resources:

```powershell
kubectl get deployments,pods,svc
```

Service exposure:
- App NodePort: **30081** (maps to container port 9090)
- Actuator NodePort: **30090**

> **Important:** `deployment.yaml` includes a `hostAliases` entry that maps `host.docker.internal` to a local IP (`192.168.1.7` by default). Replace this with the IP returned by `ping host.docker.internal` on your machine. This is required so Nagios can reach the app at `host.docker.internal:9090` from inside its container.

---

## 10. Jenkins CI/CD Pipeline

Pipeline stages:

1. **Checkout** — pulls latest source from SCM
2. **Build** — `mvn clean package` produces the JAR
3. **Docker Build** — builds `hr-portal:latest` image
4. **Deploy** — applies both Kubernetes manifests

```text
Checkout → Maven Build → Docker Build → Kubernetes Deploy → Post Actions
```

Jenkins prerequisites:
- JDK tool configured as `JDK21`
- Maven tool configured as `Maven-3.9.16`
- Docker and kubectl installed on the Jenkins agent
- `KUBECONFIG` environment variable pointing to cluster config

---

## 11. Observability Design — Full Sequence

```
Spring Boot App
    │
    ├─► Micrometer Registry
    │       Every 10 seconds pushes over TCP to Graphite:2003
    │       Metric names: hr_portal.jvm.*, hr_portal.system.*, hr_portal.http.*
    │
    ├─► Graphite (port 2003 intake, 8082 web)
    │       Stores Whisper time-series files
    │       Serves render API for Grafana queries
    │
    ├─► Grafana (port 3000)
    │       Connects to Graphite as data source
    │       Renders dashboards with time-series panels
    │
    └─► Actuator (port 9090/actuator)
            └─► Nagios polls /actuator/health via check_http every 5 min
                    HTTP 200 → Service OK (green)
                    Timeout / 5xx → Service CRITICAL (red, alerts fire)
```

Configured observability settings:
- Actuator endpoints fully exposed (`management.endpoints.web.exposure.include=*`)
- Graphite exporter enabled with **10-second push step**
- Global metric tag: `application=hr_portal`
- Nagios check interval: **5 minutes**, retry interval: **1 minute**

---

## 12. Tests

Run all unit and integration tests:

```powershell
.\mvnw.cmd test
```

Expected result:
```
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The context-load test verifies:
- Spring application context initialises correctly
- All controllers, services, and filters wire up without errors
- Graphite registry bean is created (with fallback hostname logic)
- Thymeleaf template resolution is functional for all 6 routes

---

## 13. Interview Walkthrough Script

1. **Architecture** — explain the MVC + Thymeleaf stack and why it suits internal portals.
2. **Live Demo** — run app and navigate all 6 pages showing live CPU/memory values.
3. **Actuator** — open `/actuator/metrics` and explain the Micrometer metric model.
4. **Graphite** — open http://localhost:8082, expand `hr_portal.*` tree, show a live chart.
5. **Grafana** — show Graphite data source configuration and a metric panel.
6. **Nagios** — open http://localhost:8083, show the HR Portal HTTP check status.
7. **Docker** — build the image and run with `--network hr_portal_monitoring`.
8. **Kubernetes** — apply manifests, show 2 running pods, explain NodePort exposure.
9. **Jenkins** — walk through Jenkinsfile stages: Checkout → Build → Docker → K8s Deploy.

---

## 14. Suggested Improvements

- Add Grafana dashboard provisioning YAML so panels auto-load on container startup
- Add Kubernetes readiness and liveness probes using `/actuator/health`
- Push Docker images to a registry with immutable semantic version tags
- Add Helm chart for parameterised deployment across environments
- Add GitHub Actions workflow as an alternative to Jenkins
- Add container vulnerability scanning in the pipeline (Trivy or Grype)
- Replace simulated VPN probe with real internal gateway connectivity check
- Wire `/actuator/metrics` endpoint values directly into a dedicated API endpoint for the dashboard (`/api/metrics`) to decouple live data from the MVC layer

---

## 15. License

Academic and interview demonstration project.
