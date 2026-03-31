package com.nexgen.esb.creditrisk.processor;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import com.nexgen.esb.creditrisk.model.CreditScoreDetail;
import com.nexgen.esb.creditrisk.model.RequestHeader;
import com.nexgen.esb.creditrisk.scoring.ScoringStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RiskScoreCalculator.
 * Covers full-response builds, bureau-error path, DTI calculation, employment risk,
 * debt-service ratios, and risk factor identification.
 */
@ExtendWith(MockitoExtension.class)
class RiskScoreCalculatorTest {

    private RiskScoreCalculator calculator;

    @Mock
    Exchange exchange;

    @Mock
    Message message;

    @Mock
    ScoringStrategy mockStrategy;

    @BeforeEach
    void setUp() {
        calculator = new RiskScoreCalculator();
        when(exchange.getIn()).thenReturn(message);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private CreditRiskReqType request(String employmentStatus, double annualIncome,
                                      double requestedAmount, String productType) {
        CreditRiskReqType req = new CreditRiskReqType();
        req.setApplicantId("APP-TEST");
        req.setEmploymentStatus(employmentStatus);
        req.setAnnualIncome(annualIncome);
        req.setRequestedAmount(requestedAmount);
        req.setProductType(productType);
        RequestHeader header = new RequestHeader();
        header.setTransactionId("TX-001");
        req.setRequestHeader(header);
        return req;
    }

    private CreditScoreDetail creditDetail(int bureauScore, double totalBalance,
                                           double totalCreditLimit, double utilizationRate,
                                           int delinquencies, int inquiries) {
        CreditScoreDetail detail = new CreditScoreDetail();
        detail.setBureauScore(bureauScore);
        detail.setTotalBalance(totalBalance);
        detail.setTotalCreditLimit(totalCreditLimit);
        detail.setUtilizationRate(utilizationRate);
        detail.setDelinquencyCount(delinquencies);
        detail.setInquiryCount(inquiries);
        return detail;
    }

    private void stubExchange(CreditRiskReqType req, CreditScoreDetail cd, Boolean bureauError) {
        when(exchange.getProperty("VALIDATED_REQUEST")).thenReturn(req);
        when(exchange.getProperty("CREDIT_SCORE_DETAIL")).thenReturn(cd);
        when(exchange.getProperty("SCORING_STRATEGY")).thenReturn(mockStrategy);
        when(exchange.getProperty("BUREAU_ERROR")).thenReturn(bureauError);
    }

    private CreditRiskResType captureResponse() {
        ArgumentCaptor<CreditRiskResType> captor =
                ArgumentCaptor.forClass(CreditRiskResType.class);
        verify(exchange).setProperty(eq("RISK_RESPONSE"), captor.capture());
        return captor.getValue();
    }

    // -----------------------------------------------------------------------
    // Full-response success path
    // -----------------------------------------------------------------------

    @Test
    void testCalculate_Success_BuildsFullResponse() throws Exception {
        CreditRiskReqType req = request("FULL_TIME", 60000, 200000, "MORTGAGE");
        CreditScoreDetail cd  = creditDetail(750, 5000, 20000, 0.25, 0, 2);
        stubExchange(req, cd, Boolean.FALSE);

        when(mockStrategy.getStrategyName()).thenReturn("STANDARD");
        when(mockStrategy.categorizeRisk(anyInt(), anyDouble(), anyDouble())).thenReturn("GOOD");
        when(mockStrategy.calculateOverallScore(anyInt(), anyDouble(), anyDouble(), any()))
                .thenReturn(72);
        when(mockStrategy.determineRecommendation(anyString(), anyDouble(), anyDouble()))
                .thenReturn("APPROVE");

        calculator.process(exchange);

        CreditRiskResType response = captureResponse();
        assertEquals("APP-TEST", response.getApplicantId());
        assertEquals("GOOD", response.getRiskCategory());
        assertEquals(72, response.getOverallScore().intValue());
        assertEquals("APPROVE", response.getRecommendation());
        assertEquals("FD", response.getAccuracyCode());
        assertNotNull(response.getCreditScoreDetail());
        assertNotNull(response.getIncomeVerification());
        assertNotNull(response.getEmploymentRisk());
        assertNotNull(response.getDebtService());
        assertNotNull(response.getResponseHeader());
    }

    // -----------------------------------------------------------------------
    // Bureau error path
    // -----------------------------------------------------------------------

    @Test
    void testCalculate_BureauError_ReturnsIndeterminate() throws Exception {
        CreditRiskReqType req = request("FULL_TIME", 60000, 200000, "MORTGAGE");
        CreditScoreDetail cd  = creditDetail(0, 0, 0, 0, 0, 0);
        stubExchange(req, cd, Boolean.TRUE);
        when(mockStrategy.getStrategyName()).thenReturn("STANDARD");

        calculator.process(exchange);

        CreditRiskResType response = captureResponse();
        assertEquals("INDETERMINATE", response.getRiskCategory());
        assertEquals(0, response.getOverallScore().intValue());
        assertEquals("REFER_TO_UNDERWRITER", response.getRecommendation());
        assertEquals("LM", response.getAccuracyCode());
    }

    // -----------------------------------------------------------------------
    // Income verification / DTI
    // -----------------------------------------------------------------------

    @Test
    void testBuildIncomeVerification_CalculatesDTI() throws Exception {
        // annualIncome=60000 → monthlyIncome=5000
        // totalBalance=10000 → monthlyDebt=10000*0.03=300 → DTI=300/5000=0.06
        CreditRiskReqType req = request("FULL_TIME", 60000, 0, null);
        CreditScoreDetail cd  = creditDetail(720, 10000, 50000, 0.20, 0, 0);
        stubExchange(req, cd, Boolean.FALSE);

        when(mockStrategy.getStrategyName()).thenReturn("STANDARD");
        when(mockStrategy.categorizeRisk(anyInt(), anyDouble(), anyDouble())).thenReturn("GOOD");
        when(mockStrategy.calculateOverallScore(anyInt(), anyDouble(), anyDouble(), any()))
                .thenReturn(70);
        when(mockStrategy.determineRecommendation(anyString(), anyDouble(), anyDouble()))
                .thenReturn("APPROVE");

        calculator.process(exchange);

        CreditRiskResType response = captureResponse();
        assertNotNull(response.getIncomeVerification());
        assertEquals(0.06, response.getIncomeVerification().getDebtToIncomeRatio(), 0.001);
    }

    // -----------------------------------------------------------------------
    // Employment risk
    // -----------------------------------------------------------------------

    @Test
    void testBuildEmploymentRisk_FullTime_Low() throws Exception {
        CreditRiskReqType req = request("FULL_TIME", 80000, 0, null);
        CreditScoreDetail cd  = creditDetail(720, 5000, 20000, 0.25, 0, 0);
        stubExchange(req, cd, Boolean.FALSE);
        when(mockStrategy.getStrategyName()).thenReturn("STANDARD");
        when(mockStrategy.categorizeRisk(anyInt(), anyDouble(), anyDouble())).thenReturn("GOOD");
        when(mockStrategy.calculateOverallScore(anyInt(), anyDouble(), anyDouble(), any()))
                .thenReturn(70);
        when(mockStrategy.determineRecommendation(anyString(), anyDouble(), anyDouble()))
                .thenReturn("APPROVE");

        calculator.process(exchange);

        assertEquals("LOW", captureResponse().getEmploymentRisk().getRiskLevel());
    }

    @Test
    void testBuildEmploymentRisk_Unemployed_High() throws Exception {
        CreditRiskReqType req = request("UNEMPLOYED", 0, 0, null);
        CreditScoreDetail cd  = creditDetail(600, 3000, 10000, 0.30, 0, 0);
        stubExchange(req, cd, Boolean.FALSE);
        when(mockStrategy.getStrategyName()).thenReturn("STANDARD");
        when(mockStrategy.categorizeRisk(anyInt(), anyDouble(), anyDouble())).thenReturn("POOR");
        when(mockStrategy.calculateOverallScore(anyInt(), anyDouble(), anyDouble(), any()))
                .thenReturn(20);
        when(mockStrategy.determineRecommendation(anyString(), anyDouble(), anyDouble()))
                .thenReturn("DECLINE");

        calculator.process(exchange);

        assertEquals("HIGH", captureResponse().getEmploymentRisk().getRiskLevel());
    }

    // -----------------------------------------------------------------------
    // Debt service / TDSR
    // -----------------------------------------------------------------------

    @Test
    void testBuildDebtService_CalculatesTDSR() throws Exception {
        // annualIncome=60000 → monthlyIncome=5000
        // totalBalance=10000 → monthlyDebt=300
        // requestedAmount=10000 → requestMonthly=60
        // TDSR = (300+60)/5000 = 0.072
        CreditRiskReqType req = request("FULL_TIME", 60000, 10000, null);
        CreditScoreDetail cd  = creditDetail(720, 10000, 50000, 0.20, 0, 0);
        stubExchange(req, cd, Boolean.FALSE);
        when(mockStrategy.getStrategyName()).thenReturn("STANDARD");
        when(mockStrategy.categorizeRisk(anyInt(), anyDouble(), anyDouble())).thenReturn("GOOD");
        when(mockStrategy.calculateOverallScore(anyInt(), anyDouble(), anyDouble(), any()))
                .thenReturn(70);
        when(mockStrategy.determineRecommendation(anyString(), anyDouble(), anyDouble()))
                .thenReturn("APPROVE");

        calculator.process(exchange);

        assertNotNull(captureResponse().getDebtService());
        assertEquals(0.072, captureResponse().getDebtService().getTotalDebtServiceRatio(), 0.001);
    }

    // -----------------------------------------------------------------------
    // Risk factors
    // -----------------------------------------------------------------------

    @Test
    void testIdentifyRiskFactors_LowCreditScore() throws Exception {
        CreditRiskReqType req = request("FULL_TIME", 60000, 50000, null);
        CreditScoreDetail cd  = creditDetail(600, 3000, 20000, 0.15, 0, 0);  // score < 650
        stubExchange(req, cd, Boolean.FALSE);
        when(mockStrategy.getStrategyName()).thenReturn("STANDARD");
        when(mockStrategy.categorizeRisk(anyInt(), anyDouble(), anyDouble())).thenReturn("FAIR");
        when(mockStrategy.calculateOverallScore(anyInt(), anyDouble(), anyDouble(), any()))
                .thenReturn(45);
        when(mockStrategy.determineRecommendation(anyString(), anyDouble(), anyDouble()))
                .thenReturn("APPROVE_WITH_CONDITIONS");

        calculator.process(exchange);

        List<String> factors = captureResponse().getRiskFactors();
        assertTrue(factors.contains("LOW_CREDIT_SCORE"),
                "Expected LOW_CREDIT_SCORE in risk factors");
    }

    @Test
    void testIdentifyRiskFactors_HighUtilization() throws Exception {
        CreditRiskReqType req = request("FULL_TIME", 80000, 50000, null);
        CreditScoreDetail cd  = creditDetail(700, 12000, 20000, 0.60, 0, 0);  // util > 0.50
        stubExchange(req, cd, Boolean.FALSE);
        when(mockStrategy.getStrategyName()).thenReturn("STANDARD");
        when(mockStrategy.categorizeRisk(anyInt(), anyDouble(), anyDouble())).thenReturn("FAIR");
        when(mockStrategy.calculateOverallScore(anyInt(), anyDouble(), anyDouble(), any()))
                .thenReturn(55);
        when(mockStrategy.determineRecommendation(anyString(), anyDouble(), anyDouble()))
                .thenReturn("APPROVE_WITH_CONDITIONS");

        calculator.process(exchange);

        List<String> factors = captureResponse().getRiskFactors();
        assertTrue(factors.contains("HIGH_CREDIT_UTILIZATION"),
                "Expected HIGH_CREDIT_UTILIZATION in risk factors");
    }

    @Test
    void testIdentifyRiskFactors_NoFactors() throws Exception {
        CreditRiskReqType req = request("FULL_TIME", 100000, 200000, null);
        CreditScoreDetail cd  = creditDetail(760, 5000, 50000, 0.10, 0, 2);  // all good
        stubExchange(req, cd, Boolean.FALSE);
        when(mockStrategy.getStrategyName()).thenReturn("STANDARD");
        when(mockStrategy.categorizeRisk(anyInt(), anyDouble(), anyDouble())).thenReturn("EXCELLENT");
        when(mockStrategy.calculateOverallScore(anyInt(), anyDouble(), anyDouble(), any()))
                .thenReturn(90);
        when(mockStrategy.determineRecommendation(anyString(), anyDouble(), anyDouble()))
                .thenReturn("APPROVE");

        calculator.process(exchange);

        List<String> factors = captureResponse().getRiskFactors();
        assertTrue(factors.isEmpty(), "Expected no risk factors for excellent profile");
    }
}
