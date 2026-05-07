# Mule Fraud Detector - API Documentation

## Base URLs

- **Spring Boot Fraud Service**: `http://localhost:8080`
- **ML Risk Service**: `http://localhost:8000`

---

## Spring Boot Fraud Detection APIs

### Base Path: `/api/fraud`

---

## 1. Check Mule Risk (Transaction Fraud Detection)

**Endpoint:** `POST /api/fraud/check-mule`

**Description:** Analyzes a transaction for mule fraud risk. This endpoint:
- Saves the transaction to MongoDB
- Updates account and account-link graphs
- Computes rule-based fraud scores (velocity, graph-depth, behavior, device)
- Calls the ML service for anomaly scoring
- Combines both scores (60% rule score + 40% ML score)
- Creates a fraud case if risk is CRITICAL

**Request:**
```json
{
  "fromAccountId": "ACC1001",
  "toAccountId": "ACC2002",
  "amount": 50000,
  "transactionType": "UPI",
  "deviceId": "device-abc-123",
  "sessionId": "session-001"
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| fromAccountId | String | Yes | Sender's account identifier |
| toAccountId | String | Yes | Receiver's account identifier |
| amount | BigDecimal | Yes | Transaction amount |
| transactionType | String | Yes | Type of transaction (e.g., UPI, NEFT, RTGS) |
| deviceId | String | Yes | Device identifier (device hash stored in DB) |
| sessionId | String | Yes | Session identifier for correlation |

**Response:**
```json
{
  "accountId": "ACC2002",
  "riskScore": 75,
  "riskLevel": "HIGH",
  "action": "STEP_UP_AUTH",
  "ruleScore": 55,
  "mlScore": 85,
  "referenceId": "FRAUD-20260507-ABCDEFGH",
  "reasons": [
    "High transaction velocity: more than 10 incoming transactions in last 1 hour",
    "Fan-out pattern detected: account sent to 5+ accounts within 10 minutes",
    "ML signal: account behaves like pass-through money pipe"
  ],
  "recommendation": "Require biometric or OTP step-up"
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| accountId | String | Receiver's account ID |
| riskScore | Integer | Final combined risk score (0-100) |
| riskLevel | String | Risk classification: LOW ≤30, MEDIUM 31-60, HIGH 61-80, CRITICAL >80 |
| action | String | Recommended action: ALLOW, WARN, STEP_UP_AUTH, BLOCK |
| ruleScore | Integer | Rule-based score (0-100) |
| mlScore | Integer | ML-based anomaly score (0-100) |
| referenceId | String | Fraud case reference ID (only if CRITICAL) |
| reasons | Array | List of fraud detection reasons |
| recommendation | String | User-friendly recommendation text |

**Risk Levels & Actions:**
| Risk Level | Score Range | Action |
|-----------|-------------|--------|
| LOW | ≤ 30 | ALLOW - Allow transaction |
| MEDIUM | 31 - 60 | WARN - Show warning before proceeding |
| HIGH | 61 - 80 | STEP_UP_AUTH - Require biometric or OTP step-up |
| CRITICAL | > 80 | BLOCK - Block transaction and create fraud case |

**cURL Example:**
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

**HTTP Status Codes:**
- `200 OK` - Transaction analyzed successfully
- `400 Bad Request` - Invalid request parameters
- `500 Internal Server Error` - Server error during processing

---

## 2. Get Latest Risk Score

**Endpoint:** `GET /api/fraud/risk-score/{accountId}`

**Description:** Retrieves the latest fraud risk score and detailed breakdown for a specific account.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| accountId | String | Yes | Account identifier |

**Response:**
```json
{
  "id": "635f7c5c5c5c5c5c5c5c5c5c",
  "accountId": "ACC2002",
  "riskScore": 75,
  "riskLevel": "HIGH",
  "ruleScore": 55,
  "mlScore": 85,
  "velocityScore": 25,
  "graphDepthScore": 15,
  "behaviorScore": 10,
  "deviceScore": 5,
  "reasons": [
    "High transaction velocity: more than 10 incoming transactions in last 1 hour",
    "Fan-out pattern detected: account sent to 5+ accounts within 10 minutes"
  ],
  "computedAt": "2026-05-07T14:30:45"
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| id | String | Database document ID |
| accountId | String | Account identifier |
| riskScore | Integer | Final combined risk score |
| riskLevel | String | Risk classification |
| ruleScore | Integer | Rule-based score |
| mlScore | Integer | ML-based anomaly score |
| velocityScore | Integer | Score based on transaction velocity |
| graphDepthScore | Integer | Score based on network depth analysis |
| behaviorScore | Integer | Score based on account behavior patterns |
| deviceScore | Integer | Score based on device clustering patterns |
| reasons | Array | Detected fraud indicators |
| computedAt | DateTime | Timestamp of computation |

**cURL Example:**
```bash
curl --location 'http://localhost:8080/api/fraud/risk-score/ACC2002'
```

**HTTP Status Codes:**
- `200 OK` - Risk score retrieved successfully
- `404 Not Found` - No fraud score found for account
- `500 Internal Server Error` - Server error

---

## 3. Get Fraud Cases

**Endpoint:** `GET /api/fraud/cases`

**Description:** Lists fraud cases filtered by status.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| status | String | OPEN | Fraud case status: OPEN, RESOLVED, FALSE_POSITIVE |

**Response:**
```json
[
  {
    "id": "635f7c5c5c5c5c5c5c5c5c5d",
    "referenceId": "FRAUD-20260507-ABCDEFGH",
    "accountId": "ACC2002",
    "riskScore": 85,
    "action": "BLOCK",
    "reasons": [
      "High transaction velocity: more than 10 incoming transactions in last 1 hour",
      "Circular money flow detected within 5 hops"
    ],
    "status": "OPEN",
    "resolvedBy": null,
    "resolutionNote": null,
    "falsePositiveReason": null,
    "reportedBy": null,
    "createdAt": "2026-05-07T14:30:45",
    "resolvedAt": null,
    "falsePositiveReportedAt": null
  },
  {
    "id": "635f7c5c5c5c5c5c5c5c5c5e",
    "referenceId": "FRAUD-20260506-IJKLMNOP",
    "accountId": "ACC3003",
    "riskScore": 92,
    "action": "BLOCK",
    "reasons": ["Pass-through ratio is above 90%"],
    "status": "RESOLVED",
    "resolvedBy": "ops-user-1",
    "resolutionNote": "Confirmed fraud pattern. Account blocked.",
    "falsePositiveReason": null,
    "reportedBy": null,
    "createdAt": "2026-05-06T10:15:30",
    "resolvedAt": "2026-05-06T15:45:20",
    "falsePositiveReportedAt": null
  }
]
```

**Response Fields (FraudCase Array):**
| Field | Type | Description |
|-------|------|-------------|
| id | String | Database document ID |
| referenceId | String | Unique fraud case reference |
| accountId | String | Associated account ID |
| riskScore | Integer | Risk score when case was created |
| action | String | Action taken (BLOCKED) |
| reasons | Array | Fraud detection reasons |
| status | String | Case status: OPEN, RESOLVED, FALSE_POSITIVE |
| resolvedBy | String | Who resolved the case (if applicable) |
| resolutionNote | String | Resolver's notes |
| falsePositiveReason | String | Reason if reported as false positive |
| reportedBy | String | Who reported the false positive |
| createdAt | DateTime | When case was created |
| resolvedAt | DateTime | When case was resolved |
| falsePositiveReportedAt | DateTime | When reported as false positive |

**cURL Examples:**
```bash
# Get all open fraud cases
curl --location 'http://localhost:8080/api/fraud/cases?status=OPEN'

# Get all resolved fraud cases
curl --location 'http://localhost:8080/api/fraud/cases?status=RESOLVED'

# Get all false positive reports
curl --location 'http://localhost:8080/api/fraud/cases?status=FALSE_POSITIVE'
```

**HTTP Status Codes:**
- `200 OK` - Cases retrieved successfully
- `500 Internal Server Error` - Server error

---

## 4. Report False Positive

**Endpoint:** `POST /api/fraud/report-false-positive`

**Description:** Reports a fraud case as a false positive (legitimate transaction). Updates case status and invalidates Redis cache.

**Request:**
```json
{
  "referenceId": "FRAUD-20260507-ABCDEFGH",
  "reportedBy": "user-123",
  "reason": "I know this recipient and this was legitimate"
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| referenceId | String | Yes | Fraud case reference ID to report |
| reportedBy | String | No | User ID or name who reported |
| reason | String | No | Explanation for false positive report |

**Response:**
```json
{
  "id": "635f7c5c5c5c5c5c5c5c5c5c",
  "referenceId": "FRAUD-20260507-ABCDEFGH",
  "accountId": "ACC2002",
  "riskScore": 75,
  "action": "BLOCK",
  "reasons": [
    "High transaction velocity: more than 10 incoming transactions in last 1 hour"
  ],
  "status": "FALSE_POSITIVE",
  "resolvedBy": null,
  "resolutionNote": null,
  "falsePositiveReason": "I know this recipient and this was legitimate",
  "reportedBy": "user-123",
  "createdAt": "2026-05-07T14:30:45",
  "resolvedAt": null,
  "falsePositiveReportedAt": "2026-05-07T14:35:10"
}
```

**cURL Example:**
```bash
curl --location 'http://localhost:8080/api/fraud/report-false-positive' \
--header 'Content-Type: application/json' \
--data '{
  "referenceId": "FRAUD-20260507-ABCDEFGH",
  "reportedBy": "user-123",
  "reason": "I know this recipient and this was legitimate"
}'
```

**HTTP Status Codes:**
- `200 OK` - False positive reported successfully
- `400 Bad Request` - Invalid reference ID or missing required fields
- `404 Not Found` - Fraud case not found
- `500 Internal Server Error` - Server error

---

## 5. Resolve Fraud Case

**Endpoint:** `PATCH /api/fraud/cases/{referenceId}/resolve`

**Description:** Marks a fraud case as resolved by operations team. Updates case status, resolver info, and invalidates Redis cache.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| referenceId | String | Yes | Fraud case reference ID |

**Request:**
```json
{
  "resolvedBy": "ops-user-1",
  "resolutionNote": "Reviewed transaction chain and confirmed fraud. Account blocked."
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| resolvedBy | String | Yes | User ID/name of operations person |
| resolutionNote | String | No | Notes about the resolution |

**Response:**
```json
{
  "id": "635f7c5c5c5c5c5c5c5c5c5c",
  "referenceId": "FRAUD-20260507-ABCDEFGH",
  "accountId": "ACC2002",
  "riskScore": 75,
  "action": "BLOCK",
  "reasons": [
    "High transaction velocity: more than 10 incoming transactions in last 1 hour"
  ],
  "status": "RESOLVED",
  "resolvedBy": "ops-user-1",
  "resolutionNote": "Reviewed transaction chain and confirmed fraud. Account blocked.",
  "falsePositiveReason": null,
  "reportedBy": null,
  "createdAt": "2026-05-07T14:30:45",
  "resolvedAt": "2026-05-07T15:45:20",
  "falsePositiveReportedAt": null
}
```

**cURL Example:**
```bash
curl --location --request PATCH 'http://localhost:8080/api/fraud/cases/FRAUD-20260507-ABCDEFGH/resolve' \
--header 'Content-Type: application/json' \
--data '{
  "resolvedBy": "ops-user-1",
  "resolutionNote": "Reviewed transaction chain and confirmed fraud. Account blocked."
}'
```

**HTTP Status Codes:**
- `200 OK` - Fraud case resolved successfully
- `400 Bad Request` - Missing required fields
- `404 Not Found` - Fraud case not found
- `500 Internal Server Error` - Server error

---

## ML Risk Service APIs

### Base Path: `/`

---

## 1. Health Check

**Endpoint:** `GET /health`

**Description:** Checks if the ML service is running and healthy.

**Response:**
```json
{
  "status": "UP"
}
```

**cURL Example:**
```bash
curl --location 'http://localhost:8000/health'
```

**HTTP Status Codes:**
- `200 OK` - Service is healthy

---

## 2. Predict Risk (ML Scoring)

**Endpoint:** `POST /ml/predict-risk`

**Description:** Analyzes account features and returns ML-based fraud probability and anomaly score. This endpoint is called internally by the Spring Boot service. It uses rule-based logic to evaluate:
- Transaction velocity patterns
- Pass-through behavior (money laundering pattern)
- Device clustering (multiple devices)
- Circular flow detection
- Fan-out patterns (broadcasting money to multiple accounts)
- Legitimate credit history

**Request:**
```json
{
  "txnCountLast1Hour": 15,
  "txnCountLast24Hours": 45,
  "amountReceivedLast24Hours": 500000.50,
  "amountSentLast24Hours": 480000.25,
  "passThroughRatio": 0.95,
  "avgForwardingTime": 2.5,
  "uniqueSendersCount": 8,
  "uniqueReceiversCount": 12,
  "linkedDeviceCount": 5,
  "accountAgeDays": 30,
  "inactiveDaysBeforeTxn": 2,
  "hopDistanceFromFraudNode": 2,
  "circularFlowDetected": true,
  "fanOutCount": 7,
  "hasLegitimateCredits": false
}
```

**Request Fields:**
| Field | Type | Description |
|-------|------|-------------|
| txnCountLast1Hour | Integer | Number of transactions received in last hour |
| txnCountLast24Hours | Integer | Number of transactions received in last 24 hours |
| amountReceivedLast24Hours | Float | Total amount received in last 24 hours (in INR) |
| amountSentLast24Hours | Float | Total amount sent in last 24 hours (in INR) |
| passThroughRatio | Float | Ratio of forwarded to received amount (0-1) |
| avgForwardingTime | Float | Average time between receiving and sending money (minutes) |
| uniqueSendersCount | Integer | Number of unique senders to this account |
| uniqueReceiversCount | Integer | Number of unique receivers from this account |
| linkedDeviceCount | Integer | Number of different devices associated with account |
| accountAgeDays | Integer | Days since account creation |
| inactiveDaysBeforeTxn | Integer | Days since last activity before this transaction |
| hopDistanceFromFraudNode | Integer | Graph distance to known fraud accounts |
| circularFlowDetected | Boolean | Whether circular money flow pattern detected |
| fanOutCount | Integer | Number of accounts this account sent to within 10 minutes |
| hasLegitimateCredits | Boolean | Whether account has legitimate salary/merchant credits |

**Response:**
```json
{
  "mlScore": 95,
  "fraudProbability": 0.95,
  "mlReasons": [
    "ML signal: unusually high transaction count in last 1 hour",
    "ML signal: account behaves like pass-through money pipe",
    "ML signal: device pattern resembles mule cluster",
    "ML signal: circular transaction flow detected",
    "ML signal: fan-out transaction behaviour detected",
    "ML signal: no legitimate credit history found"
  ]
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| mlScore | Integer | ML anomaly score (0-100, higher = more suspicious) |
| fraudProbability | Float | Fraud probability (0-1, decimal representation of score/100) |
| mlReasons | Array | List of ML-detected fraud signals |

**cURL Example:**
```bash
curl --location 'http://localhost:8000/ml/predict-risk' \
--header 'Content-Type: application/json' \
--data '{
  "txnCountLast1Hour": 15,
  "txnCountLast24Hours": 45,
  "amountReceivedLast24Hours": 500000.50,
  "amountSentLast24Hours": 480000.25,
  "passThroughRatio": 0.95,
  "avgForwardingTime": 2.5,
  "uniqueSendersCount": 8,
  "uniqueReceiversCount": 12,
  "linkedDeviceCount": 5,
  "accountAgeDays": 30,
  "inactiveDaysBeforeTxn": 2,
  "hopDistanceFromFraudNode": 2,
  "circularFlowDetected": true,
  "fanOutCount": 7,
  "hasLegitimateCredits": false
}'
```

**HTTP Status Codes:**
- `200 OK` - Risk prediction successful
- `400 Bad Request` - Invalid request parameters
- `422 Unprocessable Entity` - Validation error in request data
- `500 Internal Server Error` - Server error

---

## Scoring Breakdown

### Rule-Based Scoring (Spring Boot Service)

The Spring Boot service computes four component scores:

1. **Velocity Score (0-25 points)**
   - \>10 transactions in 1 hour: +25 points
   - \>5 transactions in 1 hour: +15 points
   - Otherwise: +5 points

2. **Graph Depth Score (0-25 points)**
   - Based on MongoDB `$graphLookup` analysis
   - Detects network depth to known fraud nodes
   - Circular flow detection adds +25 points

3. **Behavior Score (0-25 points)**
   - Pass-through ratio \>90%: +25 points
   - Pass-through ratio \>70%: +15 points
   - No legitimate credits: +10 points

4. **Device Score (0-25 points)**
   - Based on device linking patterns
   - Multiple devices indicate potential mule network

**Final Rule Score = minimum(100, sum of component scores)**

### ML Scoring (Python Service)

The ML service adds additional signals:
- High velocity patterns
- Pass-through behavior
- Device clustering
- Circular flows
- Fan-out patterns
- Legitimate credit history

**Final ML Score = minimum(100, sum of detected signals)**

### Combined Risk Score

**Final Risk Score = (Rule Score × 0.60) + (ML Score × 0.40)**

---

## Error Handling

### Common Error Responses

**400 Bad Request:**
```json
{
  "error": "Invalid request parameters",
  "message": "fromAccountId is required"
}
```

**404 Not Found:**
```json
{
  "error": "Resource not found",
  "message": "Fraud case with reference FRAUD-12345 not found"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Internal server error",
  "message": "Unable to connect to MongoDB"
}
```

---

## Authentication & Rate Limiting

Currently, the APIs do **not** require authentication or have rate limiting. In a production environment, consider adding:
- OAuth 2.0 or JWT token authentication
- Rate limiting (e.g., 100 requests/minute per user)
- Request signing with HMAC-SHA256

---

## Caching

- **Redis Cache**: Risk scores are cached with a 1-hour TTL
- **Cache Key Format**: `risk:account:{accountId}`
- **Cache Invalidation**: Triggered on:
  - New transaction processed
  - False positive reported
  - Fraud case resolved

---

## Data Storage

### MongoDB Collections

- **fraud_db.transactions** - Transaction records with source, destination, amount, device hash
- **fraud_db.accounts** - Account profiles with metadata
- **fraud_db.account_links** - Graph edges representing money flows
- **fraud_db.fraud_scores** - Computed risk scores with component breakdown
- **fraud_db.fraud_cases** - Fraud alerts and case management

### Indexes
- `transactions.toAccountId`
- `transactions.timestamp`
- `fraud_scores.accountId`
- `fraud_cases.status`
- `fraud_cases.referenceId` (unique)

---

## Integration Flow

```
Client POST /api/fraud/check-mule
    ↓
Spring Boot saves transaction
    ↓
Updates account & account_links graphs
    ↓
Computes rule-based scores
    ↓
Calls ML service POST /ml/predict-risk
    ↓
Combines scores (60% rule + 40% ML)
    ↓
Determines risk level & action
    ↓
Caches result in Redis
    ↓
If CRITICAL: Creates fraud case
    ↓
Response to client
```

---

## Testing Workflow

1. **Setup**: Run Docker Compose, ML service, and Spring Boot service
2. **Create Transaction**: `POST /api/fraud/check-mule` with test account IDs
3. **Check Score**: `GET /api/fraud/risk-score/{accountId}`
4. **View Cases**: `GET /api/fraud/cases?status=OPEN`
5. **Manage Cases**: Report false positives or resolve as needed

---

## Version History

- **v1.0** (2026-05-07): Initial implementation with Spring Boot + MongoDB + Redis + FastAPI ML
- Lightweight ML service for hackathon/demo usage
- MongoDB `$graphLookup` for graph analysis
- Async cache invalidation via Spring events


