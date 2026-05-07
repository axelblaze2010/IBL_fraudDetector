package com.bank.frauddetection.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MlRiskRequest {
    private long txnCountLast1Hour;
    private long txnCountLast24Hours;
    private BigDecimal amountReceivedLast24Hours;
    private BigDecimal amountSentLast24Hours;
    private double passThroughRatio;
    private double avgForwardingTime;
    private int uniqueSendersCount;
    private int uniqueReceiversCount;
    private int linkedDeviceCount;
    private long accountAgeDays;
    private long inactiveDaysBeforeTxn;
    private int hopDistanceFromFraudNode;
    private boolean circularFlowDetected;
    private int fanOutCount;
    private boolean hasLegitimateCredits;
}
