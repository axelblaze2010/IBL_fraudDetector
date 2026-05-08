package com.bank.frauddetection.service;

import com.bank.frauddetection.dto.MlRiskResponse;
import com.bank.frauddetection.model.Account;
import com.bank.frauddetection.model.AccountLink;
import com.bank.frauddetection.model.FraudScore;
import com.bank.frauddetection.repository.AccountLinkRepository;
import com.bank.frauddetection.repository.AccountRepository;
import com.bank.frauddetection.repository.FraudScoreRepository;
import com.bank.frauddetection.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskScoringService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountLinkRepository accountLinkRepository;
    private final FraudScoreRepository fraudScoreRepository;
    private final MuleGraphService muleGraphService;

    public RuleScoreResult computeRuleScore(String accountId) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        List<String> reasons = new ArrayList<>();

        int velocityScore = calculateVelocityScore(accountId, reasons);
        int graphDepthScore = calculateGraphDepthScore(accountId, reasons); //hop distance from known fraud nodes
        int behaviorScore = calculateBehaviorScore(accountId, account, reasons); // pass-through ratio, forwarding time, legitimate credits
        int deviceScore = calculateDeviceScore(account, reasons);

        boolean circularFlowDetected = muleGraphService.hasCircularFlow(accountId);
        boolean fanOutWithCircularDetected = muleGraphService.hasFanOutWithCircularFlow(accountId);
        boolean multiLevelFanOutDetected = muleGraphService.hasMultiLevelFanOut(accountId);
        int fanOutCount = muleGraphService.getFanOutCount(accountId);
        int hopDistance = muleGraphService.distanceFromKnownFraudNode(accountId);

        if (circularFlowDetected) {
            graphDepthScore = Math.max(graphDepthScore, 25);
            reasons.add("Circular money flow detected within 5 hops");
        }

        // CRITICAL: Fan-out with return (money laundering pattern)
        if (fanOutWithCircularDetected) {
            graphDepthScore = Math.max(graphDepthScore, 30);  // Extra penalty for sophisticated pattern
            reasons.add("CRITICAL: Fan-out with circular flow detected - funds split across multiple accounts and returned");
        }

        // HIGH RISK: Multi-level fan-out through intermediaries
        if (multiLevelFanOutDetected) {
            graphDepthScore = Math.max(graphDepthScore, 28);
            reasons.add("Multi-level fan-out detected: funds distributed through intermediary accounts");
        }

        if (fanOutCount >= 5) {
            velocityScore = Math.max(velocityScore, 20);
            reasons.add("Fan-out pattern detected: account sent to 5+ accounts within 10 minutes");
        }

        int ruleScore = Math.min(100, velocityScore + graphDepthScore + behaviorScore + deviceScore);

        return RuleScoreResult.builder()
                .ruleScore(ruleScore)
                .velocityScore(velocityScore)
                .graphDepthScore(graphDepthScore)
                .behaviorScore(behaviorScore)
                .deviceScore(deviceScore)
                .reasons(reasons)
                .circularFlowDetected(circularFlowDetected)
                .fanOutCount(fanOutCount)
                .hopDistanceFromFraudNode(hopDistance)
                .build();
    }

    public FraudScore saveFinalScore(
            String accountId,
            int finalScore,
            String riskLevel,
            RuleScoreResult ruleScoreResult,
            MlRiskResponse mlResponse
    ) {
        List<String> reasons = new ArrayList<>(ruleScoreResult.getReasons());
        if (mlResponse.getMlReasons() != null) {
            reasons.addAll(mlResponse.getMlReasons());
        }

        return fraudScoreRepository.save(FraudScore.builder()
                .accountId(accountId)
                .riskScore(finalScore)
                .riskLevel(riskLevel)
                .ruleScore(ruleScoreResult.getRuleScore())
                .mlScore(mlResponse.getMlScore())
                .velocityScore(ruleScoreResult.getVelocityScore())
                .graphDepthScore(ruleScoreResult.getGraphDepthScore())
                .behaviorScore(ruleScoreResult.getBehaviorScore())
                .deviceScore(ruleScoreResult.getDeviceScore())
                .reasons(reasons)
                .computedAt(LocalDateTime.now())
                .build());
    }

    private int calculateVelocityScore(String accountId, List<String> reasons) {
        long txnCount = transactionRepository.countByToAccountIdAndTimestampAfter(
                accountId,
                LocalDateTime.now().minusHours(1)
        );

        if (txnCount > 10) {
            reasons.add("High transaction velocity: more than 10 incoming transactions in last 1 hour");
            return 25;
        }

        if (txnCount > 5) {
            reasons.add("Moderate transaction velocity: more than 5 incoming transactions in last 1 hour");
            return 15;
        }

        return 5;
    }

    private int calculateGraphDepthScore(String accountId, List<String> reasons) {
        int hopDistance = muleGraphService.distanceFromKnownFraudNode(accountId);

        if (hopDistance == 1) {
            reasons.add("Account is directly connected to a known fraud node");
            return 25;
        }

        if (hopDistance >= 2 && hopDistance <= 3) {
            reasons.add("Account is 2-3 hops away from known fraud network");
            return 15;
        }

        if (hopDistance >= 4 && hopDistance <= 5) {
            reasons.add("Account is 4-5 hops away from suspicious network");
            return 8;
        }

        return 0;
    }

    private int calculateBehaviorScore(String accountId, Account account, List<String> reasons) {
        int score = 0;

        double avgForwardingTime = accountLinkRepository.findByFromAccountId(accountId)
                .stream()
                .mapToDouble(AccountLink::getAvgForwardingTime)
                .filter(minutes -> minutes > 0)
                .average()
                .orElse(Double.MAX_VALUE);

        if (account.getPassThroughRatio() > 0.90 && avgForwardingTime <= 30) {
            reasons.add("Pass-through ratio is above 90% and funds are forwarded within 30 minutes");
            score += 25;
        } else if (account.getPassThroughRatio() > 0.90) {
            reasons.add("Pass-through ratio is above 90%");
            score += 20;
        } else if (account.getPassThroughRatio() > 0.70) {
            reasons.add("Pass-through ratio is above 70%");
            score += 15;
        }

        if (!account.isHasLegitimateCredits()) {
            reasons.add("No legitimate salary or merchant credits found");
            score += 10;
        }

        return Math.min(score, 25);
    }

    private int calculateDeviceScore(Account account, List<String> reasons) {
        int linkedDeviceCount = account.getLinkedDevices() == null ? 0 : account.getLinkedDevices().size();

        if (linkedDeviceCount > 3) {
            reasons.add("Account is linked with more than 3 devices");
            return 25;
        }

        if (linkedDeviceCount > 1) {
            reasons.add("Account is linked with multiple devices");
            return 12;
        }

        return 0;
    }
}
