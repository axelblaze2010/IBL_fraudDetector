package com.bank.frauddetection.dto;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class MuleCheckResponse implements Serializable {
    private String accountId;
    private int riskScore;
    private String riskLevel;
    private String action;
    private int ruleScore;
    private int mlScore;
    private String referenceId;
    private List<String> reasons;
    private String recommendation;
}
