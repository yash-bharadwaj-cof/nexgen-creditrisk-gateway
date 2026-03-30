package com.nexgen.esb.creditrisk.scoring;

import com.nexgen.esb.creditrisk.model.CreditRiskResType;

/**
 * Strategy interface for credit risk scoring algorithms.
 * This is the key design pattern difference from the original service:
 * the original used a single hardcoded scoring approach, while this
 * uses the Strategy pattern to allow different scoring algorithms.
 */
public interface ScoringStrategy {

    String getStrategyName();

    String categorizeRisk(int bureauScore, double debtToIncomeRatio, double utilizationRate);

    int calculateOverallScore(int bureauScore, double debtToIncomeRatio,
                              double utilizationRate, String employmentStatus);

    String determineRecommendation(String riskCategory, double requestedAmount, double annualIncome);

    String determineAffordabilityRating(double debtServiceRatio);
}
