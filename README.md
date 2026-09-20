# ScaleGen — Enterprise GenAI Gateway & Intelligent LLM Router

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg?style=flat-square)](https://github.com/kamran-asif/ScaleGen)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF.svg?style=flat-square&logo=githubactions)](https://github.com/kamran-asif/ScaleGen/actions)
[![Java](https://img.shields.io/badge/Java-17-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.6.0-red.svg?style=flat-square&logo=apachekafka)](https://kafka.apache.org/)
[![OpenTelemetry](https://img.shields.io/badge/OpenTelemetry-Instrumented-blue.svg?style=flat-square&logo=opentelemetry)](https://opentelemetry.io/)
[![React](https://img.shields.io/badge/React-18-cyan.svg?style=flat-square&logo=react)](https://react.dev/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Azure%20AKS-326ce5.svg?style=flat-square&logo=kubernetes)](https://kubernetes.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](LICENSE)

**ScaleGen** is an enterprise-grade, distributed microservices platform engineered for high-throughput, fault-tolerant Generative AI request orchestration and intelligent LLM routing. 

Built on **Spring Boot 3**, **Apache Kafka**, **PostgreSQL**, **Redis**, and **React 18**, ScaleGen provides unified access to multi-vendor Foundation Models (OpenAI, Anthropic, Meta/Ollama) with automatic failover, real-time cost-latency-quality arbitration, cryptographic idempotency, PII scrubbing, and end-to-end distributed tracing.

---

## Table of Contents

1. [Architectural Blueprints](#architectural-blueprints)
   - [1. Core Microservices & Intelligent Router Topology](#1-core-microservices--intelligent-router-topology)
   - [2. Resilience & Fault-Tolerance Decision Flow](#2-resilience--fault-tolerance-decision-flow)
   - [3. Telemetry & Full-Stack Observability Pipeline](#3-telemetry--full-stack-observability-pipeline)
   - [4. CI/CD & Cloud Infrastructure Architecture](#4-cicd--cloud-infrastructure-architecture)
   - [5. Zero-Trust Security & Identity Architecture](#5-zero-trust-security--identity-architecture)
2. [End-to-End Sequence & Data Flow](#end-to-end-sequence--data-flow)
3. [Deep-Dive Systems Design](#deep-dive-systems-design)
   - [Intelligent LLM Router (Cost • Quality • Latency)](#intelligent-llm-router-cost--quality--latency)
   - [Distributed Gateway & Rate Limiting](#distributed-gateway--rate-limiting)
   - [Idempotency & Lifecycle Engine](#idempotency--lifecycle-engine)
   - [Resilience Circuitry & DLQ Cascading](#resilience-circuitry--dlq-cascading)
   - [PII Sanitization & Multi-Tier Persistence](#pii-sanitization--multi-tier-persistence)
4. [Microservices Catalog & Network Matrix](#microservices-catalog--network-matrix)
5. [Intelligent Routing Engine Matrix](#intelligent-routing-engine-matrix)
6. [API Specification & Examples](#api-specification--examples)
7. [Local Development Quickstart](#local-development-quickstart)
8. [Production Deployment (Azure AKS)](#production-deployment-azure-aks)
9. [Repository Structure](#repository-structure)
10. [License](#license)

---

## Architectural Blueprints

### 1. Core Microservices & Intelligent Router Topology

```
                              ┌──────────────────────┐
                              │       React.js       │
                              │   Client / Dashboard │
                              └──────────┬───────────┘
                                         │ HTTPS
                                         ▼
                         ┌─────────────────────────────┐
                         │   Spring Cloud Gateway     │
                         │ Auth • Rate Limit • Routing │
                         └─────────────┬───────────────┘
                                       │
                                       ▼
                         ┌─────────────────────────────┐
                         │       Orchestrator          │
                         │ Validation • Idempotency   │
                         │ Request Lifecycle           │
                         └─────────────┬───────────────┘
                                       │
                                       ▼
                         ┌─────────────────────────────┐
                         │          Kafka              │
                         │ Async Event / Work Queue    │
                         └─────────────┬───────────────┘
                                       │
                  ┌────────────────────┼────────────────────┐
                  │                    │                    │
                  ▼                    ▼                    ▼
          ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
          │ Inference    │     │ Inference    │     │ Inference    │
          │ Worker 1     │     │ Worker 2     │     │ Worker N     │
          └──────┬───────┘     └──────┬───────┘     └──────┬───────┘
                 └────────────────────┼─────────────────────┘
                                      ▼
                         ┌─────────────────────────────┐
                         │    Intelligent Model       │
                         │         Router              │
                         │ Cost • Quality • Latency   │
                         └─────────────┬───────────────┘
                                       │
                         ┌─────────────┼─────────────┐
                         ▼             ▼             ▼
                    ┌────────┐    ┌────────┐    ┌────────┐
                    │ Model A│    │ Model B│    │ Model C│
                    │  Fast  │    │Balanced│    │  Large │
                    └────┬───┘    └────┬───┘    └────┬───┘
                         └─────────────┼─────────────┘
                                       ▼
                              ┌────────────────┐
                              │  OpenAI / LLM  │
                              │    Provider    │
                              └───────┬────────┘
                                      │
                                      ▼
                         ┌─────────────────────────────┐
                         │    Response Processor      │
                         │ Validation • Formatting    │
                         │ Token/Cost Tracking        │
                         └─────────────┬───────────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    ▼                  ▼                  ▼
             ┌────────────┐     ┌────────────┐     ┌────────────┐
             │ PostgreSQL │     │   Redis    │     │Azure Blob  │
             │ Metadata   │     │ Cache      │     │ Storage    │
             └────────────┘     └────────────┘     └────────────┘
```

#### Topology Flowchart (Mermaid)

```mermaid
flowchart TD
    Client(["React.js Client\n(SPA Dashboard)"]) -->|HTTPS / WSS| GW["Spring Cloud Gateway\n(OAuth2/JWT Auth • Redis Token Bucket Rate Limiter)"]
    
    subgraph IngressTier ["Ingress & Orchestration Tier"]
        GW -->|REST / gRPC| Orch["Orchestrator Service\n(Schema Validation • SHA-256 Idempotency Engine)"]
        Orch <-->|Cache Check / Lock| RedisCache[(Redis In-Memory)]
    end

    subgraph MessagingTier ["Event Streaming Fabric"]
        Orch -->|Produce| KReq["Kafka Topic:\ninference-requests"]
        KReq --> KWork["Kafka Topic:\ninference-requests.worker"]
    end

    subgraph ComputeWorkers ["Distributed Inference Worker Pool (HPA Scaled)"]
        KWork --> W1["Worker Pod 1"]
        KWork --> W2["Worker Pod 2"]
        KWork --> WN["Worker Pod N"]
    end

    subgraph RoutingAndResilience ["Intelligent Routing & Resilience Engine"]
        W1 & W2 & WN --> Router["Intelligent Router\n(Dynamic Scoring: Cost • Quality • Latency)"]
        Router --> RouteA["Model A: Fast / Low Cost\n(Llama 3 70B / Mistral)"]
        Router --> RouteB["Model B: Balanced\n(Claude 3.5 Sonnet)"]
        Router --> RouteC["Model C: Large / Reasoning\n(GPT-4o)"]
        
        RouteA & RouteB & RouteC --> LLMProvider["LLM Provider Execution"]
        LLMProvider --> Resilience["Resilience Engine\n(Circuit Breaker • Jittered Backoff • Fallback Cascade)"]
        Resilience -->|Poison Pill / Exhaustion| DLQ["Kafka Topic:\ninference-dlq"]
    end

    subgraph ResponseProcessingTier ["Response Processing & Multi-Tier Persistence"]
        Resilience -->|Raw Stream| RP["Response Processor Service\n(PII Masking • Token Accounting • Pricing)"]
        RP -->|Relational Audit & Metadata| PG[(PostgreSQL)]
        RP -->|Prompt/Response Caching| RedisCache
        RP -->|Cold Payload & Artifact Store| Blob[(Azure Blob Storage)]
        RP -->|Publish Completion| KResp["Kafka Topic:\ninference-responses"]
        KResp --> GW
    end
```

---

### 2. Resilience & Fault-Tolerance Decision Flow

```
                         Inference
                            │
                       ┌────┴────┐
                       │         │
                    Success    Failure
                       │         │
                       ▼         ▼
                    Response    Retry
                                 │
                          ┌──────┴──────┐
                          │             │
                       Success       Failure
                          │             │
                          │             ▼
                          │       Fallback Model
                          │             │
                          │       ┌─────┴─────┐
                          │       │           │
                          │    Success      Failure
                          │       │           │
                          │       │           ▼
                          │       │          DLQ
                          └───────┴─────────────
```

#### Resilience Decision Matrix (Mermaid)

```mermaid
flowchart TD
    Start(["Inference Request Dispatched"]) --> Primary["Invoke Primary Model (Model A)"]
    Primary --> CheckPrimary{Call Status?}
    CheckPrimary -- "Success (200 OK)" --> ReturnSuccess["Return Response to Client"]
    CheckPrimary -- "Failure (429 / 5xx / Timeout)" --> RetryCount{"Max Retries Reached?\n(Exp Backoff + Full Jitter)"}
    
    RetryCount -- "No (Retry <= 3)" --> RetryAction["Sleep with Jittered Backoff\nRetry Primary Model"]
    RetryAction --> Primary
    
    RetryCount -- "Yes (Retries Exhausted)" --> FallbackCheck{"Has Fallback Model?\n(Circuit Breaker Checked)"}
    FallbackCheck -- "Yes" --> FallbackModel["Invoke Fallback Model (Model B / C)"]
    FallbackModel --> CheckFallback{Fallback Status?}
    CheckFallback -- "Success (200 OK)" --> AnnotateFallback["Annotate Metadata (fallback: true)\nReturn Response"]
    CheckFallback -- "Failure" --> FallbackRetry{"More Fallbacks Available?"}
    FallbackRetry -- "Yes" --> FallbackModel
    FallbackRetry -- "No" --> DLQAction["Route to Dead Letter Queue (DLQ)\nEmit Failure Notification"]
    DLQAction --> EndFail(["Request Terminated in DLQ"])
    AnnotateFallback --> ReturnSuccess
```

---

### 3. Telemetry & Full-Stack Observability Pipeline

```
Gateway
   │
Orchestrator
   │
 Kafka
   │
Workers
   │
LLM Provider
   │
Response Processor
   │
   ▼
OpenTelemetry
   │
   ▼
OpenTelemetry Collector
   │
   ├──────────────► Jaeger
   │                 Traces
   │
   ├──────────────► Prometheus
   │                 Metrics
   │
   └──────────────► OpenSearch
                     Logs
                        │
                        ▼
                     Grafana
```

#### Telemetry Flowchart (Mermaid)

```mermaid
flowchart LR
    subgraph Microservices ["ScaleGen Distributed Services"]
        GW["API Gateway"]
        Orch["Orchestrator"]
        Kafka["Kafka Broker"]
        Workers["Inference Workers"]
        LLM["LLM Providers"]
        RP["Response Processor"]
    end

    GW & Orch & Kafka & Workers & LLM & RP -->|W3C Trace Context / OTLP| OTel["OpenTelemetry SDK / Agent"]
    OTel -->|OTLP gRPC :4317 / HTTP :4318| Collector["OpenTelemetry Collector"]

    Collector -->|Distributed Traces| Jaeger["Jaeger\n(Trace Spans, Latency Waterfall)"]
    Collector -->|Scrape Metrics| Prometheus["Prometheus\n(RED Metrics, Token Rates, Error %)"]
    Collector -->|Structured JSON Logs| OpenSearch["OpenSearch / Elasticsearch\n(Audit Logs, TraceID Linked)"]

    Jaeger & Prometheus & OpenSearch --> Grafana["Grafana Unified Dashboards\n(Executive KPI, Alerts, Latency SLAs)"]
```

---

### 4. CI/CD & Cloud Infrastructure Architecture

```
                    GitHub Repository
                           │
                           ▼
                    GitHub Actions
                           │
                Build • Test • Scan
                           │
                           ▼
                    Docker Image
                           │
                           ▼
                 Azure Container Registry
                           │
                           ▼
                      Azure AKS
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
           Gateway      Workers       Services
                            │
                         HPA
                            │
                  Horizontal Scaling
```

#### DevOps & GitOps Lifecycle (Mermaid)

```mermaid
flowchart TD
    GitRepo["GitHub Repository\n(kamran-asif/ScaleGen)"] -->|Git Push to main| GHA["GitHub Actions CI/CD Pipeline"]
    
    subgraph Pipeline ["Automated Build & Quality Gates"]
        GHA --> Build["Maven Build & Unit Tests (JDK 17)"]
        Build --> Scan["Security & Vulnerability Scan"]
        Scan --> Dockerize["Docker Multi-Stage Container Build"]
    end

    Dockerize --> ACR["Azure Container Registry (ACR)\n(Versioned OCI Images)"]
    ACR --> AKS["Azure Kubernetes Service (AKS)\n(Production Cluster)"]

    subgraph ClusterWorkloads ["Kubernetes Deployments"]
        AKS --> PodGW["API Gateway Pods"]
        AKS --> PodOrch["Orchestrator Pods"]
        AKS --> PodWork["Inference Worker Pods"]
        AKS --> PodRP["Response Processor Pods"]
        
        PodWork --> HPA["Horizontal Pod Autoscaler (HPA)\n(Scaled on Kafka Lag & CPU Util)"]
    end
```

---

### 5. Zero-Trust Security & Identity Architecture

```
Client
  │
  ▼
Gateway
  │
Authentication / Authorization
  │
  ▼
Services
  │
  ├── Azure Key Vault → Secrets
  ├── TLS → Service communication
  └── Request validation
```

#### Security Domain Model (Mermaid)

```mermaid
flowchart TD
    User(["External Client / React App"]) -->|mTLS / HTTPS (TLS 1.3)| Gateway["Spring Cloud Gateway Ingress"]
    
    subgraph EdgeSecurity ["Edge Perimeter"]
        Gateway --> Auth["OAuth2.0 / OIDC JWT Token Validation"]
        Gateway --> WAF["Rate Limiting & IP Throttling (Redis)"]
    end

    subgraph InternalMesh ["Internal Zero-Trust Mesh"]
        Auth --> Services["Internal Microservices (mTLS Encrypted)"]
        Services --> KeyVault["Azure Key Vault\n(Secret Rotation, Model API Keys, DB Credentials)"]
        Services --> Validation["Strict Input Schema Validation\n(JSON Schema, Prompt Bound Checks)"]
        Services --> RBAC["Role-Based Access Control (Tenant Segregation)"]
    end
```

---

## End-to-End Sequence & Data Flow

```mermaid
sequenceDiagram
    autonumber
    actor User as React.js Client
    participant GW as Spring Cloud Gateway
    participant Orch as Orchestrator
    participant Redis as Redis Cache
    participant Kafka as Apache Kafka
    participant Worker as Inference Worker
    participant Router as Intelligent Router
    participant LLM as LLM Provider (OpenAI/Anthropic/Ollama)
    participant RP as Response Processor
    participant DB as PostgreSQL / Azure Blob

    User->>GW: POST /api/v1/inference (Payload + Bearer Token + Idempotency-Key)
    GW->>GW: Verify JWT & Token Bucket Rate Limits (Redis)
    GW->>Orch: Forward Validated Request
    Orch->>Redis: Check Idempotency Key (SHA-256 Hash)
    alt Idempotency Hit (Cached Request)
        Redis-->>Orch: Return Cached Payload
        Orch-->>User: 200 OK (Instant Cached Response <5ms)
    else Idempotency Miss (New Invocations)
        Orch->>Kafka: Publish to 'inference-requests'
        Orch-->>User: 202 Accepted (requestId: "req-7b89f1a2")
        Kafka->>Worker: Consume Event Partition
        Worker->>Router: Compute Model Route (Strategy: AUTO/COST/QUALITY/LATENCY)
        Router-->>Worker: Route Chain: [Model A (Primary), Model B (Fallback 1), Model C (Fallback 2)]
        
        loop Fallback Cascading & Circuit Breaker
            Worker->>Worker: Check CircuitBreaker.allowExecution(Model)
            alt Circuit CLOSED
                Worker->>LLM: Dispatch Inference Call (with Exponential Backoff)
                alt Call Succeeds
                    LLM-->>Worker: 200 OK Generation Output
                else 429 Rate Limited / 503 Provider Outage
                    Worker->>Worker: Record Failure & Trip to Next Fallback Model
                end
            end
        end

        Worker->>Kafka: Publish to 'response-processing'
        Kafka->>RP: Consume Completion Event
        RP->>RP: Scrub PII (Emails, SSNs, CCs) & Compute Token Financials
        par Multi-Tier Persistence
            RP->>DB: Write Relational Record (Postgres) & Raw Blob (Azure Blob)
            RP->>Redis: Set Key-Value Response Cache (TTL 12h)
        end
        RP->>Kafka: Publish to 'inference-responses'
        Kafka->>GW: Notify Ingress Gateway
        GW-->>User: Client Receives Final Event / Polling Resolution
    end
```

---

## Deep-Dive Systems Design

### Intelligent LLM Router (Cost • Quality • Latency)
The router dynamically optimizes model selection using a weighted multi-criteria decision matrix:

$$\text{Score} = w_c \cdot C_{\text{est}} + w_l \cdot L_{\text{p99}} + w_q \cdot (1 - Q_{\text{model}}) + w_a \cdot (1 - A_{\text{health}})$$

* **`AUTO` Mode**: Evaluates prompt linguistic complexity and semantic token length. Simple queries (e.g., sentiment analysis, classification) route to high-speed, cost-efficient models; complex multi-step reasoning queries dynamically route to Frontier models.
* **`COST` Mode**: Aggressively minimizes token expenditure by selecting the lowest cost-per-token provider matching minimum quality thresholds.
* **`QUALITY` Mode**: Directs traffic to Tier-1 models (e.g., GPT-4o, Claude 3.5 Sonnet) prioritized for factual reasoning and instruction-following.
* **`LATENCY` Mode**: Prioritizes lowest Time-To-First-Token (TTFT) and P99 latency models (e.g., Llama 3 70B, Mistral Small).

### Distributed Gateway & Rate Limiting
* **Authentication & Authorization**: Validates incoming OAuth2/OIDC JWT tokens, extracting tenant identities, user scopes, and subscription entitlement tiers.
* **Token Bucket Algorithm**: Distributed, low-latency rate limiting enforced via Redis hashes. Prevents noisy-neighbor saturation by throttling on both:
  * **RPM (Requests Per Minute)**
  * **TPM (Tokens Per Minute)**

### Idempotency & Lifecycle Engine
* **Cryptographic Request Fingerprinting**: Generates deterministic SHA-256 digests over `userId + prompt + modelParameters + idempotencyKey`.
* **Zero-Compute Short Circuiting**: If an identical request was fulfilled within the deduplication window, ScaleGen bypasses the LLM compute grid entirely, returning the cached payload in $<5\text{ms}$.
* **Guardrail & Prompt Enrichment**: Sanitizes input schemas, bounds generation limits (`max_tokens`, `temperature`, `top_p`), and injects organization system directives.

### Resilience Circuitry & DLQ Cascading
* **Circuit Breaker State Machine**: Monitors rolling error rates per model endpoint. When failures exceed threshold ($>3$ consecutive or $>30\%$ window), the breaker transitions to `OPEN`, immediately shielding the failing provider and diverting traffic to fallbacks.
* **Exponential Backoff with Full Jitter**: Retries transient $429$ (rate-limited) and $5xx$ errors with randomized backoff delays:
  $$t_{\text{sleep}} = \text{random}(0, \min(t_{\text{max}}, t_{\text{base}} \cdot 2^{\text{attempt}}))$$
* **Active Fallback Cascading**: If Model A fails or times out, the engine seamlessly invokes Model B $\rightarrow$ Model C in real-time while annotating telemetry logs with the full fallback traversal.

### PII Sanitization & Multi-Tier Persistence
* **PII Redaction Engine**: High-performance regex pipeline scrubbing sensitive data (Emails, Social Security Numbers, Credit Card patterns, IPv4 addresses) from completion streams before client delivery.
* **Tiered Storage Architecture**:
  * **PostgreSQL**: ACID-compliant transactional store for audit trails, token accounting, latency breakdowns, and request tracking.
  * **Redis**: Microsecond in-memory key-value cache for hot idempotency keys and frequent prompt/response pairs.
  * **Azure Blob Storage**: Scalable cloud object storage for heavy raw payloads, multi-modal artifacts, and compliance cold logs.

---

## Microservices Catalog & Network Matrix

| Service | Port | Technology | Primary Functionality |
| :--- | :--- | :--- | :--- |
| **`api-gateway`** | `8080` | Spring Boot 3, Spring Data JPA, Redis, Kafka | Edge ingress, authentication verification, request tracking, metric aggregation |
| **`orchestrator`** | `8081` | Spring Boot 3, Kafka, Redis | Schema validation, SHA-256 idempotency cache check, context enrichment |
| **`inference-worker`** | `8082` | Spring Boot 3, Kafka, OkHttp3, Resilience Engine | Intelligent routing, multi-provider invocation, circuit breakers, fallback cascading |
| **`response-processor`** | `8083` | Spring Boot 3, Kafka, Redis | PII scrubbing, financial token accounting, multi-tier storage dispatch |
| **`payment-service`** | `8084` | Spring Boot 3, Stripe SDK, PostgreSQL | Credit balance management, Stripe checkout integration, webhook handling |
| **`frontend`** | `3000` | React 18, Axios, Stripe Elements | Enterprise dashboard, live router playground, telemetry & audit viewer |

---

## Intelligent Routing Engine Matrix

| Model Tier | Model Target | Provider | Cost / 1k Input | Cost / 1k Output | Avg P99 Latency | Quality Rating | Target Use Case |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Model A** | `llama-3-70b` | Meta / Ollama | $\$0.0008$ | $\$0.0020$ | $180\text{ ms}$ | $88\%$ | High-volume summarization, fast Q&A |
| **Model B** | `claude-3-5-sonnet` | Anthropic | $\$0.0030$ | $\$0.0120$ | $320\text{ ms}$ | $95\%$ | Code generation, nuanced text synthesis |
| **Model C** | `gpt-4o` | OpenAI | $\$0.0050$ | $\$0.0150$ | $450\text{ ms}$ | $98\%$ | Complex reasoning, mathematics, architecture |

---

## API Specification & Examples

### 1. Submit Inference Request
```bash
curl -X POST http://localhost:8080/api/v1/inference \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Explain distributed idempotency patterns in event-driven systems.",
    "routingStrategy": "AUTO",
    "model": "gpt-4o",
    "idempotencyKey": "idem-uuid-98421",
    "userId": "usr-enterprise-42",
    "tenantId": "org-fintech",
    "parameters": {
      "systemPrompt": "Be concise, formal, and precise.",
      "temperature": 0.3
    }
  }'
```

**Response (`202 Accepted`):**
```json
{
  "requestId": "req-9b84a210",
  "prompt": "Explain distributed idempotency patterns in event-driven systems.",
  "model": "gpt-4o",
  "routingStrategy": "AUTO",
  "idempotencyKey": "idem-uuid-98421",
  "userId": "usr-enterprise-42",
  "tenantId": "org-fintech",
  "traceId": "trace-8f7d92a104",
  "spanId": "span-b291c3",
  "createdAt": "2026-09-20T10:30:00"
}
```

### 2. Query Request Execution & Telemetry Status
```bash
curl -X GET http://localhost:8080/api/v1/inference/req-9b84a210
```

**Response (`200 OK`):**
```json
{
  "requestId": "req-9b84a210",
  "status": "COMPLETED",
  "selectedModel": "gpt-4o",
  "response": "Distributed idempotency ensures that duplicate messages or requests produce the exact same outcome without side effects. Standard enterprise implementations combine unique request fingerprints (e.g. SHA-256) cached in distributed stores (e.g. Redis) with transactional database deduplication.",
  "fallbackChain": "GPT-4o (Model A)",
  "processingTimeMs": 342,
  "promptTokens": 18,
  "completionTokens": 54,
  "costUsd": 0.0009,
  "traceId": "trace-8f7d92a104",
  "createdAt": "2026-09-20T10:30:00",
  "updatedAt": "2026-09-20T10:30:01"
}
```

### 3. Real-Time Observability & Metrics
```bash
curl -X GET http://localhost:8080/api/v1/inference/metrics
```

**Response (`200 OK`):**
```json
{
  "totalRequests": 1420,
  "completedRequests": 1408,
  "failedRequests": 12,
  "totalCostUsd": 1.48201,
  "avgLatencyMs": 284,
  "openTelemetryCollector": "CONNECTED",
  "jaegerTracing": "ACTIVE",
  "prometheusMetrics": "EXPORTING"
}
```

---

## Local Development Quickstart

### Prerequisites
* **Java 17 (JDK 17 LTS)**
* **Maven 3.8+**
* **Node.js 18+ & npm**
* **Docker & Docker Compose**

### 1. Boot Infrastructure Services
Launch PostgreSQL, Redis, Kafka, Zookeeper, Jaeger, Prometheus, and Grafana:
```bash
docker-compose up -d
```

Verify running containers:
```bash
docker-compose ps
```

### 2. Build Backend Artifacts
Compile and package the parent reactor:
```bash
mvn clean compile
```

### 3. Launch Microservices (Run in separate terminal tabs)
```bash
# Terminal 1: API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 2: Orchestrator Service
cd orchestrator && mvn spring-boot:run

# Terminal 3: Inference Worker Pool (Supports mock fallback if no API key is provided)
cd inference-worker && mvn spring-boot:run

# Terminal 4: Response Processor Service
cd response-processor && mvn spring-boot:run

# Terminal 5: Payment Service (Optional)
cd payment-service && mvn spring-boot:run
```

### 4. Launch React Frontend
```bash
cd frontend
npm install
npm start
```
Open **`http://localhost:3000`** in your browser to access the interactive dashboard.

---

## Production Deployment (Azure AKS)

ScaleGen includes cloud-native Kubernetes deployment descriptors under [`k8s/`](./k8s):

```
k8s/
├── api-gateway.yml
├── orchestrator.yml
├── inference-worker.yml
├── response-processor.yml
├── kafka.yml
├── postgres.yml
└── redis.yml
```

### Deploy to Azure Kubernetes Service:
```bash
# 1. Connect to your AKS cluster
az aks get-credentials --resource-group <rg-name> --name <cluster-name>

# 2. Apply Namespace and Configurations
kubectl apply -f k8s/postgres.yml
kubectl apply -f k8s/redis.yml
kubectl apply -f k8s/kafka.yml

# 3. Deploy ScaleGen Workloads
kubectl apply -f k8s/api-gateway.yml
kubectl apply -f k8s/orchestrator.yml
kubectl apply -f k8s/inference-worker.yml
kubectl apply -f k8s/response-processor.yml

# 4. Verify Pod Status & HPA Autoscalers
kubectl get pods -w
```

---

## Repository Structure

```
ScaleGen/
├── .github/
│   └── workflows/
│       └── ci.yml            # Automated GitHub Actions CI/CD Pipeline
├── api-gateway/              # Spring Cloud Ingress Gateway, JWT Auth & Rate Limiter
├── orchestrator/             # Schema Validation, Guardrails & SHA-256 Idempotency Engine
├── inference-worker/         # Dynamic Router, Circuit Breaker & Multi-LLM Provider Grid
├── response-processor/       # PII Scrubbing, Token Accounting & Multi-Tier Persistence
├── payment-service/          # Credit Balances & Stripe Checkout Integration
├── common/                   # Shared DTOs, Kafka Constants, Token Usage Analytics
├── frontend/                 # React 18 SPA Control Panel & Real-Time Telemetry Monitor
├── k8s/                      # Production Kubernetes Deployment & Service Manifests
├── docker-compose.yml        # Full Local Infrastructure Stack (Postgres, Redis, Kafka, OTel)
├── pom.xml                   # Master Maven Reactor POM
└── README.md                 # Technical Architecture Documentation
```

---

## License

ScaleGen is open-source software licensed under the [Apache License 2.0](LICENSE).
