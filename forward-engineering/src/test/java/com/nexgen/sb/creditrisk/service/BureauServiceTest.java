package com.nexgen.sb.creditrisk.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nexgen.sb.creditrisk.generated.BureauInquiryRequest;
import com.nexgen.sb.creditrisk.generated.BureauInquiryResponse;
import com.nexgen.sb.creditrisk.model.CreditRiskReqType;
import com.nexgen.sb.creditrisk.model.CreditScoreDetail;
import com.nexgen.sb.creditrisk.config.BureauProperties;

class BureauServiceTest {

    private BureauProperties properties;
    private BureauService service;

    @BeforeEach
    void setUp() {
        properties = new BureauProperties();
        properties.setRequestIdPrefix("test-prefix-");
        properties.setSubscriberCode("TEST-001");
        properties.setSubscriberName("Test Subscriber");
        properties.setStubEnabled(true);
        service = new BureauService(properties);
    }

    // -------------------------------------------------------------------------
    // buildRequest
    // -------------------------------------------------------------------------

    @Test
    void buildRequest_mapsAllFieldsFromRequest() {
        CreditRiskReqType req = new CreditRiskReqType();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setDateOfBirth("1990-06-15");
        req.setSocialInsuranceNumber("123-456-789");
        req.setProvince("ON");
        req.setPostalCode("M5V 3A8");
        req.setProductType("MORTGAGE");

        BureauInquiryRequest bureauReq = service.buildRequest(req);

        assertEquals("Jane", bureauReq.getSubject().getFirstName());
        assertEquals("Doe", bureauReq.getSubject().getLastName());
        assertEquals("1990-06-15", bureauReq.getSubject().getDateOfBirth());
        assertEquals("123-456-789", bureauReq.getSubject().getSocialInsuranceNumber());
        assertEquals("ON", bureauReq.getSubject().getProvince());
        assertEquals("M5V 3A8", bureauReq.getSubject().getPostalCode());
        assertEquals("MORTGAGE", bureauReq.getProductType());
        assertEquals("TEST-001", bureauReq.getSubscriber().getSubscriberCode());
        assertEquals("Test Subscriber", bureauReq.getSubscriber().getSubscriberName());
        assertTrue(bureauReq.getRequestId().startsWith("test-prefix-"),
                "requestId should start with configured prefix");
        assertNotNull(bureauReq.getTimestamp(), "timestamp must be set");
    }

    // -------------------------------------------------------------------------
    // callBureau (stubbed)
    // -------------------------------------------------------------------------

    @Test
    void callBureau_returnsStubResponse_whenStubEnabled() {
        BureauInquiryRequest req = new BureauInquiryRequest();
        req.setRequestId("req-1");

        BureauInquiryResponse response = service.callBureau(req);

        assertNotNull(response);
        assertEquals("req-1", response.getRequestId());
        assertTrue(response.getResponseId().startsWith("STUB-"));
        assertEquals(720, response.getCreditScore());
        assertEquals(0, response.getDelinquencyCount());
        assertEquals(2, response.getInquiryCount());
        assertEquals(5, response.getOpenTradelineCount());
        assertEquals(50000.0, response.getTotalCreditLimit());
        assertEquals(15000.0, response.getTotalBalance());
        assertNull(response.getErrorCode());
    }

    @Test
    void callBureau_throwsUnsupportedOperation_whenStubDisabled() {
        properties.setStubEnabled(false);
        BureauInquiryRequest req = new BureauInquiryRequest();
        req.setRequestId("req-2");

        assertThrows(UnsupportedOperationException.class, () -> service.callBureau(req));
    }

    // -------------------------------------------------------------------------
    // mapResponse — score range thresholds
    // -------------------------------------------------------------------------

    @Test
    void mapResponse_scoreRange_exceptional() {
        BureauResult result = service.mapResponse(responseWithScore(800));
        assertEquals("EXCEPTIONAL", result.creditDetail().getBureauScoreRange());
    }

    @Test
    void mapResponse_scoreRange_veryGood() {
        BureauResult result = service.mapResponse(responseWithScore(740));
        assertEquals("VERY_GOOD", result.creditDetail().getBureauScoreRange());
    }

    @Test
    void mapResponse_scoreRange_good() {
        BureauResult result = service.mapResponse(responseWithScore(670));
        assertEquals("GOOD", result.creditDetail().getBureauScoreRange());
    }

    @Test
    void mapResponse_scoreRange_fair() {
        BureauResult result = service.mapResponse(responseWithScore(580));
        assertEquals("FAIR", result.creditDetail().getBureauScoreRange());
    }

    @Test
    void mapResponse_scoreRange_poor() {
        BureauResult result = service.mapResponse(responseWithScore(579));
        assertEquals("POOR", result.creditDetail().getBureauScoreRange());
    }

    // -------------------------------------------------------------------------
    // mapResponse — field mapping
    // -------------------------------------------------------------------------

    @Test
    void mapResponse_mapsAllFieldsCorrectly() {
        BureauInquiryResponse resp = new BureauInquiryResponse();
        resp.setCreditScore(720);
        resp.setDelinquencyCount(1);
        resp.setInquiryCount(3);
        resp.setOpenTradelineCount(7);
        resp.setTotalCreditLimit(40000.0);
        resp.setTotalBalance(10000.0);

        BureauResult result = service.mapResponse(resp);

        assertFalse(result.hasError());
        assertNull(result.errorCode());
        CreditScoreDetail detail = result.creditDetail();
        assertNotNull(detail);
        assertEquals(720, detail.getBureauScore());
        assertEquals("GOOD", detail.getBureauScoreRange());
        assertEquals(1, detail.getDelinquencyCount());
        assertEquals(3, detail.getInquiryCount());
        assertEquals(7, detail.getOpenAccountCount());
        assertEquals(40000.0, detail.getTotalCreditLimit());
        assertEquals(10000.0, detail.getTotalBalance());
        assertEquals(0.25, detail.getUtilizationRate(), 0.0001);
    }

    @Test
    void mapResponse_utilizationRate_zeroWhenNoCreditLimit() {
        BureauInquiryResponse resp = new BureauInquiryResponse();
        resp.setCreditScore(700);
        resp.setTotalCreditLimit(0.0);
        resp.setTotalBalance(0.0);

        BureauResult result = service.mapResponse(resp);

        assertEquals(0.0, result.creditDetail().getUtilizationRate());
    }

    // -------------------------------------------------------------------------
    // mapResponse — error handling
    // -------------------------------------------------------------------------

    @Test
    void mapResponse_nullResponse_returnsCR301Error() {
        BureauResult result = service.mapResponse(null);

        assertTrue(result.hasError());
        assertEquals("CR-301", result.errorCode());
        assertNull(result.creditDetail());
    }

    @Test
    void mapResponse_errorCodeInResponse_returnsCR302Error() {
        BureauInquiryResponse resp = new BureauInquiryResponse();
        resp.setErrorCode("BUREAU-500");
        resp.setErrorMessage("Service unavailable");

        BureauResult result = service.mapResponse(resp);

        assertTrue(result.hasError());
        assertEquals("CR-302", result.errorCode());
        assertEquals("Service unavailable", result.errorMessage());
        assertNull(result.creditDetail());
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private BureauInquiryResponse responseWithScore(int score) {
        BureauInquiryResponse resp = new BureauInquiryResponse();
        resp.setCreditScore(score);
        resp.setTotalCreditLimit(10000.0);
        resp.setTotalBalance(1000.0);
        return resp;
    }
}
