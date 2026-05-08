// Test Data: Fan-Out with Circular Flow Pattern
// Pattern: ACC1 → ACC2 → (ACC3, ACC4, ACC5 split) → back to ACC1

use fraud_db

// ============================================
// 1. INSERT ACCOUNTS FOR FAN-OUT PATTERN
// ============================================
db.accounts.insertMany([
  {
    "_id": ObjectId(),
    "accountId": "FANOUT1",
    "accountHolder": "Original Account",
    "email": "fanout1@test.com",
    "phone": "+91-10000-00001",
    "accountType": "SAVINGS",
    "createdDate": ISODate("2026-04-01"),
    "lastActiveDate": ISODate("2026-05-08"),
    "passThroughRatio": 0.85,
    "hasLegitimateCredits": false,
    "linkedDevices": ["device-fo-001"],
    "status": "ACTIVE"
  },
  {
    "_id": ObjectId(),
    "accountId": "FANOUT2",
    "accountHolder": "Intermediary Account",
    "email": "fanout2@test.com",
    "phone": "+91-10000-00002",
    "accountType": "SAVINGS",
    "createdDate": ISODate("2026-04-05"),
    "lastActiveDate": ISODate("2026-05-08"),
    "passThroughRatio": 0.92,
    "hasLegitimateCredits": false,
    "linkedDevices": ["device-fo-002"],
    "status": "ACTIVE"
  },
  {
    "_id": ObjectId(),
    "accountId": "FANOUT3",
    "accountHolder": "Split Recipient 1",
    "email": "fanout3@test.com",
    "phone": "+91-10000-00003",
    "accountType": "SAVINGS",
    "createdDate": ISODate("2026-05-01"),
    "lastActiveDate": ISODate("2026-05-08"),
    "passThroughRatio": 0.88,
    "hasLegitimateCredits": false,
    "linkedDevices": ["device-fo-003", "device-fo-003-alt"],
    "status": "ACTIVE"
  },
  {
    "_id": ObjectId(),
    "accountId": "FANOUT4",
    "accountHolder": "Split Recipient 2",
    "email": "fanout4@test.com",
    "phone": "+91-10000-00004",
    "accountType": "SAVINGS",
    "createdDate": ISODate("2026-05-01"),
    "lastActiveDate": ISODate("2026-05-08"),
    "passThroughRatio": 0.89,
    "hasLegitimateCredits": false,
    "linkedDevices": ["device-fo-004"],
    "status": "ACTIVE"
  },
  {
    "_id": ObjectId(),
    "accountId": "FANOUT5",
    "accountHolder": "Split Recipient 3",
    "email": "fanout5@test.com",
    "phone": "+91-10000-00005",
    "accountType": "SAVINGS",
    "createdDate": ISODate("2026-05-01"),
    "lastActiveDate": ISODate("2026-05-08"),
    "passThroughRatio": 0.87,
    "hasLegitimateCredits": false,
    "linkedDevices": ["device-fo-005"],
    "status": "ACTIVE"
  }
])
print("✓ Inserted 5 accounts for fan-out with circular flow")

// ============================================
// 2. TRANSACTIONS FOR FAN-OUT PATTERN
// ============================================
db.transactions.insertMany([
  // Initial: External → FANOUT1 (100,000 INR)
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC0000",
    "toAccountId": "FANOUT1",
    "amount": 100000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "external-hash",
    "timestamp": ISODate("2026-05-08T08:00:00Z"),
    "processingTime": 2,
    "status": "COMPLETED"
  },
  // FANOUT1 → FANOUT2 (100,000)
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT1",
    "toAccountId": "FANOUT2",
    "amount": 100000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "fo1to2-hash",
    "timestamp": ISODate("2026-05-08T08:05:00Z"),
    "processingTime": 1,
    "status": "COMPLETED"
  },
  // Fan-out: FANOUT2 splits to FANOUT3, FANOUT4, FANOUT5 (within 5 minutes)
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT2",
    "toAccountId": "FANOUT3",
    "amount": 33000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "fo2to3-hash",
    "timestamp": ISODate("2026-05-08T08:07:00Z"),
    "processingTime": 1,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT2",
    "toAccountId": "FANOUT4",
    "amount": 33000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "fo2to4-hash",
    "timestamp": ISODate("2026-05-08T08:08:00Z"),
    "processingTime": 1,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT2",
    "toAccountId": "FANOUT5",
    "amount": 34000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "fo2to5-hash",
    "timestamp": ISODate("2026-05-08T08:09:00Z"),
    "processingTime": 1,
    "status": "COMPLETED"
  },
  // CIRCULAR: FANOUT3, FANOUT4, FANOUT5 return funds to FANOUT1
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT3",
    "toAccountId": "FANOUT1",
    "amount": 32000,
    "currency": "INR",
    "transactionType": "NEFT",
    "deviceIdHash": "fo3back-hash",
    "timestamp": ISODate("2026-05-08T09:00:00Z"),
    "processingTime": 3,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT4",
    "toAccountId": "FANOUT1",
    "amount": 32000,
    "currency": "INR",
    "transactionType": "NEFT",
    "deviceIdHash": "fo4back-hash",
    "timestamp": ISODate("2026-05-08T09:05:00Z"),
    "processingTime": 3,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT5",
    "toAccountId": "FANOUT1",
    "amount": 33000,
    "currency": "INR",
    "transactionType": "NEFT",
    "deviceIdHash": "fo5back-hash",
    "timestamp": ISODate("2026-05-08T09:10:00Z"),
    "processingTime": 3,
    "status": "COMPLETED"
  }
])
print("✓ Inserted 8 transactions: FANOUT1 ~ FANOUT2 [split to 3] ~ back to FANOUT1")

// ============================================
// 3. ACCOUNT LINKS
// ============================================
db.account_links.insertMany([
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC0000",
    "toAccountId": "FANOUT1",
    "transactionCount": 1,
    "totalAmountTransferred": 100000,
    "avgForwardingTime": 5,
    "lastTransactionTime": ISODate("2026-05-08T08:00:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT1",
    "toAccountId": "FANOUT2",
    "transactionCount": 1,
    "totalAmountTransferred": 100000,
    "avgForwardingTime": 5,
    "lastTransactionTime": ISODate("2026-05-08T08:05:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT2",
    "toAccountId": "FANOUT3",
    "transactionCount": 1,
    "totalAmountTransferred": 33000,
    "avgForwardingTime": 2,
    "lastTransactionTime": ISODate("2026-05-08T08:07:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT2",
    "toAccountId": "FANOUT4",
    "transactionCount": 1,
    "totalAmountTransferred": 33000,
    "avgForwardingTime": 2,
    "lastTransactionTime": ISODate("2026-05-08T08:08:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT2",
    "toAccountId": "FANOUT5",
    "transactionCount": 1,
    "totalAmountTransferred": 34000,
    "avgForwardingTime": 2,
    "lastTransactionTime": ISODate("2026-05-08T08:09:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT3",
    "toAccountId": "FANOUT1",
    "transactionCount": 1,
    "totalAmountTransferred": 32000,
    "avgForwardingTime": 60,
    "lastTransactionTime": ISODate("2026-05-08T09:00:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT4",
    "toAccountId": "FANOUT1",
    "transactionCount": 1,
    "totalAmountTransferred": 32000,
    "avgForwardingTime": 60,
    "lastTransactionTime": ISODate("2026-05-08T09:05:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "FANOUT5",
    "toAccountId": "FANOUT1",
    "transactionCount": 1,
    "totalAmountTransferred": 33000,
    "avgForwardingTime": 60,
    "lastTransactionTime": ISODate("2026-05-08T09:10:00Z"),
    "isDirect": true
  }
])
print("✓ Inserted 8 account links")

// ============================================
// 4. SUMMARY
// ============================================
print("\n=== FAN-OUT WITH CIRCULAR FLOW PATTERN ===")
print("Pattern: ACC0000 → FANOUT1 → FANOUT2 → (FANOUT3,4,5 split) → back to FANOUT1")
print("")
print("Stage 1: Entry → 100,000")
print("Stage 2: Pass through (FANOUT1 → FANOUT2) → 100,000")
print("Stage 3: Split/Fan-out (FANOUT2 → 3 accounts) → 33k, 33k, 34k")
print("Stage 4: Convergence/Return (back to FANOUT1) → 32k, 32k, 33k (~97k)")
print("")
print("Risk Indicators:")
print("- High pass-through ratio (85-92%)")
print("- No legitimate credits")
print("- Multiple devices on accounts")
print("- Fan-out to 3 recipients in short time")
print("- Funds return to source (circular)")
print("")
print("✓ Ready to test with: POST /api/fraud/check-mule with toAccountId: FANOUT1")
