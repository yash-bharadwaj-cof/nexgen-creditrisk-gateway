package com.nexgen.sb.creditrisk.scoring;

import org.springframework.stereotype.Component;

/**
 * Standard scoring strategy — balanced thresholds.
 * Weights: Bureau 40%, DTI 30%, Utilization 20%, Employment 10%.
 * Thresholds: EXCELLENT>=750, GOOD>=680, FAIR>=620, POOR>=560.
 * DTI multiplier: 200. Util multiplier: 150.
 */
@Component
public class StandardScoringStrategy implements ScoringStrategy {

    private static final int WEIGHT_BUREAU = 40;
    private static final int WEIGHT_DTI = 30;
    private static final int WEIGHT_UTILIZATION = 20;
    private static final int WEIGHT_EMPLOYMENT = 10;

    @Override
    public String getStrategyName() {
        return "STANDARD";
    }

    @Override
    public String categorizeRisk(int bureauScore, double debtToIncomeRatio, double utilizationRate) {
        if (bureauScore >= 750 && debtToIncomeRatio < 0.30 && utilizationRate < 0.30) {
            return "EXCELLENT";
        } else if (bureauScore >= 680) {
            return "GOOD";
        } else if (bureauScore >= 620) {
            return "FAIR";
        } else if (bureauScore >= 560) {
            return "POOR";
        }
        return "VERY_POOR";
    }

    @Override
    public int calculateOverallScore(int bureauScore, double debtToIncomeRatio,
                                     double utilizationRate, String employmentStatus) {
        double normalizedBureau = Math.min(100.0, (bureauScore - 300.0) / 550.0 * 100.0);
        double normalizedDTI = Math.max(0, 100.0 - (debtToIncomeRatio * 200.0));
        double normalizedUtil = Math.max(0, 100.0 - (utilizationRate * 150.0));
        double employmentScore = calculateEmploymentScore(employmentStatus);

        double weighted = (normalizedBureau * WEIGHT_BUREAU +
                          normalizedDTI * WEIGHT_DTI +
                          normalizedUtil * WEIGHT_UTILIZATION +
                          employmentScore * WEIGHT_EMPLOYMENT) / 100.0;

        return (int) Math.round(Math.max(0, Math.min(100, weighted)));
    }

    @Override
    public String determineRecommendation(String riskCategory, double requestedAmount, double annualIncome) {
        double ratio = requestedAmount / Math.max(1.0, annualIncome);

        switch (riskCategory) {
            case "EXCELLENT":
                return "APPROVE";
            case "GOOD":
                return ratio > 5.0 ? "APPROVE_WITH_CONDITIONS" : "APPROVE";
            case "FAIR":
                return ratio > 3.0 ? "REFER_TO_UNDERWRITER" : "APPROVE_WITH_CONDITIONS";
            case "POOR":
                return "REFER_TO_UNDERWRITER";
            case "VERY_POOR":
                return "DECLINE";
            default:
                return "REFER_TO_UNDERWRITER";
        }
    }

    @Override
    public String determineAffordabilityRating(double debtServiceRatio) {
        if (debtServiceRatio < 0.28) return "COMFORTABLE";
        if (debtServiceRatio < 0.36) return "MANAGEABLE";
        if (debtServiceRatio < 0.44) return "STRETCHED";
        return "OVEREXTENDED";
    }

    private double calculateEmploymentScore(String employmentStatus) {
        if (employmentStatus == null) return 50.0;
        switch (employmentStatus.toUpperCase()) {
            case "FULL_TIME":     return 100.0;
            case "PART_TIME":     return 70.0;
            case "SELF_EMPLOYED": return 65.0;
            case "CONTRACT":      return 60.0;
            case "RETIRED":       return 80.0;
            case "STUDENT":       return 40.0;
            case "UNEMPLOYED":    return 10.0;
            default:              return 50.0;
        }
    }
}
