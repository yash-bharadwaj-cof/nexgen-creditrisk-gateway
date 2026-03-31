package com.nexgen.sb.creditrisk.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import com.nexgen.esb.creditrisk.model.CreditScoreDetail;
import com.nexgen.esb.creditrisk.model.DebtServiceDetail;
import com.nexgen.esb.creditrisk.model.EmploymentRiskDetail;
import com.nexgen.esb.creditrisk.model.IncomeVerificationDetail;
import com.nexgen.esb.creditrisk.model.ResponseHeader;
import com.nexgen.esb.creditrisk.scoring.ScoringStrategy;
import com.nexgen.esb.creditrisk.scoring.StandardScoringStrategy;

/**
 * Applies the selected scoring strategy to produce the final credit risk assessment.
 * Migrated from legacy {@code RiskScoreCalculator} Camel Processor.
 */
@Service
public class RiskCalculationService {

    private static final Logger LOG = LoggerFactory.getLogger(RiskCalculationService.class);
    private static final SimpleDateFormat TIMESTAMP_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final ScoringStrategy STANDARD_STRATEGY = new StandardScoringStrategy();

    /**
     * Calculates the credit risk assessment result.
     *
     * @param request      validated credit risk request
     * @param creditDetail bureau credit score details (may be {@code null} when {@code bureauError} is true)
     * @param strategy     scoring strategy resolved for this request
     * @param bureauError  {@code true} if the bureau call failed or returned an error
     * @return populated {@link CreditRiskResType}
     */
    public CreditRiskResType calculate(CreditRiskReqType request,
                                       CreditScoreDetail creditDetail,
                                       ScoringStrategy strategy,
                                       boolean bureauError) {
        CreditRiskResType response = new CreditRiskResType();

        ResponseHeader header = new ResponseHeader();
        header.setTransactionId(request.getRequestHeader() != null
                ? request.getRequestHeader().getTransactionId()
                : UUID.randomUUID().toString());
        header.setTimestamp(TIMESTAMP_FORMAT.format(new Date()));

        response.setResponseHeader(header);
        response.setApplicantId(request.getApplicantId());
        response.setScoringModelVersion(strategy.getStrategyName() + "-v1.0");

        if (bureauError) {
            header.setStatusCode("W215E");
            header.setStatusMessage("Bureau data unavailable - limited assessment");
            response.setRiskCategory("INDETERMINATE");
            response.setOverallScore(0);
            response.setRecommendation("REFER_TO_UNDERWRITER");
            response.setAccuracyCode("LM");
        } else {
            header.setStatusCode("SUCCESS");
            header.setStatusMessage("Assessment completed successfully");

            response.setCreditScoreDetail(creditDetail);

            IncomeVerificationDetail incomeDetail = buildIncomeVerification(request, creditDetail);
            response.setIncomeVerification(incomeDetail);

            EmploymentRiskDetail empDetail = buildEmploymentRisk(request);
            response.setEmploymentRisk(empDetail);

            DebtServiceDetail debtDetail = buildDebtService(request, creditDetail);
            response.setDebtService(debtDetail);

            double dti = incomeDetail.getDebtToIncomeRatio() != null
                    ? incomeDetail.getDebtToIncomeRatio() : 0.5;
            double utilRate = creditDetail.getUtilizationRate() != null
                    ? creditDetail.getUtilizationRate() : 0.5;
            int bureauScore = creditDetail.getBureauScore() != null
                    ? creditDetail.getBureauScore() : 0;

            String riskCategory = strategy.categorizeRisk(bureauScore, dti, utilRate);
            response.setRiskCategory(riskCategory);

            int overallScore = strategy.calculateOverallScore(
                    bureauScore, dti, utilRate, request.getEmploymentStatus());
            response.setOverallScore(overallScore);

            double requestedAmt = request.getRequestedAmount() != null
                    ? request.getRequestedAmount() : 0.0;
            double annualIncome = request.getAnnualIncome() != null
                    ? request.getAnnualIncome() : 0.0;
            String recommendation = strategy.determineRecommendation(riskCategory, requestedAmt, annualIncome);
            response.setRecommendation(recommendation);

            response.setRiskFactors(identifyRiskFactors(creditDetail, incomeDetail, request));
            response.setAccuracyCode("FD");
        }

        LOG.info("Risk assessment completed. Category: {}, Score: {}, Recommendation: {}",
                response.getRiskCategory(), response.getOverallScore(), response.getRecommendation());
        return response;
    }

    private IncomeVerificationDetail buildIncomeVerification(CreditRiskReqType request,
                                                              CreditScoreDetail creditDetail) {
        IncomeVerificationDetail detail = new IncomeVerificationDetail();
        detail.setReportedIncome(request.getAnnualIncome());
        detail.setVerifiedIncome(request.getAnnualIncome());
        detail.setVerificationStatus("SELF_REPORTED");
        detail.setIncomeSource(request.getEmploymentStatus());

        double monthlyIncome = request.getAnnualIncome() != null
                ? request.getAnnualIncome() / 12.0 : 0.0;
        double monthlyDebt = creditDetail.getTotalBalance() != null
                ? creditDetail.getTotalBalance() * 0.03 : 0.0;
        detail.setDebtToIncomeRatio(monthlyIncome > 0 ? monthlyDebt / monthlyIncome : 1.0);
        return detail;
    }

    private EmploymentRiskDetail buildEmploymentRisk(CreditRiskReqType request) {
        EmploymentRiskDetail detail = new EmploymentRiskDetail();
        detail.setEmploymentType(request.getEmploymentStatus());

        String empStatus = request.getEmploymentStatus();
        if ("FULL_TIME".equalsIgnoreCase(empStatus)) {
            detail.setRiskLevel("LOW");
        } else if ("PART_TIME".equalsIgnoreCase(empStatus) || "RETIRED".equalsIgnoreCase(empStatus)) {
            detail.setRiskLevel("MODERATE");
        } else if ("SELF_EMPLOYED".equalsIgnoreCase(empStatus) || "CONTRACT".equalsIgnoreCase(empStatus)) {
            detail.setRiskLevel("MODERATE_HIGH");
        } else {
            detail.setRiskLevel("HIGH");
        }
        return detail;
    }

    private DebtServiceDetail buildDebtService(CreditRiskReqType request, CreditScoreDetail creditDetail) {
        DebtServiceDetail detail = new DebtServiceDetail();
        double monthlyIncome = request.getAnnualIncome() != null
                ? request.getAnnualIncome() / 12.0 : 0.0;
        double monthlyDebt = creditDetail.getTotalBalance() != null
                ? creditDetail.getTotalBalance() * 0.03 : 0.0;

        detail.setTotalMonthlyIncome(monthlyIncome);
        detail.setTotalMonthlyDebt(monthlyDebt);
        detail.setDebtServiceRatio(monthlyIncome > 0 ? monthlyDebt / monthlyIncome : 1.0);

        double requestMonthly = request.getRequestedAmount() != null
                ? request.getRequestedAmount() * 0.006 : 0.0;
        detail.setTotalDebtServiceRatio(
                monthlyIncome > 0 ? (monthlyDebt + requestMonthly) / monthlyIncome : 1.0);

        detail.setAffordabilityRating(
                STANDARD_STRATEGY.determineAffordabilityRating(detail.getTotalDebtServiceRatio()));
        return detail;
    }

    private List<String> identifyRiskFactors(CreditScoreDetail creditDetail,
                                              IncomeVerificationDetail incomeDetail,
                                              CreditRiskReqType request) {
        List<String> factors = new ArrayList<>();
        if (creditDetail.getBureauScore() != null && creditDetail.getBureauScore() < 650) {
            factors.add("LOW_CREDIT_SCORE");
        }
        if (creditDetail.getUtilizationRate() != null && creditDetail.getUtilizationRate() > 0.50) {
            factors.add("HIGH_CREDIT_UTILIZATION");
        }
        if (creditDetail.getDelinquencyCount() != null && creditDetail.getDelinquencyCount() > 0) {
            factors.add("PAST_DELINQUENCIES");
        }
        if (creditDetail.getInquiryCount() != null && creditDetail.getInquiryCount() > 5) {
            factors.add("EXCESSIVE_CREDIT_INQUIRIES");
        }
        if (incomeDetail.getDebtToIncomeRatio() != null && incomeDetail.getDebtToIncomeRatio() > 0.40) {
            factors.add("HIGH_DEBT_TO_INCOME");
        }
        if ("UNEMPLOYED".equalsIgnoreCase(request.getEmploymentStatus())) {
            factors.add("NO_EMPLOYMENT_INCOME");
        }
        return factors;
    }
}
