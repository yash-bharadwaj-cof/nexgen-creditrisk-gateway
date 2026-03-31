package com.nexgen.esb.creditrisk.processor;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.scoring.AggressiveScoringStrategy;
import com.nexgen.esb.creditrisk.scoring.ConservativeScoringStrategy;
import com.nexgen.esb.creditrisk.scoring.ScoringStrategy;
import com.nexgen.esb.creditrisk.scoring.StandardScoringStrategy;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScoringStrategyProcessor.
 * Verifies that the correct scoring strategy is selected based on product type.
 */
@ExtendWith(MockitoExtension.class)
class ScoringStrategyProcessorTest {

    private ScoringStrategyProcessor processor;

    @Mock
    Exchange exchange;

    @BeforeEach
    void setUp() {
        processor = new ScoringStrategyProcessor();
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private CreditRiskReqType requestWithProduct(String productType) {
        CreditRiskReqType req = new CreditRiskReqType();
        req.setApplicantId("APP-001");
        req.setProductType(productType);
        req.setRequestChannel("REST");
        return req;
    }

    private ScoringStrategy captureStrategy() {
        ArgumentCaptor<ScoringStrategy> captor = ArgumentCaptor.forClass(ScoringStrategy.class);
        verify(exchange).setProperty(eq("SCORING_STRATEGY"), captor.capture());
        return captor.getValue();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    void testMortgage_ReturnsConservative() throws Exception {
        when(exchange.getProperty("VALIDATED_REQUEST")).thenReturn(requestWithProduct("MORTGAGE"));
        processor.process(exchange);
        assertInstanceOf(ConservativeScoringStrategy.class, captureStrategy());
    }

    @Test
    void testAutoLoan_ReturnsConservative() throws Exception {
        when(exchange.getProperty("VALIDATED_REQUEST")).thenReturn(requestWithProduct("AUTO_LOAN"));
        processor.process(exchange);
        assertInstanceOf(ConservativeScoringStrategy.class, captureStrategy());
    }

    @Test
    void testCreditCard_ReturnsAggressive() throws Exception {
        when(exchange.getProperty("VALIDATED_REQUEST")).thenReturn(requestWithProduct("CREDIT_CARD"));
        processor.process(exchange);
        assertInstanceOf(AggressiveScoringStrategy.class, captureStrategy());
    }

    @Test
    void testLineOfCredit_ReturnsAggressive() throws Exception {
        when(exchange.getProperty("VALIDATED_REQUEST")).thenReturn(requestWithProduct("LINE_OF_CREDIT"));
        processor.process(exchange);
        assertInstanceOf(AggressiveScoringStrategy.class, captureStrategy());
    }

    @Test
    void testDefault_ReturnsStandard() throws Exception {
        when(exchange.getProperty("VALIDATED_REQUEST")).thenReturn(requestWithProduct("PERSONAL_LOAN"));
        processor.process(exchange);
        assertInstanceOf(StandardScoringStrategy.class, captureStrategy());
    }

    @Test
    void testNullProductType_ReturnsStandard() throws Exception {
        when(exchange.getProperty("VALIDATED_REQUEST")).thenReturn(requestWithProduct(null));
        processor.process(exchange);
        assertInstanceOf(StandardScoringStrategy.class, captureStrategy());
    }
}
