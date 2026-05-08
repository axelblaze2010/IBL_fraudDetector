# MongoDB Quick Start - Connect and Insert Data

## ⚡ 60-Second Quick Start

### Step 1: Connect to MongoDB (Copy & Paste)
```bash
docker exec -it fraud-mongo mongosh
```

### Step 2: Load the Sample Data Script
Once in mongosh, run:
```javascript
load('/insert_sample_data.js')
```

OR manually paste the content from `insert_sample_data.js`

### Step 3: Verify Data was Inserted
```javascript
use fraud_db
show collections
db.accounts.find()
```

---

## 📌 Most Common Commands

### Connect to MongoDB
```bash
# Via Docker (easiest)
docker exec -it fraud-mongo mongosh

# If mongosh installed locally
mongosh mongodb://localhost:27017/fraud_db
```

### Check Collections
```javascript
use fraud_db
show collections
```

### View All Data in a Collection
```javascript
db.accounts.find()           // View all accounts
db.transactions.find()       // View all transactions
db.fraud_cases.find()        // View all fraud cases
```

### View Formatted (Pretty) Output
```javascript
db.accounts.find().pretty()
```

### Count Documents
```javascript
db.accounts.countDocuments()
db.transactions.countDocuments()
```

### Find Specific Document
```javascript
db.accounts.findOne({ accountId: "ACC2002" })
db.fraud_cases.findOne({ status: "OPEN" })
```

### Filter by Status
```javascript
db.fraud_cases.find({ status: "OPEN" })
db.fraud_cases.find({ status: "RESOLVED" })
```

### Exit MongoDB Shell
```javascript
exit
```

---

## 🔍 Example Queries

### Find all transactions for an account
```javascript
db.transactions.find({ toAccountId: "ACC2002" })
```

### Find suspicious accounts (high pass-through ratio)
```javascript
db.accounts.find({ passThroughRatio: { $gt: 0.7 } })
```

### Find all open fraud cases
```javascript
db.fraud_cases.find({ status: "OPEN" })
```

### Count transactions from a specific account
```javascript
db.transactions.countDocuments({ fromAccountId: "ACC1001" })
```

### Find fraud cases created today
```javascript
db.fraud_cases.find({
  createdAt: { $gte: ISODate("2026-05-07T00:00:00Z") }
})
```

---

## 📊 Sample Data Structure

### Accounts
```json
{
  "accountId": "ACC2002",
  "accountHolder": "Priya Singh",
  "passThroughRatio": 0.95,      // Pass money through = fraud indicator
  "hasLegitimateCredits": false,  // No salary credits = suspicious
  "linkedDevices": 5              // Multiple devices = mule network
}
```

### Transactions
```json
{
  "fromAccountId": "ACC1001",
  "toAccountId": "ACC2002",
  "amount": 50000,
  "timestamp": ISODate("2026-05-07T10:30:00Z")
}
```

### Fraud Cases
```json
{
  "referenceId": "FRAUD-20260507-ABCDEFGH",
  "accountId": "ACC2002",
  "riskScore": 85,
  "status": "OPEN"               // Can be: OPEN, RESOLVED, FALSE_POSITIVE
}
```

---

## 🗑️ Reset/Delete Data (if needed)

### Delete All Data from a Collection
```javascript
db.accounts.deleteMany({})
db.transactions.deleteMany({})
db.fraud_cases.deleteMany({})
```

### Delete Entire Database
```javascript
db.dropDatabase()
```

### Delete Specific Document
```javascript
db.fraud_cases.deleteOne({ referenceId: "FRAUD-20260507-ABCDEFGH" })
```

---

## 🎯 Practical Workflow

1. **Connect to MongoDB:**
   ```bash
   docker exec -it fraud-mongo mongosh
   ```

2. **Insert Sample Data:**
   ```javascript
   load('/insert_sample_data.js')
   ```

3. **Check what was inserted:**
   ```javascript
   use fraud_db
   show collections
   db.accounts.countDocuments()      // Should show 4
   db.transactions.countDocuments()  // Should show 6
   db.fraud_cases.countDocuments()   // Should show 2
   ```

4. **View specific fraud case:**
   ```javascript
   db.fraud_cases.findOne({ status: "OPEN" })
   ```

5. **View account transactions:**
   ```javascript
   db.transactions.find({ toAccountId: "ACC2002" })
   ```

---

## ✅ Verification Checklist

After inserting data, verify:

- [ ] `docker ps` shows `fraud-mongo` running
- [ ] `db.accounts.countDocuments()` returns 4
- [ ] `db.transactions.countDocuments()` returns 6
- [ ] `db.fraud_cases.countDocuments()` returns 2
- [ ] `db.fraud_cases.find({ status: "OPEN" })` returns FRAUD-20260507-ABCDEFGH

---

## 🚀 Next: Test with Spring Boot API

Once data is in MongoDB, start the Spring Boot service:
```bash
cd springboot-fraud-service
./gradlew bootRun
```

Then test the API:
```bash
curl http://localhost:8080/api/fraud/cases?status=OPEN
```

The Spring Boot app will query the MongoDB data you just inserted!

---

## 💡 Pro Tips

- **Use Compass GUI**: Install MongoDB Compass for visual data browsing
- **Backup data**: `mongoexport --db fraud_db --collection accounts --out accounts.json`
- **Import data**: `mongoimport --db fraud_db --collection accounts --file accounts.json`
- **Pretty print**: Add `.pretty()` to any query for readable output

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| mongosh not found | `brew install mongosh` or `npm install -g mongosh` |
| Cannot connect | Ensure Docker container running: `docker-compose up -d` |
| Collection empty | Run `load('/insert_sample_data.js')` again |
| Data not persisting | Check MongoDB volume: `docker volume ls` |


