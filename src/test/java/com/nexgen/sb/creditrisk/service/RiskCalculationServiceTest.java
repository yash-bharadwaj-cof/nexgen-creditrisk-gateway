package com.nexgen.sb.creditrisk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import com.nexgen.esb.creditrisk.model.CreditScoreDetail;
import com.nexgen.esb.creditrisk.model.RequestHeader;
import com.nexgen.esb.creditrisk.scoring.StandardScoringStrategy;

class RiskCalculationServiceTest {

    private RiskCalculationService service;
    private StandardScoringStrategy strategy;

    @BeforeEach
    void setUp() {
        service = new RiskCalculationService();
        strategy = new StandardScoringStrategy();
    }

    // --- Bureau error path ---

    @Test
    void testBureauError_setsIndeterminateResponse() {
        CreditRiskReqType request = buildRequest("APP-001", 60000.0, 25000.0, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(720, 0.30, 0, 2, 50000.0, 15000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, true);

        assertNotNull(response);
        assertEquals("INDETERMINATE", response.getRiskCategory());
        assertEquals(0, response.getOverallScore());
        assertEquals("REFER_TO_UNDERWRITER", response.getRecommendation());
        assertEquals("LM", response.getAccuracyCode());
        assertNotNull(response.getResponseHeader());
        assertEquals("W215E", response.getResponseHeader().getStatusCode());
        assertEquals("STANDARD-v1.0", response.getScoringModelVersion());
    }

    // --- Success path ---

    @Test
    void testSuccessPath_returnsFullDataResponse() {
        CreditRiskReqType request = buildRequest("APP-002", 80000.0, 20000.0, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(760, 0.20, 0, 1, 100000.0, 20000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);

        assertNotNull(response);
        assertEquals("FD", response.getAccuracyCode());
        assertEquals("SUCCESS", response.getResponseHeader().getStatusCode());
        assertNotNull(response.getIncomeVerification());
        assertNotNull(response.getEmploymentRisk());
        assertNotNull(response.getDebtService());
        assertNotNull(response.getRiskFactors());
    }

    // --- Employment risk mapping ---

    @Test
    void testEmploymentRisk_fullTime_isLow() {
        CreditRiskResType response = calculateWithEmployment("FULL_TIME");
        assertEquals("LOW", response.getEmploymentRisk().getRiskLevel());
    }

    @Test
    void testEmploymentRisk_partTime_isModerate() {
        CreditRiskResType response = calculateWithEmployment("PART_TIME");
        assertEquals("MODERATE", response.getEmploymentRisk().getRiskLevel());
    }

    @Test
    void testEmploymentRisk_retired_isModerate() {
        CreditRiskResType response = calculateWithEmployment("RETIRED");
        assertEquals("MODERATE", response.getEmploymentRisk().getRiskLevel());
    }

    @Test
    void testEmploymentRisk_selfEmployed_isModerateHigh() {
        CreditRiskResType response = calculateWithEmployment("SELF_EMPLOYED");
        assertEquals("MODERATE_HIGH", response.getEmploymentRisk().getRiskLevel());
    }

    @Test
    void testEmploymentRisk_contract_isModerateHigh() {
        CreditRiskResType response = calculateWithEmployment("CONTRACT");
        assertEquals("MODERATE_HIGH", response.getEmploymentRisk().getRiskLevel());
    }

    @Test
    void testEmploymentRisk_unemployed_isHigh() {
        CreditRiskResType response = calculateWithEmployment("UNEMPLOYED");
        assertEquals("HIGH", response.getEmploymentRisk().getRiskLevel());
    }

    // --- Risk factor identification thresholds ---

    @Test
    void testRiskFactor_lowCreditScore_belowThreshold() {
        CreditRiskReqType request = buildRequest("APP-003", 60000.0, 10000.0, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(640, 0.30, 0, 2, 50000.0, 15000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);
        assertTrue(response.getRiskFactors().contains("LOW_CREDIT_SCORE"));
    }

    @Test
    void testRiskFactor_highCreditUtilization_aboveThreshold() {
        CreditRiskReqType request = buildRequest("APP-004", 60000.0, 10000.0, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.55, 0, 2, 50000.0, 27500.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);
        assertTrue(response.getRiskFactors().contains("HIGH_CREDIT_UTILIZATION"));
    }

    @Test
    void testRiskFactor_pastDelinquencies() {
        CreditRiskReqType request = buildRequest("APP-005", 60000.0, 10000.0, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 1, 2, 50000.0, 15000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);
        assertTrue(response.getRiskFactors().contains("PAST_DELINQUENCIES"));
    }

    @Test
    void testRiskFactor_excessiveCreditInquiries_aboveFive() {
        CreditRiskReqType request = buildRequest("APP-006", 60000.0, 10000.0, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 0, 6, 50000.0, 15000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);
        assertTrue(response.getRiskFactors().contains("EXCESSIVE_CREDIT_INQUIRIES"));
    }

    @Test
    void testRiskFactor_highDebtToIncome_aboveThreshold() {
        // Annual income 12000 → monthly 1000; total balance 100000 → monthly debt 3000 → DTI = 3.0
        CreditRiskReqType request = buildRequest("APP-007", 12000.0, 10000.0, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 0, 2, 50000.0, 100000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);
        assertTrue(response.getRiskFactors().contains("HIGH_DEBT_TO_INCOME"));
    }

    @Test
    void testRiskFactor_noEmploymentIncome_unemployed() {
        CreditRiskReqType request = buildRequest("APP-008", 0.0, 10000.0, "UNEMPLOYED");
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 0, 2, 50000.0, 15000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);
        assertTrue(response.getRiskFactors().contains("NO_EMPLOYMENT_INCOME"));
    }

    // --- Income verification detail ---

    @Test
    void testIncomeVerification_selfReportedStatus() {
        CreditRiskReqType request = buildRequest("APP-009", 60000.0, 20000.0, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 0, 2, 50000.0, 12000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);

        assertEquals("SELF_REPORTED", response.getIncomeVerification().getVerificationStatus());
        assertEquals(60000.0, response.getIncomeVerification().getReportedIncome());
    }

    @Test
    void testIncomeVerification_dtiCalculation() {
        // monthlyIncome = 60000/12 = 5000; monthlyDebt = 12000*0.03 = 360; DTI = 360/5000 = 0.072
        CreditRiskReqType request = buildRequest("APP-010", 60000.0, 20000.0, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 0, 2, 50000.0, 12000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);

        double expectedDti = (12000.0 * 0.03) / (60000.0 / 12.0);
        assertEquals(expectedDti, response.getIncomeVerification().getDebtToIncomeRatio(), 0.0001);
    }

    @Test
    void testIncomeVerification_zeroIncome_dtiDefaultsToOne() {
        CreditRiskReqType request = buildRequest("APP-011", 0.0, 20000.0, "UNEMPLOYED");
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 0, 2, 50000.0, 12000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);

        assertEquals(1.0, response.getIncomeVerification().getDebtToIncomeRatio(), 0.0001);
    }

    // --- Debt service detail ---

    @Test
    void testDebtService_hardcodedFactors() {
        // monthlyDebt = totalBalance * 0.03; requestMonthly = requestedAmount * 0.006
        double totalBalance = 20000.0;
        double annualIncome = 60000.0;
        double requestedAmount = 15000.0;

        CreditRiskReqType request = buildRequest("APP-012", annualIncome, requestedAmount, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 0, 2, 50000.0, totalBalance);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);

        double monthlyIncome = annualIncome / 12.0;
        double monthlyDebt = totalBalance * 0.03;
        double expectedDsr = monthlyDebt / monthlyIncome;
        double expectedTdsr = (monthlyDebt + requestedAmount * 0.006) / monthlyIncome;

        assertEquals(expectedDsr, response.getDebtService().getDebtServiceRatio(), 0.0001);
        assertEquals(expectedTdsr, response.getDebtService().getTotalDebtServiceRatio(), 0.0001);
    }

    // --- Scoring model version ---

    @Test
    void testScoringModelVersion_appendsV1() {
        CreditRiskReqType request = buildRequest("APP-013", 60000.0, 20000.0, "FULL_TIME");
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 0, 2, 50000.0, 12000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);

        assertEquals("STANDARD-v1.0", response.getScoringModelVersion());
    }

    @Test
    void testResponseHeader_containsTransactionId() {
        RequestHeader reqHeader = new RequestHeader();
        reqHeader.setTransactionId("TXN-999");

        CreditRiskReqType request = buildRequest("APP-014", 60000.0, 20000.0, "FULL_TIME");
        request.setRequestHeader(reqHeader);
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 0, 2, 50000.0, 12000.0);

        CreditRiskResType response = service.calculate(request, creditDetail, strategy, false);

        assertEquals("TXN-999", response.getResponseHeader().getTransactionId());
    }

    // --- Helpers ---

    private CreditRiskResType calculateWithEmployment(String employmentStatus) {
        CreditRiskReqType request = buildRequest("APP-EMP", 60000.0, 20000.0, employmentStatus);
        CreditScoreDetail creditDetail = buildCreditDetail(700, 0.30, 0, 2, 50000.0, 12000.0);
        return service.calculate(request, creditDetail, strategy, false);
    }

    private CreditRiskReqType buildRequest(String applicantId, Double annualIncome,
                                            Double requestedAmount, String employmentStatus) {
        CreditRiskReqType request = new CreditRiskReqType();
        request.setApplicantId(applicantId);
        request.setAnnualIncome(annualIncome);
        request.setRequestedAmount(requestedAmount);
        request.setEmploymentStatus(employmentStatus);
        return request;
    }

    private CreditScoreDetail buildCreditDetail(Integer bureauScore, Double utilizationRate,
                                                 Integer delinquencyCount, Integer inquiryCount,
                                                 Double totalCreditLimit, Double totalBalance) {
        CreditScoreDetail detail = new CreditScoreDetail();
        detail.setBureauScore(bureauScore);
        detail.setUtilizationRate(utilizationRate);
        detail.setDelinquencyCount(delinquencyCount);
        detail.setInquiryCount(inquiryCount);
        detail.setTotalCreditLimit(totalCreditLimit);
        detail.setTotalBalance(totalBalance);
        return detail;
    }
}
