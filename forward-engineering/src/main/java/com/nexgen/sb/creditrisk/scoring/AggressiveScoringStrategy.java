package com.nexgen.sb.creditrisk.scoring;

import org.springframework.stereotype.Component;

/**
 * Aggressive scoring strategy — relaxed thresholds for unsecured products.
 * Used for credit cards and lines of credit.
 * Weights: Bureau 50%, DTI 25%, Utilization 15%, Employment 10%.
 * Thresholds: EXCELLENT>=720, GOOD>=650, FAIR>=580, POOR>=520.
 * DTI multiplier: 150. Util multiplier: 120.
 */
@Component
public class AggressiveScoringStrategy implements ScoringStrategy {

    private static final int WEIGHT_BUREAU = 50;
    private static final int WEIGHT_DTI = 25;
    private static final int WEIGHT_UTILIZATION = 15;
    private static final int WEIGHT_EMPLOYMENT = 10;

    @Override
    public String getStrategyName() {
        return "AGGRESSIVE";
    }

    @Override
    public String categorizeRisk(int bureauScore, double debtToIncomeRatio, double utilizationRate) {
        if (bureauScore >= 720 && debtToIncomeRatio < 0.40) {
            return "EXCELLENT";
        } else if (bureauScore >= 650) {
            return "GOOD";
        } else if (bureauScore >= 580) {
            return "FAIR";
        } else if (bureauScore >= 520) {
            return "POOR";
        }
        return "VERY_POOR";
    }

    @Override
    public int calculateOverallScore(int bureauScore, double debtToIncomeRatio,
                                     double utilizationRate, String employmentStatus) {
        double normalizedBureau = Math.min(100.0, (bureauScore - 300.0) / 550.0 * 100.0);
        double normalizedDTI = Math.max(0, 100.0 - (debtToIncomeRatio * 150.0));
        double normalizedUtil = Math.max(0, 100.0 - (utilizationRate * 120.0));
        double employmentScore = calculateEmploymentScore(employmentStatus);

        double weighted = (normalizedBureau * WEIGHT_BUREAU +
                          normalizedDTI * WEIGHT_DTI +
                          normalizedUtil * WEIGHT_UTILIZATION +
                          employmentScore * WEIGHT_EMPLOYMENT) / 100.0;

        return (int) Math.round(Math.max(0, Math.min(100, weighted)));
    }

    @Override
    public String determineRecommendation(String riskCategory, double requestedAmount, double annualIncome) {
        switch (riskCategory) {
            case "EXCELLENT":
            case "GOOD":
                return "APPROVE";
            case "FAIR":
                return "APPROVE_WITH_CONDITIONS";
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
        if (debtServiceRatio < 0.35) return "COMFORTABLE";
        if (debtServiceRatio < 0.45) return "MANAGEABLE";
        if (debtServiceRatio < 0.55) return "STRETCHED";
        return "OVEREXTENDED";
    }

    private double calculateEmploymentScore(String employmentStatus) {
        if (employmentStatus == null) return 60.0;
        switch (employmentStatus.toUpperCase()) {
            case "FULL_TIME":     return 100.0;
            case "PART_TIME":     return 75.0;
            case "SELF_EMPLOYED": return 70.0;
            case "CONTRACT":      return 65.0;
            case "RETIRED":       return 85.0;
            case "STUDENT":       return 50.0;
            case "UNEMPLOYED":    return 20.0;
            default:              return 60.0;
        }
    }
}
