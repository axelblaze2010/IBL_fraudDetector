# How to Import Mule Fraud Detector API into Postman

## Quick Import Steps

### Method 1: Import via File (Recommended)

1. **Open Postman** on your computer
2. Click the **Import** button in the top-left corner (or use `Ctrl+O` / `Cmd+O`)
3. Select **Upload Files** tab
4. Navigate to and select: `Mule_Fraud_Detector_API.postman_collection.json`
5. Click **Import** button
6. The collection will appear in your left sidebar under Collections

### Method 2: Import by Link (if using Postman cloud)

1. Click **Import** button in Postman
2. Select **Link** tab
3. Paste the file path or use the Import button
4. Follow the prompts

## After Import

### Verify Collection Loaded
- You should see **"Mule Fraud Detector API"** in your Collections sidebar
- Expand it to see:
  - ✅ Spring Boot Fraud Service (5 endpoints)
  - ✅ ML Risk Service (2 endpoints)

### Pre-configured Content

The collection includes:

**Spring Boot APIs:**
- ✅ POST /api/fraud/check-mule (with 2 example responses)
- ✅ GET /api/fraud/risk-score/{accountId}
- ✅ GET /api/fraud/cases (with OPEN and RESOLVED examples)
- ✅ POST /api/fraud/report-false-positive
- ✅ PATCH /api/fraud/cases/{referenceId}/resolve

**ML Service APIs:**
- ✅ GET /health
- ✅ POST /ml/predict-risk (with high risk and low risk examples)

### All Requests Include:
- ✅ Pre-filled JSON request bodies
- ✅ Sample responses for reference
- ✅ Proper HTTP methods (GET, POST, PATCH)
- ✅ Content-Type headers
- ✅ Descriptions for each endpoint

## Customization Tips

### Modify Base URLs
If your services run on different ports, edit:

1. Click **Mule Fraud Detector API** collection
2. Go to **Variables** tab
3. Update:
   - `baseUrl` - Change `8080` port if needed (e.g., for production)
   - `mlBaseUrl` - Change `8000` port if needed

### Use Variables in Requests
Instead of hardcoding URLs, you can use:
- `{{baseUrl}}/api/fraud/check-mule` - Uses Spring Boot base URL
- `{{mlBaseUrl}}/ml/predict-risk` - Uses ML service base URL

To use these variables, click on a request URL and replace the base URL with the variable reference.

### Save Responses as Examples
After running a request:
1. Click the response area
2. Click **Save as example**
3. This stores the response with the request for reference

## Testing Workflow

### 1. Health Check
- Go to: `Mule Risk Service` → `Health Check`
- Click **Send**
- Should see `{"status": "UP"}`

### 2. Create Test Transaction
- Go to: `Spring Boot Fraud Service` → `Check Mule Risk`
- Update `fromAccountId` and `toAccountId` as needed
- Click **Send**
- Review the risk score and fraud case reference

### 3. Check Risk Score
- Go to: `Get Latest Risk Score`
- Replace `ACC2002` with your test account ID
- Click **Send**

### 4. View Fraud Cases
- Go to: `Get Fraud Cases by Status`
- Click **Send** (defaults to OPEN cases)
- To view RESOLVED cases, modify `status` parameter to `RESOLVED`

### 5. Report False Positive (if needed)
- Go to: `Report False Positive`
- Replace `FRAUD-20260507-ABCDEFGH` with actual case reference
- Update `reportedBy` and `reason`
- Click **Send**

### 6. Resolve Fraud Case (if needed)
- Go to: `Resolve Fraud Case`
- Replace the case reference in URL
- Update `resolvedBy` and `resolutionNote`
- Click **Send**

## Common Issues & Solutions

### Issue: "Cannot connect to localhost:8080"
**Solution:** 
- Ensure Spring Boot service is running: `cd springboot-fraud-service && gradle bootRun`
- Check port 8080 is not in use

### Issue: "Cannot connect to localhost:8000"
**Solution:**
- Ensure ML service is running: `cd ml-service && uvicorn main:app --port 8000`
- Check port 8000 is not in use

### Issue: "Cannot connect to MongoDB/Redis"
**Solution:**
- Ensure Docker containers are running: `docker-compose up -d`
- Verify with: `docker ps`

### Issue: Parameters not updating in requests
**Solution:**
- Click directly on parameter values to edit them
- Or use Postman variables for dynamic values

### Issue: Collection doesn't appear after import
**Solution:**
- Restart Postman
- Try importing again using Method 1 (Upload Files)
- Check file path is correct

## Advanced Features

### Use Environment Variables (Optional)
Create a Postman environment for different deployment scenarios:

1. Click **Environments** gear icon
2. Click **Create Environment**
3. Name it (e.g., "Local Dev", "Staging", "Production")
4. Add variables:
   ```
   baseUrl: http://localhost:8080  (or your endpoint)
   mlBaseUrl: http://localhost:8000  (or your endpoint)
   ```
5. Select the environment before running requests

### Add Pre-request Scripts (Optional)
For advanced testing with timestamps or calculations:

1. Open any request
2. Click **Pre-request Script** tab
3. Add JavaScript to set dynamic values:
   ```javascript
   pm.variables.set("timestamp", new Date().toISOString());
   ```

## Exporting Results

### Export Collection
To share the collection with team members:
1. Right-click collection name
2. Select **Export**
3. Choose export format (use default JSON)
4. Share the exported file

### Generate API Documentation
Postman can auto-generate docs:
1. Click collection name
2. Look for **Publish** or **Share** options
3. Generate shareable API documentation link

## Next Steps

✅ **Import the collection** using Method 1  
✅ **Verify Docker containers are running** with `docker-compose ps`  
✅ **Start ML service** in terminal: `cd ml-service && uvicorn main:app --port 8000`  
✅ **Start Spring Boot service** in terminal: `cd springboot-fraud-service && gradle bootRun`  
✅ **Test endpoints** using the pre-configured requests  
✅ **Review sample responses** built into each request  

## Support

For API field descriptions and detailed documentation, see:
- 📖 `/API_DOCUMENTATION.md` - Comprehensive API guide with all parameters explained

Enjoy testing your Mule Fraud Detector APIs! 🚀

