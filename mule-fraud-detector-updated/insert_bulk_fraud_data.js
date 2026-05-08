// Bulk data insertion to trigger fraud case detection
// Run this after insert_sample_data.js in mongosh

// ============================================
// 1. INSERT SUSPICIOUS ACCOUNT
// ============================================
db.accounts.insertOne({
  "_id": ObjectId(),
  "accountId": "ACC5005",
  "accountHolder": "Suspicious User",
  "email": "suspicious@fake.com",
  "phone": "+91-99999-99999",
  "accountType": "SAVINGS",
  "createdDate": ISODate("2026-05-01"),
  "lastActiveDate": ISODate("2026-05-07"),
  "passThroughRatio": 0.95,
  "hasLegitimateCredits": false,
  "linkedDevices": ["device-sus-001", "device-sus-002", "device-sus-003", "device-sus-004", "device-sus-005"],
  "status": "ACTIVE"
})
print("✓ Inserted suspicious account ACC5005")

// ============================================
// 2. INSERT BULK TRANSACTIONS (15 in last hour to trigger velocity)
// ============================================
let transactions = []
let baseTime = new Date("2026-05-07T11:00:00Z")
for (let i = 1; i <= 15; i++) {
  transactions.push({
    "_id": ObjectId(),
    "fromAccountId": "ACC" + (1000 + Math.floor(Math.random() * 100)).toString().padStart(4, '0'),
    "toAccountId": "ACC5005",
    "amount": Math.floor(Math.random() * 50000) + 10000, // 10k to 60k
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "sus-device-" + i,
    "timestamp": new Date(baseTime.getTime() + (i * 2 * 60 * 1000)), // every 2 minutes
    "processingTime": Math.floor(Math.random() * 5) + 1,
    "status": "COMPLETED"
  })
}
db.transactions.insertMany(transactions)
print("✓ Inserted 15 bulk transactions to ACC5005")

// ============================================
// 3. INSERT ACCOUNT LINKS FOR CIRCULAR FLOW
// ============================================
db.accounts.insertMany([
  {
    "_id": ObjectId(),
    "accountId": "ACC6006",
    "accountHolder": "Circular Node 1",
    "email": "circular1@fake.com",
    "phone": "+91-88888-88888",
    "accountType": "SAVINGS",
    "createdDate": ISODate("2026-05-01"),
    "lastActiveDate": ISODate("2026-05-07"),
    "passThroughRatio": 0.92,
    "hasLegitimateCredits": false,
    "linkedDevices": ["device-circ-001", "device-circ-002"],
    "status": "ACTIVE"
  },
  {
    "_id": ObjectId(),
    "accountId": "ACC7007",
    "accountHolder": "Circular Node 2",
    "email": "circular2@fake.com",
    "phone": "+91-77777-77777",
    "accountType": "SAVINGS",
    "createdDate": ISODate("2026-05-01"),
    "lastActiveDate": ISODate("2026-05-07"),
    "passThroughRatio": 0.90,
    "hasLegitimateCredits": false,
    "linkedDevices": ["device-circ-003"],
    "status": "ACTIVE"
  }
])

// Insert transactions for circular flow
db.transactions.insertMany([
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC5005",
    "toAccountId": "ACC6006",
    "amount": 200000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "circ-hash-1",
    "timestamp": ISODate("2026-05-07T11:35:00Z"),
    "processingTime": 2,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC6006",
    "toAccountId": "ACC7007",
    "amount": 195000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "circ-hash-2",
    "timestamp": ISODate("2026-05-07T11:40:00Z"),
    "processingTime": 1,
    "status": "COMPLETED"
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC7007",
    "toAccountId": "ACC5005",
    "amount": 190000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "circ-hash-3",
    "timestamp": ISODate("2026-05-07T11:45:00Z"),
    "processingTime": 3,
    "status": "COMPLETED"
  }
])

// Insert account links
db.account_links.insertMany([
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC5005",
    "toAccountId": "ACC6006",
    "transactionCount": 1,
    "totalAmountTransferred": 200000,
    "avgForwardingTime": 5,
    "lastTransactionTime": ISODate("2026-05-07T11:35:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC6006",
    "toAccountId": "ACC7007",
    "transactionCount": 1,
    "totalAmountTransferred": 195000,
    "avgForwardingTime": 5,
    "lastTransactionTime": ISODate("2026-05-07T11:40:00Z"),
    "isDirect": true
  },
  {
    "_id": ObjectId(),
    "fromAccountId": "ACC7007",
    "toAccountId": "ACC5005",
    "transactionCount": 1,
    "totalAmountTransferred": 190000,
    "avgForwardingTime": 5,
    "lastTransactionTime": ISODate("2026-05-07T11:45:00Z"),
    "isDirect": true
  }
])

print("✓ Inserted circular flow accounts, transactions, and links")

// ============================================
// 4. INSERT FAN-OUT TRANSACTIONS
// ============================================
let fanOutTransactions = []
for (let i = 1; i <= 6; i++) {
  fanOutTransactions.push({
    "_id": ObjectId(),
    "fromAccountId": "ACC5005",
    "toAccountId": "ACC" + (8000 + i).toString().padStart(4, '0'),
    "amount": Math.floor(Math.random() * 30000) + 5000,
    "currency": "INR",
    "transactionType": "UPI",
    "deviceIdHash": "fan-device-" + i,
    "timestamp": ISODate("2026-05-07T11:50:00Z"),
    "processingTime": 2,
    "status": "COMPLETED"
  })
}
db.transactions.insertMany(fanOutTransactions)
print("✓ Inserted 6 fan-out transactions from ACC5005")

// ============================================
// 5. VERIFY DATA
// ============================================
print("\n=== BULK DATA SUMMARY ===")
print("New suspicious account: ACC5005")
print("Bulk transactions to ACC5005: " + db.transactions.countDocuments({toAccountId: "ACC5005"}))
print("Circular flow transactions: 3")
print("Fan-out transactions: 6")
print("✓ Bulk data inserted to trigger fraud detection for ACC5005!")
