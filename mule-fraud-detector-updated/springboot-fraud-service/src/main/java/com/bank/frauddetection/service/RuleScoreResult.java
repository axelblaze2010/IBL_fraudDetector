package com.bank.frauddetection.service;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RuleScoreResult {
    private int ruleScore;
    private int velocityScore;
    private int graphDepthScore;
    private int behaviorScore;
    private int deviceScore;
    private List<String> reasons;
    private boolean circularFlowDetected;
    private int fanOutCount;
    private int hopDistanceFromFraudNode;
}
