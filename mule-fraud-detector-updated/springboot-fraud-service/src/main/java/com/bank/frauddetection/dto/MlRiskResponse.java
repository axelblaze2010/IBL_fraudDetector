package com.bank.frauddetection.dto;

import lombok.Data;
import java.util.List;

@Data
public class MlRiskResponse {
    private int mlScore;
    private double fraudProbability;
    private List<String> mlReasons;
}
