package com.nexgen.sb.creditrisk.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexgen.sb.creditrisk.generated.BureauInquiryRequest;
import com.nexgen.sb.creditrisk.generated.BureauInquiryResponse;
import com.nexgen.sb.creditrisk.model.CreditRiskReqType;
import com.nexgen.sb.creditrisk.model.CreditRiskResType;
import com.nexgen.sb.creditrisk.model.CreditScoreDetail;
import com.nexgen.sb.creditrisk.model.RequestHeader;
import com.nexgen.sb.creditrisk.model.ResponseHeader;
import com.nexgen.sb.creditrisk.scoring.StandardScoringStrategy;
import com.nexgen.sb.creditrisk.logging.TransactionLogService;
import com.nexgen.sb.creditrisk.exception.CreditRiskProcessingException;
import com.nexgen.sb.creditrisk.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
class CreditRiskOrchestrationServiceTest {

    @Mock
    private ValidationService validationService;

    @Mock
    private ScoringStrategyService scoringStrategyService;

    @Mock
    private BureauService bureauService;

    @Mock
    private RiskCalculationService riskCalculationService;

    @Mock
    private GatewayPreProcessService gatewayPreProcessService;

    @Mock
    private TransactionLogService transactionLogService;

    @InjectMocks
    private CreditRiskOrchestrationService orchestrationService;

    private CreditRiskReqType validRequest;
    private BureauInquiryRequest bureauRequest;
    private BureauInquiryResponse bureauResponse;
    private BureauResult bureauResult;
    private CreditRiskResType expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = new CreditRiskReqType();
        validRequest.setApplicantId("APP-001");
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setDateOfBirth("1985-06-15");
        validRequest.setSocialInsuranceNumber("123-456-789");
        validRequest.setProvince("ON");
        validRequest.setProductType("PERSONAL_LOAN");
        validRequest.setAnnualIncome(75000.0);
        validRequest.setRequestedAmount(20000.0);
        validRequest.setEmploymentStatus("FULL_TIME");

        RequestHeader header = new RequestHeader();
        header.setTransactionId("TXN-001");
        header.setSourceSystem("REST");
        validRequest.setRequestHeader(header);

        bureauRequest = new BureauInquiryRequest();
        bureauResponse = new BureauInquiryResponse();
        bureauResponse.setCreditScore(720);

        CreditScoreDetail creditDetail = new CreditScoreDetail();
        creditDetail.setBureauScore(720);
        bureauResult = new BureauResult(creditDetail, false);

        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setStatusCode("SUCCESS");
        responseHeader.setTransactionId("TXN-001");
        expectedResponse = new CreditRiskResType();
        expectedResponse.setResponseHeader(responseHeader);
        expectedResponse.setApplicantId("APP-001");
        expectedResponse.setRiskCategory("GOOD");
        expectedResponse.setOverallScore(75);
        expectedResponse.setRecommendation("APPROVE");
    }

    // -------------------------------------------------------------------------
    // REST channel: validate → strategy → bureau → risk → log
    // -------------------------------------------------------------------------

    @Test
    void processRequest_RestChannel_ReturnsRiskResponse() {
        when(scoringStrategyService.resolveStrategy(validRequest))
                .thenReturn(new StandardScoringStrategy());
        when(bureauService.buildRequest(validRequest)).thenReturn(bureauRequest);
        when(bureauService.callBureau(bureauRequest)).thenReturn(bureauResponse);
        when(bureauService.mapResponse(bureauResponse)).thenReturn(bureauResult);
        when(riskCalculationService.calculate(eq(validRequest), any(), any(), eq(false)))
                .thenReturn(expectedResponse);

        CreditRiskResType result = orchestrationService.processRequest(validRequest, "REST");

        assertNotNull(result);
        assertEquals("APP-001", result.getApplicantId());
        assertEquals("GOOD", result.getRiskCategory());

        verify(validationService).validate(validRequest);
        verify(gatewayPreProcessService, never()).preProcess(any());
        verify(transactionLogService).logAsync(validRequest, expectedResponse, "REST");
    }

    // -------------------------------------------------------------------------
    // SOAP channel: same flow as REST, no pre-processing
    // -------------------------------------------------------------------------

    @Test
    void processRequest_SoapChannel_DoesNotInvokeGatewayPreProcess() {
        when(scoringStrategyService.resolveStrategy(validRequest))
                .thenReturn(new StandardScoringStrategy());
        when(bureauService.buildRequest(validRequest)).thenReturn(bureauRequest);
        when(bureauService.callBureau(bureauRequest)).thenReturn(bureauResponse);
        when(bureauService.mapResponse(bureauResponse)).thenReturn(bureauResult);
        when(riskCalculationService.calculate(eq(validRequest), any(), any(), eq(false)))
                .thenReturn(expectedResponse);

        CreditRiskResType result = orchestrationService.processRequest(validRequest, "SOAP");

        assertNotNull(result);
        verify(gatewayPreProcessService, never()).preProcess(any());
        verify(transactionLogService).logAsync(validRequest, expectedResponse, "SOAP");
    }

    // -------------------------------------------------------------------------
    // GATEWAY channel: preProcess → validate → strategy → bureau → risk → log
    // -------------------------------------------------------------------------

    @Test
    void processRequest_GatewayChannel_InvokesPreProcessFirst() {
        when(gatewayPreProcessService.preProcess(validRequest)).thenReturn(validRequest);
        when(scoringStrategyService.resolveStrategy(validRequest))
                .thenReturn(new StandardScoringStrategy());
        when(bureauService.buildRequest(validRequest)).thenReturn(bureauRequest);
        when(bureauService.callBureau(bureauRequest)).thenReturn(bureauResponse);
        when(bureauService.mapResponse(bureauResponse)).thenReturn(bureauResult);
        when(riskCalculationService.calculate(eq(validRequest), any(), any(), eq(false)))
                .thenReturn(expectedResponse);

        CreditRiskResType result = orchestrationService.processRequest(validRequest, "GATEWAY");

        assertNotNull(result);
        verify(gatewayPreProcessService).preProcess(validRequest);

        // validation must happen AFTER pre-processing
        InOrder order = inOrder(gatewayPreProcessService, validationService);
        order.verify(gatewayPreProcessService).preProcess(validRequest);
        order.verify(validationService).validate(validRequest);

        verify(transactionLogService).logAsync(validRequest, expectedResponse, "GATEWAY");
    }

    // -------------------------------------------------------------------------
    // Validation failure: error logged asynchronously, exception re-thrown
    // -------------------------------------------------------------------------

    @Test
    void processRequest_ValidationFails_LogsErrorAndRethrows() {
        ValidationException validationEx =
                new ValidationException("CR-001", "Applicant ID field is blank");
        doThrow(validationEx).when(validationService).validate(any());

        assertThrows(ValidationException.class,
                () -> orchestrationService.processRequest(validRequest, "REST"));

        verify(transactionLogService).logErrorAsync(validRequest, "CR-001",
                "Applicant ID field is blank", "REST");
        verify(transactionLogService, never()).logAsync(any(), any(), any());
    }

    // -------------------------------------------------------------------------
    // Unexpected exception: wrapped in CreditRiskProcessingException
    // -------------------------------------------------------------------------

    @Test
    void processRequest_UnexpectedError_ThrowsCreditRiskProcessingException() {
        when(scoringStrategyService.resolveStrategy(any()))
                .thenThrow(new RuntimeException("Unexpected failure"));

        CreditRiskProcessingException ex = assertThrows(CreditRiskProcessingException.class,
                () -> orchestrationService.processRequest(validRequest, "REST"));

        assertEquals("Internal service error", ex.getMessage());
        assertNotNull(ex.getCause());
        verify(transactionLogService).logErrorAsync(eq(validRequest), eq("CR-500"),
                eq("Unexpected failure"), eq("REST"));
    }

    // -------------------------------------------------------------------------
    // Bureau error path: risk calc receives hasError=true
    // -------------------------------------------------------------------------

    @Test
    void processRequest_BureauError_CalculatesRiskWithErrorFlag() {
        BureauResult errorResult = new BureauResult(null, true);

        when(scoringStrategyService.resolveStrategy(validRequest))
                .thenReturn(new StandardScoringStrategy());
        when(bureauService.buildRequest(validRequest)).thenReturn(bureauRequest);
        when(bureauService.callBureau(bureauRequest)).thenReturn(bureauResponse);
        when(bureauService.mapResponse(bureauResponse)).thenReturn(errorResult);

        ResponseHeader indeterminateHeader = new ResponseHeader();
        indeterminateHeader.setStatusCode("W215E");
        CreditRiskResType indeterminateResponse = new CreditRiskResType();
        indeterminateResponse.setResponseHeader(indeterminateHeader);
        indeterminateResponse.setRiskCategory("INDETERMINATE");
        indeterminateResponse.setRecommendation("REFER_TO_UNDERWRITER");

        when(riskCalculationService.calculate(eq(validRequest), isNull(),
                any(), eq(true))).thenReturn(indeterminateResponse);

        CreditRiskResType result = orchestrationService.processRequest(validRequest, "REST");

        assertNotNull(result);
        assertEquals("INDETERMINATE", result.getRiskCategory());
        verify(riskCalculationService).calculate(eq(validRequest), isNull(), any(), eq(true));
    }
}
