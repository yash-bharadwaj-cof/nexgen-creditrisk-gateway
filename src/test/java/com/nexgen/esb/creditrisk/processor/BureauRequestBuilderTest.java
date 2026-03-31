package com.nexgen.esb.creditrisk.processor;

import com.nexgen.esb.creditrisk.generated.BureauInquiryRequest;
import com.nexgen.esb.creditrisk.generated.BureauSubject;
import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.RequestHeader;
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
 * Unit tests for BureauRequestBuilder.
 * Verifies that all applicant fields are correctly mapped to the bureau request.
 */
@ExtendWith(MockitoExtension.class)
class BureauRequestBuilderTest {

    private BureauRequestBuilder builder;

    @Mock
    Exchange exchange;

    @Mock
    Message outMessage;

    @BeforeEach
    void setUp() {
        builder = new BureauRequestBuilder();
        builder.setRequestIdPrefix("TEST-");
        builder.setSubscriberCode("SUB-001");
        builder.setSubscriberName("Test Subscriber");
        when(exchange.getOut()).thenReturn(outMessage);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private CreditRiskReqType buildRequest() {
        CreditRiskReqType req = new CreditRiskReqType();
        req.setApplicantId("APP-001");
        req.setFirstName("Jane");
        req.setLastName("Smith");
        req.setDateOfBirth("1990-03-22");
        req.setSocialInsuranceNumber("987-654-321");
        req.setProvince("BC");
        req.setPostalCode("V6B 1A1");
        req.setProductType("MORTGAGE");
        RequestHeader header = new RequestHeader();
        header.setTransactionId("TX-ABC");
        req.setRequestHeader(header);
        return req;
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    void testBuildRequest_MapsAllFields() throws Exception {
        when(exchange.getProperty("VALIDATED_REQUEST")).thenReturn(buildRequest());

        builder.process(exchange);

        ArgumentCaptor<BureauInquiryRequest> captor =
                ArgumentCaptor.forClass(BureauInquiryRequest.class);
        verify(outMessage).setBody(captor.capture());
        BureauInquiryRequest bureauReq = captor.getValue();

        // Subject fields are mapped from applicant
        BureauSubject subject = bureauReq.getSubject();
        assertNotNull(subject);
        assertEquals("Jane", subject.getFirstName());
        assertEquals("Smith", subject.getLastName());
        assertEquals("1990-03-22", subject.getDateOfBirth());
        assertEquals("987-654-321", subject.getSocialInsuranceNumber());
        assertEquals("BC", subject.getProvince());
        assertEquals("V6B 1A1", subject.getPostalCode());

        // Subscriber code is set
        assertNotNull(bureauReq.getSubscriber());
        assertEquals("SUB-001", bureauReq.getSubscriber().getSubscriberCode());
        assertEquals("Test Subscriber", bureauReq.getSubscriber().getSubscriberName());

        // Product type is forwarded
        assertEquals("MORTGAGE", bureauReq.getProductType());

        // Request ID is prefixed
        assertTrue(bureauReq.getRequestId().startsWith("TEST-"));

        // Timestamp is set
        assertNotNull(bureauReq.getTimestamp());
    }

    @Test
    void testBuildRequest_SetsOperationHeaders() throws Exception {
        when(exchange.getProperty("VALIDATED_REQUEST")).thenReturn(buildRequest());

        builder.process(exchange);

        verify(outMessage).setHeader("operationName", "inquire");
        verify(outMessage).setHeader("operationNamespace",
                "http://ws.esb.nexgen.com/bureau/v1");
    }

    @Test
    void testBuildRequest_StoresBureauRequestId() throws Exception {
        when(exchange.getProperty("VALIDATED_REQUEST")).thenReturn(buildRequest());

        builder.process(exchange);

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        verify(exchange).setProperty(eq("BUREAU_REQUEST_ID"), idCaptor.capture());
        assertTrue(idCaptor.getValue().startsWith("TEST-"));
    }
}
