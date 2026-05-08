// Sample MongoDB Data Insertion Script
// Copy all of this into mongosh terminal after connecting to fraud_db

use fraud_db

// ============================================
// 1. INSERT SAMPLE ACCOUNTS
// ============================================
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
print("✓ Inserted 4 accounts")

// ============================================
// 2. INSERT SAMPLE TRANSACTIONS
// ============================================
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
print("✓ Inserted 6 transactions")

// ============================================
// 3. INSERT ACCOUNT LINKS (Graph)
// ============================================
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
print("✓ Inserted 3 account links")

// ============================================
// 4. INSERT FRAUD SCORES
// ============================================
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
print("✓ Inserted 2 fraud scores")

// ============================================
// 5. INSERT FRAUD CASES
// ============================================
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
    "resolutionNote": "Confirmed fraud pattern. Account permanently blocked.",
    "falsePositiveReason": null,
    "reportedBy": null,
    "createdAt": ISODate("2026-05-06T10:15:30Z"),
    "resolvedAt": ISODate("2026-05-06T15:45:20Z"),
    "falsePositiveReportedAt": null
  }
])
print("✓ Inserted 2 fraud cases")

// ============================================
// 6. CREATE INDEXES
// ============================================
db.transactions.createIndex({ "toAccountId": 1, "timestamp": -1 })
db.transactions.createIndex({ "fromAccountId": 1 })
db.fraud_scores.createIndex({ "accountId": 1 })
db.fraud_cases.createIndex({ "status": 1 })
db.fraud_cases.createIndex({ "referenceId": 1 }, { unique: true })
db.account_links.createIndex({ "fromAccountId": 1 })
print("✓ Created indexes")

// ============================================
// 7. VERIFY DATA
// ============================================
print("\n=== DATA SUMMARY ===")
print("Accounts: " + db.accounts.countDocuments())
print("Transactions: " + db.transactions.countDocuments())
print("Account Links: " + db.account_links.countDocuments())
print("Fraud Scores: " + db.fraud_scores.countDocuments())
print("Fraud Cases: " + db.fraud_cases.countDocuments())
print("✓ All data inserted successfully!")

