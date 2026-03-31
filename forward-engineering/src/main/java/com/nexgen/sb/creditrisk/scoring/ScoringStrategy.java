package com.nexgen.sb.creditrisk.scoring;

/**
 * Strategy interface for credit risk scoring algorithms.
 */
public interface ScoringStrategy {

    String getStrategyName();

    String categorizeRisk(int bureauScore, double debtToIncomeRatio, double utilizationRate);

    int calculateOverallScore(int bureauScore, double debtToIncomeRatio,
                              double utilizationRate, String employmentStatus);

    String determineRecommendation(String riskCategory, double requestedAmount, double annualIncome);

    String determineAffordabilityRating(double debtServiceRatio);
}
