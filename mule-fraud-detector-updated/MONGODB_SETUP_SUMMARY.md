# MongoDB Setup Summary - Files Created

## ✅ MongoDB is Running

**Version**: 6.0.27  
**Connection**: `mongodb://localhost:27017`  
**Database**: `fraud_db`

---

## 📁 Files I Created for You

### 1. **MONGODB_QUICK_START.md** ⭐ START HERE
The fastest way to get started:
- 60-second quick start guide
- Common commands
- Example queries
- Troubleshooting

👉 **USE THIS FIRST** - Open this file and follow Step 1-3

---

### 2. **MONGODB_GUIDE.md**
Comprehensive reference guide with:
- Multiple connection methods (Shell, Compass, Docker)
- Sample dataset with realistic fraud detection data
- Query examples
- Data structure explanations
- GUI tool setup (MongoDB Compass)

👉 **USE THIS FOR DETAILS** - If you need full documentation

---

### 3. **insert_sample_data.js**
Ready-to-use MongoDB data insertion script containing:
- 4 sample accounts (1 legitimate, 3 suspicious)
- 6 sample transactions
- 3 account links (money flow graph)
- 2 fraud scores
- 2 fraud cases (1 OPEN, 1 RESOLVED)

👉 **USE THIS TO LOAD DATA** - Run this script to populate MongoDB

---

## 🚀 QUICK START (3 Steps)

### Step 1: Open MongoDB Shell
```bash
docker exec -it fraud-mongo mongosh
```

### Step 2: Load Sample Data
In the mongosh shell, paste (or use):
```javascript
load('/insert_sample_data.js')
```

### Step 3: Verify Data
```javascript
use fraud_db
show collections
db.accounts.countDocuments()
```

**Expected Results:**
- Accounts: 4
- Transactions: 6
- Account Links: 3
- Fraud Scores: 2
- Fraud Cases: 2

---

## 📊 Sample Data Accounts

| Account ID | Account Holder | Risk Level | Status |
|------------|----------------|-----------|--------|
| ACC1001 | Rajesh Kumar | LOW | Legitimate user |
| ACC2002 | Priya Singh | HIGH | Suspicious - high pass-through |
| ACC3003 | Amit Patel | CRITICAL | Fraud - blocked |
| ACC4004 | Merchant ABC | LOW | Legitimate merchant |

---

## 🎯 Common Use Cases

### View All Open Fraud Cases
```javascript
db.fraud_cases.find({ status: "OPEN" })
```

### Check Transactions for Suspicious Account
```javascript
db.transactions.find({ toAccountId: "ACC2002" })
```

### Find High-Risk Accounts
```javascript
db.accounts.find({ passThroughRatio: { $gt: 0.7 } })
```

### Count Total Transactions
```javascript
db.transactions.countDocuments()
```

---

## 🔌 Connection Methods

### Method 1: Via Docker (Easiest - What I Recommend)
```bash
docker exec -it fraud-mongo mongosh
```

### Method 2: Local mongosh (If Installed)
```bash
mongosh mongodb://localhost:27017/fraud_db
```

### Method 3: MongoDB Compass (GUI - Visual)
1. Download from: https://www.mongodb.com/products/tools/compass
2. Connection String: `mongodb://localhost:27017`
3. Click Connect

---

## 📈 What Happens Next

1. **Insert Data** ← You are here
2. Start Spring Boot API: `cd springboot-fraud-service && ./gradlew bootRun`
3. Start ML Service: `cd ml-service && uvicorn main:app --port 8000`
4. Test API with Postman (use the Postman collection I created earlier)

---

## 💾 Useful Commands Reference

### Basic MongoDB Commands
```bash
# Connect
docker exec -it fraud-mongo mongosh

# In mongosh shell:
use fraud_db                           # Switch database
show collections                       # Show all tables
db.accounts.find()                     # View all accounts
db.accounts.countDocuments()           # Count records
db.fraud_cases.find({ status: "OPEN" }) # Filter by status
exit                                   # Exit shell
```

### View Formatted Data
```javascript
db.fraud_cases.findOne({ status: "OPEN" })  // Show one document nicely
db.fraud_cases.find().pretty()              // Show all with formatting
```

### Reset Data (if needed)
```javascript
db.dropDatabase()           // Delete all data
```

---

## ✨ Key Collections

Your MongoDB will have these collections:

1. **accounts** - User account profiles (4 records)
2. **transactions** - Money transfers (6 records)
3. **account_links** - Money flow relationships (3 records)
4. **fraud_scores** - Risk assessments (2 records)
5. **fraud_cases** - Fraud alerts (2 records)

---

## 🆘 Troubleshooting

### MongoDB not running?
```bash
docker-compose up -d
```

### Data didn't load?
Make sure you're in `fraud_db` database:
```javascript
use fraud_db
```

### Want to reload data?
```javascript
db.dropDatabase()  // Delete old data
load('/insert_sample_data.js')  // Load fresh data
```

### Can't connect?
Check Docker is running:
```bash
docker ps | grep mongo
```

---

## 📚 Next Steps

1. ✅ Open MongoDB shell: `docker exec -it fraud-mongo mongosh`
2. ✅ Load data: `load('/insert_sample_data.js')`
3. ✅ Verify: `show collections`
4. ✅ View data: `db.fraud_cases.find()`
5. ✅ Move to API testing with Postman

---

## 📖 File Locations

All files are in:
```
/Users/anuj/Desktop/project/IBL_fraudDetector/mule-fraud-detector-updated/
```

- `MONGODB_QUICK_START.md` - Quick reference
- `MONGODB_GUIDE.md` - Full documentation
- `insert_sample_data.js` - Data loading script

---

**You're all set!** 🎉 MongoDB is running with sample data ready to be loaded. Follow the 3-step quick start above!

