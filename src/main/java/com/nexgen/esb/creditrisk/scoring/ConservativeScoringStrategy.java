package com.nexgen.esb.creditrisk.scoring;

/**
 * Conservative scoring strategy - tighter thresholds for secured products.
 * Used for mortgages and auto loans where higher standards apply.
 */
public class ConservativeScoringStrategy implements ScoringStrategy {

    @Override
    public String getStrategyName() {
        return "CONSERVATIVE";
    }

    @Override
    public String categorizeRisk(int bureauScore, double debtToIncomeRatio, double utilizationRate) {
        if (bureauScore >= 780 && debtToIncomeRatio < 0.25 && utilizationRate < 0.25) {
            return "EXCELLENT";
        } else if (bureauScore >= 720 && debtToIncomeRatio < 0.35) {
            return "GOOD";
        } else if (bureauScore >= 660) {
            return "FAIR";
        } else if (bureauScore >= 600) {
            return "POOR";
        }
        return "VERY_POOR";
    }

    @Override
    public int calculateOverallScore(int bureauScore, double debtToIncomeRatio,
                                     double utilizationRate, String employmentStatus) {
        double normalizedBureau = Math.min(100.0, (bureauScore - 300.0) / 550.0 * 100.0);
        double normalizedDTI = Math.max(0, 100.0 - (debtToIncomeRatio * 250.0));
        double normalizedUtil = Math.max(0, 100.0 - (utilizationRate * 200.0));
        double employmentScore = calculateEmploymentScore(employmentStatus);

        double weighted = (normalizedBureau * 35 +
                          normalizedDTI * 35 +
                          normalizedUtil * 15 +
                          employmentScore * 15) / 100.0;

        return (int) Math.round(Math.max(0, Math.min(100, weighted)));
    }

    @Override
    public String determineRecommendation(String riskCategory, double requestedAmount, double annualIncome) {
        double ratio = requestedAmount / Math.max(1.0, annualIncome);

        switch (riskCategory) {
            case "EXCELLENT":
                return ratio > 5.0 ? "APPROVE_WITH_CONDITIONS" : "APPROVE";
            case "GOOD":
                return ratio > 4.0 ? "REFER_TO_UNDERWRITER" : "APPROVE_WITH_CONDITIONS";
            case "FAIR":
                return "REFER_TO_UNDERWRITER";
            case "POOR":
            case "VERY_POOR":
                return "DECLINE";
            default:
                return "REFER_TO_UNDERWRITER";
        }
    }

    @Override
    public String determineAffordabilityRating(double debtServiceRatio) {
        if (debtServiceRatio < 0.25) return "COMFORTABLE";
        if (debtServiceRatio < 0.32) return "MANAGEABLE";
        if (debtServiceRatio < 0.40) return "STRETCHED";
        return "OVEREXTENDED";
    }

    private double calculateEmploymentScore(String employmentStatus) {
        if (employmentStatus == null) return 40.0;
        switch (employmentStatus.toUpperCase()) {
            case "FULL_TIME": return 100.0;
            case "PART_TIME": return 55.0;
            case "SELF_EMPLOYED": return 50.0;
            case "CONTRACT": return 45.0;
            case "RETIRED": return 75.0;
            case "STUDENT": return 20.0;
            case "UNEMPLOYED": return 0.0;
            default: return 40.0;
        }
    }
}
