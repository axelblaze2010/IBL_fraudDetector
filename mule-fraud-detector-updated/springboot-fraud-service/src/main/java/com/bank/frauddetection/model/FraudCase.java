package com.bank.frauddetection.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fraud_cases")
public class FraudCase {

    @Id
    private String id;

    @Indexed(unique = true)
    private String referenceId;

    @Indexed
    private String accountId;

    private int riskScore;
    private String action;
    private List<String> reasons;

    @Indexed
    private String status; // OPEN | RESOLVED | FALSE_POSITIVE

    private String resolvedBy;
    private String resolutionNote;
    private String falsePositiveReason;
    private String reportedBy;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime falsePositiveReportedAt;
}
