package com.nexgen.esb.creditrisk.processor;

import com.nexgen.esb.creditrisk.generated.BureauInquiryResponse;
import com.nexgen.esb.creditrisk.model.CreditScoreDetail;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BureauResponseMapper.
 * Covers successful mapping, null-response errors, error-code responses, and score-range mapping.
 */
@ExtendWith(MockitoExtension.class)
class BureauResponseMapperTest {

    private BureauResponseMapper mapper;

    @Mock
    Exchange exchange;

    @Mock
    Message message;

    @BeforeEach
    void setUp() {
        mapper = new BureauResponseMapper();
        when(exchange.getIn()).thenReturn(message);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private BureauInquiryResponse successResponse(int score) {
        BureauInquiryResponse resp = new BureauInquiryResponse();
        resp.setCreditScore(score);
        resp.setDelinquencyCount(0);
        resp.setInquiryCount(2);
        resp.setOpenTradelineCount(5);
        resp.setTotalCreditLimit(20000.0);
        resp.setTotalBalance(4000.0);
        return resp;
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    void testMapResponse_Success_MapsCreditDetail() throws Exception {
        when(message.getBody(BureauInquiryResponse.class)).thenReturn(successResponse(720));

        mapper.process(exchange);

        ArgumentCaptor<CreditScoreDetail> detailCaptor =
                ArgumentCaptor.forClass(CreditScoreDetail.class);
        verify(exchange).setProperty(eq("CREDIT_SCORE_DETAIL"), detailCaptor.capture());
        verify(exchange).setProperty("BUREAU_ERROR", false);

        CreditScoreDetail detail = detailCaptor.getValue();
        assertEquals(720, detail.getBureauScore().intValue());
        assertEquals(0, detail.getDelinquencyCount().intValue());
        assertEquals(2, detail.getInquiryCount().intValue());
        assertEquals(5, detail.getOpenAccountCount().intValue());
        assertEquals(20000.0, detail.getTotalCreditLimit(), 0.001);
        assertEquals(4000.0, detail.getTotalBalance(), 0.001);
        // utilization = 4000 / 20000 = 0.20
        assertEquals(0.20, detail.getUtilizationRate(), 0.001);
    }

    @Test
    void testMapResponse_NullResponse_ReturnsBureauError() throws Exception {
        when(message.getBody(BureauInquiryResponse.class)).thenReturn(null);

        mapper.process(exchange);

        verify(exchange).setProperty("BUREAU_ERROR", true);
        verify(exchange).setProperty("BUREAU_ERROR_CODE", "CR-301");
    }

    @Test
    void testMapResponse_ErrorCode_ReturnsBureauError() throws Exception {
        BureauInquiryResponse errResp = new BureauInquiryResponse();
        errResp.setErrorCode("E999");
        errResp.setErrorMessage("Bureau unavailable");
        when(message.getBody(BureauInquiryResponse.class)).thenReturn(errResp);

        mapper.process(exchange);

        verify(exchange).setProperty("BUREAU_ERROR", true);
        verify(exchange).setProperty("BUREAU_ERROR_CODE", "CR-302");
        verify(exchange).setProperty("BUREAU_ERROR_MSG", "Bureau unavailable");
    }

    @Test
    void testMapResponse_Score800Plus_Exceptional() throws Exception {
        when(message.getBody(BureauInquiryResponse.class)).thenReturn(successResponse(820));

        mapper.process(exchange);

        ArgumentCaptor<CreditScoreDetail> captor =
                ArgumentCaptor.forClass(CreditScoreDetail.class);
        verify(exchange).setProperty(eq("CREDIT_SCORE_DETAIL"), captor.capture());
        assertEquals("EXCEPTIONAL", captor.getValue().getBureauScoreRange());
    }

    @Test
    void testMapResponse_Score740Plus_VeryGood() throws Exception {
        when(message.getBody(BureauInquiryResponse.class)).thenReturn(successResponse(760));

        mapper.process(exchange);

        ArgumentCaptor<CreditScoreDetail> captor =
                ArgumentCaptor.forClass(CreditScoreDetail.class);
        verify(exchange).setProperty(eq("CREDIT_SCORE_DETAIL"), captor.capture());
        assertEquals("VERY_GOOD", captor.getValue().getBureauScoreRange());
    }

    @Test
    void testMapResponse_Score670Plus_Good() throws Exception {
        when(message.getBody(BureauInquiryResponse.class)).thenReturn(successResponse(690));

        mapper.process(exchange);

        ArgumentCaptor<CreditScoreDetail> captor =
                ArgumentCaptor.forClass(CreditScoreDetail.class);
        verify(exchange).setProperty(eq("CREDIT_SCORE_DETAIL"), captor.capture());
        assertEquals("GOOD", captor.getValue().getBureauScoreRange());
    }

    @Test
    void testMapResponse_Score580Plus_Fair() throws Exception {
        when(message.getBody(BureauInquiryResponse.class)).thenReturn(successResponse(610));

        mapper.process(exchange);

        ArgumentCaptor<CreditScoreDetail> captor =
                ArgumentCaptor.forClass(CreditScoreDetail.class);
        verify(exchange).setProperty(eq("CREDIT_SCORE_DETAIL"), captor.capture());
        assertEquals("FAIR", captor.getValue().getBureauScoreRange());
    }

    @Test
    void testMapResponse_ScoreBelow580_Poor() throws Exception {
        when(message.getBody(BureauInquiryResponse.class)).thenReturn(successResponse(550));

        mapper.process(exchange);

        ArgumentCaptor<CreditScoreDetail> captor =
                ArgumentCaptor.forClass(CreditScoreDetail.class);
        verify(exchange).setProperty(eq("CREDIT_SCORE_DETAIL"), captor.capture());
        assertEquals("POOR", captor.getValue().getBureauScoreRange());
    }

    @Test
    void testMapResponse_ZeroCreditLimit_UtilizationIsZero() throws Exception {
        BureauInquiryResponse resp = successResponse(700);
        resp.setTotalCreditLimit(0.0);
        resp.setTotalBalance(0.0);
        when(message.getBody(BureauInquiryResponse.class)).thenReturn(resp);

        mapper.process(exchange);

        ArgumentCaptor<CreditScoreDetail> captor =
                ArgumentCaptor.forClass(CreditScoreDetail.class);
        verify(exchange).setProperty(eq("CREDIT_SCORE_DETAIL"), captor.capture());
        assertEquals(0.0, captor.getValue().getUtilizationRate(), 0.001);
    }
}
