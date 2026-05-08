# How to Connect to MongoDB and Insert Data

## Connection Details

- **Host**: localhost
- **Port**: 27017
- **Database**: fraud_db
- **Connection URI**: mongodb://localhost:27017/fraud_db

## Method 1: MongoDB Shell (mongosh)

### Installation (macOS)

```bash
# Install MongoDB Community Edition
brew tap mongodb/brew
brew install mongodb-community

# Or if you already have MongoDB installed
brew install mongosh
```

### Connect to MongoDB

```bash
mongosh mongodb://localhost:27017
```

Or directly specify the database:
```bash
mongosh --authenticationDatabase admin mongodb://localhost:27017/fraud_db
```

### Basic MongoDB Commands

```javascript
// Show current database
db

// Show all databases
show dbs

// Use/switch to a database
use fraud_db

// Show all collections
show collections

// View collection data (first 10 documents)
db.transactions.find().limit(10)

// Count documents in a collection
db.transactions.countDocuments()

// Drop a collection
db.transactions.drop()
```

---

## Method 2: MongoDB Compass (GUI)

### Installation

1. Download from: https://www.mongodb.com/products/tools/compass
2. Install on your macOS
3. Open MongoDB Compass

### Connect Using GUI

1. Click **New Connection**
2. Enter connection string:
   ```
   mongodb://localhost:27017
   ```
3. Click **Connect**
4. Navigate to **fraud_db** database in the left sidebar
5. Browse collections and data visually

---

## Method 3: Docker Exec (No Installation Needed)

### Access MongoDB Shell via Docker

```bash
docker exec -it fraud-mongo mongosh
```

Then use MongoDB commands as shown above.

---

## Sample Dataset for Fraud Detection

I'll show you how to insert realistic test data for the fraud detection system.

### Connect and Insert Sample Data

```javascript
// Connect to MongoDB
use fraud_db

// 1. CREATE ACCOUNTS COLLECTION
db.accounts.insertMany([
  {
    "_id": ObjectId(),
    "accountId": "ACC1001",
    "accountHolder": "Rajesh Kumar",
    "email": "rajesh@bank.com",
    "phone": "+91-98765-43210",
    "accountType": "SAVINGS",
    "createdDate": ISODate("2025-01-15"),
    "lastActiveDate": ISODate("2026-05-07"),
    "passThroughRatio": 0.05,
    "hasLegitimateCredits": true,
    "linkedDevices": ["device-001", "device-002"],
    "status": "ACTIVE"
  },
  {
    "_id": ObjectId(),
    "accountId": "ACC2002",
    "accountHolder": "Priya Singh",
    "email": "priya@bank.com",
    "phone": "+91-98765-43211",
    "accountType": "SAVINGS",
    "createdDate": ISODate("2026-03-01"),
    "lastActiveDate": ISODate("2026-05-07"),
    "passThroughRatio": 0.95,
    "hasLegitimateCredits": false,
    "linkedDevices": ["device-abc-123", "device-def-456", "device-ghi-789", "device-jkl-012", "device-mno-345"],
    "status": "SUSPECT"
  },
  {
    "_id": ObjectId(),
    "accountId": "ACC3003",
    "accountHolder": "Amit Patel",
    "email": "amit@bank.com",
    "phone": "+91-98765-43212",
    "accountType": "CURRENT",
    "createdDate": ISODate("2024-06-10"),
    "lastActiveDate": ISODate("2026-05-06"),
    "passThroughRatio": 0.88,
    "hasLegitimateCredits": false,
    "linkedDevices": ["device-xyz-123", "device-xyz-124", "device-xyz-125"],
    "status": "BLOCKED"
  },
  {
    "_id": ObjectId(),
    "accountId": "ACC4004",
    "accountHolder": "Merchant ABC Corp",
    "email": "merchant@abc.com",
    "phone": "+91-98765-43213",
    "accountType": "MERCHANT",
    "createdDate": ISODate("2024-01-01"),
    "lastActiveDate": ISODate("2026-05-07"),
    "passThroughRatio": 0.02,
    "hasLegitimateCredits": true,
    "linkedDevices": ["device-merchant-001"],
    "status": "ACTIVE"
  }
])

// 2. CREATE TRANSACTIONS COLLECTION
db.transactions.insertMany([
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC1001",
    "toAccountId": "ACC2002",
    "amount": 50000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "abc123hash",
    "timestamp": ISODate("2026-05-07T10:30:00Z"),
    "processingTime": 2,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC1001",
    "toAccountId": "ACC2002",
    "amount": 75000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "abc123hash",
    "timestamp": ISODate("2026-05-07T10:32:00Z"),
    "processingTime": 1,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC1001",
    "toAccountId": "ACC2002",
    "amount": 100000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "abc123hash",
    "timestamp": ISODate("2026-05-07T10:34:00Z"),
    "processingTime": 2,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC2002",
    "toAccountId": "ACC3003",
    "amount": 50000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "def456hash",
    "timestamp": ISODate("2026-05-07T10:35:00Z"),
    "processingTime": 1,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC2002",
    "toAccountId": "ACC3003",
    "amount": 75000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "ghi789hash",
    "timestamp": ISODate("2026-05-07T10:36:00Z"),
    "processingTime": 2,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC2002",
    "toAccountId": "ACC3003",
    "amount": 60000,
    "currency": "INR",
    "transactionType": "NEFT",
    "deviceIdHash": "jkl012hash",
    "timestamp": ISODate("2026-05-07T10:37:00Z"),
    "processingTime": 5,
    "status": "COMPLETED"
  }
])

// 3. CREATE ACCOUNT_LINKS COLLECTION (Graph relationships)
db.account_links.insertMany([
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC1001",
    "toAccountId": "ACC2002",
    "transactionCount": 3,
    "totalAmountTransferred": 225000,
    "avgForwardingTime": 2,
    "lastTransactionTime": ISODate("2026-05-07T10:34:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC2002",
    "toAccountId": "ACC3003",
    "transactionCount": 3,
    "totalAmountTransferred": 185000,
    "avgForwardingTime": 2.67,
    "lastTransactionTime": ISODate("2026-05-07T10:37:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC3003",
    "toAccountId": "ACC4004",
    "transactionCount": 1,
    "totalAmountTransferred": 150000,
    "avgForwardingTime": 1,
    "lastTransactionTime": ISODate("2026-05-05T15:20:00Z"),
    "isDirect": true
  }
])

// 4. CREATE FRAUD_SCORES COLLECTION
db.fraud_scores.insertMany([
  {
    "_id": ObjectId(),
    "accountId": "ACC2002",
    "riskScore": 85,
    "riskLevel": "HIGH",
    "ruleScore": 70,
    "mlScore": 90,
    "velocityScore": 25,
    "graphDepthScore": 20,
    "behaviorScore": 20,
    "deviceScore": 5,
    "reasons": [
      "High transaction velocity: more than 10 incoming transactions in last 1 hour",
      "Fan-out pattern detected: account sent to 5+ accounts within 10 minutes",
      "Pass-through ratio is above 90%",
      "ML signal: account behaves like pass-through money pipe"
    ],
    "computedAt": ISODate("2026-05-07T10:40:00Z")
  },
  {
    "_id": ObjectId(),
    "accountId": "ACC3003",
    "riskScore": 92,
    "riskLevel": "CRITICAL",
    "ruleScore": 85,
    "mlScore": 95,
    "velocityScore": 25,
    "graphDepthScore": 30,
    "behaviorScore": 25,
    "deviceScore": 5,
    "reasons": [
      "Circular money flow detected within 5 hops",
      "ML signal: device pattern resembles mule cluster",
      "Pass-through ratio is above 90%",
      "No legitimate credit history found"
    ],
    "computedAt": ISODate("2026-05-07T10:42:00Z")
  }
])

// 5. CREATE FRAUD_CASES COLLECTION
db.fraud_cases.insertMany([
  {
    "_id": ObjectId(),
    "referenceId": "FRAUD-20260507-ABCDEFGH",
    "accountId": "ACC2002",
    "riskScore": 85,
    "action": "BLOCK",
    "reasons": [
      "High transaction velocity",
      "Fan-out pattern detected",
      "Pass-through ratio is above 90%"
    ],
    "status": "OPEN",
    "resolvedBy": null,
    "resolutionNote": null,
    "falsePositiveReason": null,
    "reportedBy": null,
    "createdAt": ISODate("2026-05-07T10:45:00Z"),
    "resolvedAt": null,
    "falsePositiveReportedAt": null
  },
  {
    "_id": ObjectId(),
    "referenceId": "FRAUD-20260506-IJKLMNOP",
    "accountId": "ACC3003",
    "riskScore": 92,
    "action": "BLOCK",
    "reasons": [
      "Circular money flow detected",
      "Device pattern suggests mule network",
      "No legitimate credits"
    ],
    "status": "RESOLVED",
    "resolvedBy": "ops-user-1",
    "resolutionNote": "Confirmed fraud pattern. Account permanently blocked. Network compromised.",
    "falsePositiveReason": null,
    "reportedBy": null,
    "createdAt": ISODate("2026-05-06T10:15:30Z"),
    "resolvedAt": ISODate("2026-05-06T15:45:20Z"),
    "falsePositiveReportedAt": null
  }
])

// 6. CREATE INDEXES FOR PERFORMANCE
db.transactions.createIndex({ "toAccountId": 1, "timestamp": -1 })
db.transactions.createIndex({ "fromAccountId": 1 })
db.fraud_scores.createIndex({ "accountId": 1 })
db.fraud_cases.createIndex({ "status": 1 })
db.fraud_cases.createIndex({ "referenceId": 1 }, { unique: true })
db.account_links.createIndex({ "fromAccountId": 1 })

// 7. VERIFY DATA WAS INSERTED
console.log("Accounts: " + db.accounts.countDocuments())
console.log("Transactions: " + db.transactions.countDocuments())
console.log("Account Links: " + db.account_links.countDocuments())
console.log("Fraud Scores: " + db.fraud_scores.countDocuments())
console.log("Fraud Cases: " + db.fraud_cases.countDocuments())
```

---

## Quick Start: Copy-Paste Method

### 1. Start MongoDB via Docker (if not running)
```bash
docker-compose up -d
```

### 2. Connect to MongoDB
```bash
docker exec -it fraud-mongo mongosh
```

### 3. Run the Insert Script
Copy and paste the complete script above into the mongosh terminal.

### 4. Verify the data
```javascript
show dbs
use fraud_db
show collections
db.accounts.find()
db.transactions.find()
db.fraud_cases.find()
```

---

## Alternative: Use MongoDB Compass (GUI Method)

### 1. Download and Install
- Go to: https://www.mongodb.com/products/tools/compass
- Install MongoDB Compass

### 2. Connect
- Open MongoDB Compass
- Connection string: `mongodb://localhost:27017`
- Click Connect

### 3. Create Database & Collections
- Click **Create Database**
- Database Name: `fraud_db`
- Collection Name: `accounts`
- Click Create

### 4. Insert Data via GUI
- Click Insert Document
- Paste JSON data for each collection
- Or use the Script tab to run JavaScript

---

## View Data in Collections

### Using mongosh terminal:

```javascript
// View all accounts
db.accounts.find().pretty()

// View specific account
db.accounts.findOne({ accountId: "ACC2002" })

// View transactions
db.transactions.find({ toAccountId: "ACC2002" })

// View fraud cases
db.fraud_cases.find({ status: "OPEN" })

// Count documents
db.transactions.countDocuments()

// Delete all (if needed to reset)
db.transactions.deleteMany({})
```

### Using MongoDB Compass:
1. Navigate to collection in left sidebar
2. Click collection name
3. View/edit documents in the main panel
4. Click document to view full details

---

## Sample Query Examples

```javascript
// Find all transactions for an account in the last 24 hours
db.transactions.find({
  toAccountId: "ACC2002",
  timestamp: { $gte: ISODate("2026-05-06T10:45:00Z") }
})

// Find fraud cases by status
db.fraud_cases.find({ status: "OPEN" })

// Find accounts with high pass-through ratio
db.accounts.find({ passThroughRatio: { $gt: 0.7 } })

// Get fraud case with reference ID
db.fraud_cases.findOne({ referenceId: "FRAUD-20260507-ABCDEFGH" })
```

---

## Clean Up / Reset Data

```javascript
// Drop entire database (careful!)
db.dropDatabase()

// Drop specific collection
db.accounts.drop()
db.transactions.drop()
db.fraud_cases.drop()

// Delete specific documents
db.fraud_cases.deleteOne({ referenceId: "FRAUD-20260507-ABCDEFGH" })
```

---

## Next Steps

1. ✅ Start Docker: `docker-compose up -d`
2. ✅ Connect to MongoDB (either via mongosh or Compass)
3. ✅ Insert sample data using the script above
4. ✅ Verify data with `show collections` and `db.collection.find()`
5. ✅ Now ready to test Spring Boot API with this data!

---

## Connection from Spring Boot Application

The Spring Boot service will automatically use this data. Configuration in `application.yml`:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/fraud_db
```

When you run the Spring Boot app and make API calls, it will query and update this MongoDB data.


