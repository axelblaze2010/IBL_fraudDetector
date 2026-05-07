# Mule Fraud Detector

This package contains an aligned Spring Boot + MongoDB implementation of the Mule Account Network Detector design.

## Modules

- `springboot-fraud-service` — Spring Boot REST API with MongoDB, Redis cache, rule scoring, graph traversal, fraud cases, and FastAPI ML integration.
- `ml-service` — Python FastAPI service for ML/anomaly risk scoring.
- `docker-compose.yml` — local MongoDB + Redis setup.

## Implemented backend logic

- `POST /api/fraud/check-mule`
  - Saves transaction
  - Updates account/account_links graph
  - Invalidates Redis cache through Spring async event
  - Runs MongoDB `$graphLookup` for circular-flow and network traversal
  - Computes velocity, graph-depth, behavior, and device scores
  - Calls Python FastAPI ML service
  - Combines rule score + ML score
  - Saves fraud score
  - Creates fraud case for CRITICAL risk

- `GET /api/fraud/risk-score/{accountId}`
  - Returns latest saved risk score

- `POST /api/fraud/report-false-positive`
  - Marks fraud case as `FALSE_POSITIVE`
  - Invalidates Redis cache

- `GET /api/fraud/cases?status=OPEN`
  - Lists fraud cases by status

- `PATCH /api/fraud/cases/{referenceId}/resolve`
  - Marks fraud case as `RESOLVED`
  - Saves resolver and resolution note
  - Invalidates Redis cache

## Run infra

```bash
docker-compose up -d
```

## Run Python ML service

```bash
cd ml-service
python -m venv venv
# Windows: venv\Scripts\activate
source venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

## Run Spring Boot service

```bash
cd springboot-fraud-service
gradle bootRun
```

If you use IntelliJ/Eclipse, import `springboot-fraud-service` as a Gradle project and run `FraudDetectionApplication`.

## Test check-mule API

```bash
curl --location 'http://localhost:8080/api/fraud/check-mule' \
--header 'Content-Type: application/json' \
--data '{
  "fromAccountId": "ACC1001",
  "toAccountId": "ACC2002",
  "amount": 50000,
  "transactionType": "UPI",
  "deviceId": "device-abc-123",
  "sessionId": "session-001"
}'
```

## Test latest risk score

```bash
curl --location 'http://localhost:8080/api/fraud/risk-score/ACC2002'
```

## Test fraud cases

```bash
curl --location 'http://localhost:8080/api/fraud/cases?status=OPEN'
```

## Report false positive

```bash
curl --location 'http://localhost:8080/api/fraud/report-false-positive' \
--header 'Content-Type: application/json' \
--data '{
  "referenceId": "FRAUD-20260507-ABCDEFGH",
  "reportedBy": "user-123",
  "reason": "I know this recipient and this was legitimate"
}'
```

## Resolve fraud case

```bash
curl --location --request PATCH 'http://localhost:8080/api/fraud/cases/FRAUD-20260507-ABCDEFGH/resolve' \
--header 'Content-Type: application/json' \
--data '{
  "resolvedBy": "ops-user-1",
  "resolutionNote": "Reviewed transaction chain and confirmed fraud"
}'
```

## Notes

- The ML service is intentionally lightweight for hackathon/demo usage. Replace its internals with Isolation Forest, XGBoost, or a trained model later.
- MongoDB graph traversal is implemented using `$graphLookup` in `MuleGraphService`.
- Redis key format is `risk:account:{accountId}` with 1-hour TTL.
