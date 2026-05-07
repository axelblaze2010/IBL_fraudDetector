package com.bank.frauddetection.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "fraud_scores")
public class FraudScore {
    @Id private String id;
    @Indexed private String accountId;
    private int riskScore;
    private String riskLevel;
    private int ruleScore;
    private int mlScore;
    private int velocityScore;
    private int graphDepthScore;
    private int behaviorScore;
    private int deviceScore;
    private List<String> reasons;
    @Indexed private LocalDateTime computedAt;
}
