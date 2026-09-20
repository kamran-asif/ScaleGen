# Scalable GenAI Orchestration Platform

A microservices-based platform for orchestrating GenAI inference requests using Spring Boot, Kafka, PostgreSQL, and Redis.

## Architecture Overview

```
                         React.js
                            │
                            ▼
                 Spring Cloud Gateway
                 Auth • Rate Limit
                            │
                            ▼
                     Orchestrator
              Validation • Idempotency
                            │
                            ▼
                          Kafka
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
          Worker 1       Worker 2       Worker N
             └──────────────┼──────────────┘
                            ▼
                   Intelligent Router
                  Cost • Quality • Latency
                       /     |      \
                      ▼      ▼       ▼
                   Model A Model B Model C
                      \      |       /
                       └──────┼─────┘
                              ▼
                         LLM Provider
                              │
                    ┌─────────┴─────────┐
                    │ Retry             │
                    │ Fallback          │
                    │ Circuit Breaker   │
                    │ DLQ               │
                    └─────────┬─────────┘
                              ▼
                     Response Processor
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
         PostgreSQL         Redis        Azure Blob
                             
                 ─── Observability ───
                              │
                     OpenTelemetry
                              │
          ┌───────────────────┼──────────────────┐
          ▼                   ▼                  ▼
       Jaeger             Prometheus         OpenSearch
       Traces              Metrics               Logs
          └───────────────────┼──────────────────┘
                              ▼
                           Grafana

                 ─── Cloud Infrastructure ───
                              │
                         Azure AKS
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
                   ACR             Key Vault
```

## Services

### Backend
1. **API Gateway (8080)**: Accepts inference requests, tracks them in PostgreSQL, caches in Redis
2. **Orchestrator (8081)**: Enhances prompts and routes to inference workers
3. **Inference Worker (8082)**: Calls OpenAI API, handles retries, DLQ for failures
4. **Response Processor (8083)**: Cleans and formats responses
5. **Payment Service (8084)**: Stripe payment integration, credit management

### Frontend
- **React App (3000)**: User interface for submitting requests, buying credits, and checking status

## Technologies

- **Spring Boot 3.2**: Microservices framework
- **React 18**: Frontend framework
- **Apache Kafka**: Async message broker
- **PostgreSQL**: Request tracking and payment storage
- **Redis**: Caching layer
- **Stripe**: Payment processing
- **Docker & Kubernetes**: Containerization and orchestration

## Getting Started

### Prerequisites

- Java 17
- Maven 3.8+
- Docker & Docker Compose
- Stripe account (for payment testing)

### Local Development

1. Start infrastructure services:
```bash
docker-compose up -d
```

2. Build all services:
```bash
mvn clean install
```

3. Set up environment variables:

**For Payment Service:**
- Get your Stripe keys from https://dashboard.stripe.com/test/apikeys
- Set `STRIPE_SECRET_KEY` environment variable

**For Inference Worker:**
- Set `OPENAI_API_KEY` environment variable

4. Run backend services in separate terminals:

```bash
# API Gateway
cd api-gateway && mvn spring-boot:run

# Orchestrator
cd orchestrator && mvn spring-boot:run

# Inference Worker (set OPENAI_API_KEY first)
$env:OPENAI_API_KEY="your-key-here"; cd inference-worker; mvn spring-boot:run

# Response Processor
cd response-processor && mvn spring-boot:run

# Payment Service (set STRIPE_SECRET_KEY first)
$env:STRIPE_SECRET_KEY="sk_test_your_secret_key"; cd payment-service; mvn spring-boot:run
```

5. Run frontend:
```bash
cd frontend
npm install
npm start
```

**Frontend Setup:**
- Open `frontend/src/App.js` and replace `pk_test_your_publishable_key_here` with your Stripe publishable key
- The frontend will be available at http://localhost:3000

### Payment Testing

Use Stripe test cards to test payments:
- Success: `4242 4242 4242 4242`
- Card declined: `4000 0000 0000 0002`
- Require authentication: `4000 0025 0000 3155`

### Pricing

- 10 Credits: $5.00
- 50 Credits: $20.00 (20% off)
- 100 Credits: $35.00 (30% off)

Each AI request costs 1 credit.

## API Usage

### Submit Inference Request
```bash
curl -X POST http://localhost:8080/api/v1/inference \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Explain microservices",
    "model": "gpt-3.5-turbo",
    "parameters": {
      "tone": "friendly",
      "maxLength": 500
    },
    "userId": "user123"
  }'
```

### Get Request Status
```bash
curl http://localhost:8080/api/v1/inference/{requestId}
```

## Project Structure

```
genai-orchestration-platform/
├── api-gateway/          # API Gateway service
├── orchestrator/         # Orchestrator service
├── inference-worker/     # Inference Worker service
├── response-processor/   # Response Processor service
├── common/               # Shared DTOs and utilities
├── frontend/             # React frontend application
├── k8s/                  # Kubernetes manifests
├── docker-compose.yml    # Local development stack
└── pom.xml               # Parent Maven pom
```
