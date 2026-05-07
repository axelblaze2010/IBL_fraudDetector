from fastapi import FastAPI
from pydantic import BaseModel
from typing import List

app = FastAPI(title="Mule Risk ML Service")

class MlRiskRequest(BaseModel):
    txnCountLast1Hour: int
    txnCountLast24Hours: int
    amountReceivedLast24Hours: float
    amountSentLast24Hours: float
    passThroughRatio: float
    avgForwardingTime: float
    uniqueSendersCount: int
    uniqueReceiversCount: int
    linkedDeviceCount: int
    accountAgeDays: int
    inactiveDaysBeforeTxn: int
    hopDistanceFromFraudNode: int
    circularFlowDetected: bool
    fanOutCount: int
    hasLegitimateCredits: bool

class MlRiskResponse(BaseModel):
    mlScore: int
    fraudProbability: float
    mlReasons: List[str]

@app.get("/health")
def health():
    return {"status": "UP"}

@app.post("/ml/predict-risk", response_model=MlRiskResponse)
def predict_risk(request: MlRiskRequest):
    score = 0
    reasons = []

    if request.txnCountLast1Hour > 10:
        score += 20
        reasons.append("ML signal: unusually high transaction count in last 1 hour")
    if request.passThroughRatio > 0.90:
        score += 25
        reasons.append("ML signal: account behaves like pass-through money pipe")
    if request.linkedDeviceCount > 3:
        score += 20
        reasons.append("ML signal: device pattern resembles mule cluster")
    if request.circularFlowDetected:
        score += 20
        reasons.append("ML signal: circular transaction flow detected")
    if request.fanOutCount >= 5:
        score += 15
        reasons.append("ML signal: fan-out transaction behaviour detected")
    if not request.hasLegitimateCredits:
        score += 10
        reasons.append("ML signal: no legitimate credit history found")

    score = min(score, 100)
    return MlRiskResponse(mlScore=score, fraudProbability=score / 100.0, mlReasons=reasons)
