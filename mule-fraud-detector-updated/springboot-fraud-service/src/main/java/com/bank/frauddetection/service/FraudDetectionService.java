package com.bank.frauddetection.service;

import com.bank.frauddetection.dto.FalsePositiveRequest;
import com.bank.frauddetection.dto.MlRiskResponse;
import com.bank.frauddetection.dto.MuleCheckRequest;
import com.bank.frauddetection.dto.MuleCheckResponse;
import com.bank.frauddetection.dto.ResolveFraudCaseRequest;
import com.bank.frauddetection.model.FraudCase;
import com.bank.frauddetection.model.FraudScore;
import com.bank.frauddetection.repository.FraudCaseRepository;
import com.bank.frauddetection.repository.FraudScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final TransactionService transactionService;
    private final RiskScoringService riskScoringService;
    private final MlRiskService mlRiskService;
    private final AlertService alertService;
    private final RiskCacheService riskCacheService;
    private final FraudScoreRepository fraudScoreRepository;
    private final FraudCaseRepository fraudCaseRepository;

    public MuleCheckResponse checkMuleRisk(MuleCheckRequest request) {
        MuleCheckResponse cached = riskCacheService.get(request.getToAccountId());
        if (cached != null) {
            return cached;
        }

        transactionService.saveTransactionAndUpdateGraph(request);

        RuleScoreResult ruleScoreResult = riskScoringService.computeRuleScore(request.getToAccountId());
        MlRiskResponse mlResponse = mlRiskService.getMlRiskScore(request.getToAccountId());

        int finalScore = calculateFinalScore(ruleScoreResult.getRuleScore(), mlResponse.getMlScore());
        String riskLevel = resolveRiskLevel(finalScore);
        String action = resolveAction(riskLevel);

        FraudScore savedScore = riskScoringService.saveFinalScore(
                request.getToAccountId(),
                finalScore,
                riskLevel,
                ruleScoreResult,
                mlResponse
        );

        String referenceId = null;
        if ("CRITICAL".equals(riskLevel)) {
            referenceId = alertService.createFraudCase(savedScore, "BLOCKED");
        }

        MuleCheckResponse response = MuleCheckResponse.builder()
                .accountId(request.getToAccountId())
                .riskScore(finalScore)
                .riskLevel(riskLevel)
                .action(action)
                .ruleScore(ruleScoreResult.getRuleScore())
                .mlScore(mlResponse.getMlScore())
                .referenceId(referenceId)
                .reasons(savedScore.getReasons())
                .recommendation(resolveRecommendation(riskLevel))
                .build();

        riskCacheService.save(request.getToAccountId(), response);
        return response;
    }

    public FraudScore getLatestRiskScore(String accountId) {
        return fraudScoreRepository.findTopByAccountIdOrderByComputedAtDesc(accountId)
                .orElseThrow(() -> new IllegalArgumentException("No risk score found for accountId: " + accountId));
    }

    public List<FraudCase> getFraudCases(String status) {
        return fraudCaseRepository.findByStatus(status);
    }

    public FraudCase reportFalsePositive(FalsePositiveRequest request) {
        FraudCase fraudCase = fraudCaseRepository.findByReferenceId(request.getReferenceId())
                .orElseThrow(() -> new IllegalArgumentException("Fraud case not found: " + request.getReferenceId()));

        fraudCase.setStatus("FALSE_POSITIVE");
        fraudCase.setReportedBy(request.getReportedBy());
        fraudCase.setFalsePositiveReason(request.getReason());
        fraudCase.setFalsePositiveReportedAt(LocalDateTime.now());
        fraudCase.setResolvedAt(LocalDateTime.now());

        FraudCase saved = fraudCaseRepository.save(fraudCase);
        /*riskCacheService.invalidate(saved.getAccountId());*/
        return saved;
    }

    public FraudCase resolveFraudCase(String referenceId, ResolveFraudCaseRequest request) {
        FraudCase fraudCase = fraudCaseRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new IllegalArgumentException("Fraud case not found: " + referenceId));

        fraudCase.setStatus("RESOLVED");
        fraudCase.setResolvedBy(request.getResolvedBy());
        fraudCase.setResolutionNote(request.getResolutionNote());
        fraudCase.setResolvedAt(LocalDateTime.now());

        FraudCase saved = fraudCaseRepository.save(fraudCase);
        /*riskCacheService.invalidate(saved.getAccountId());*/
        return saved;
    }

    private int calculateFinalScore(int ruleScore, int mlScore) {
        return Math.min(100, (int) Math.round((ruleScore * 0.6) + (mlScore * 0.4)));
    }

    private String resolveRiskLevel(int score) {
        if (score <= 30) return "LOW";
        if (score <= 60) return "MEDIUM";
        if (score <= 80) return "HIGH";
        return "CRITICAL";
    }

    private String resolveAction(String riskLevel) {
        switch (riskLevel) {
            case "LOW": return "ALLOW";
            case "MEDIUM": return "WARN";
            case "HIGH": return "STEP_UP_AUTH";
            case "CRITICAL": return "BLOCK";
            default: return "REVIEW";
        }
    }

    private String resolveRecommendation(String riskLevel) {
        switch (riskLevel) {
            case "LOW": return "Allow transaction";
            case "MEDIUM": return "Show warning before proceeding";
            case "HIGH": return "Require biometric or OTP step-up";
            case "CRITICAL": return "Block transaction and create fraud case";
            default: return "Manual review required";
        }
    }
}
