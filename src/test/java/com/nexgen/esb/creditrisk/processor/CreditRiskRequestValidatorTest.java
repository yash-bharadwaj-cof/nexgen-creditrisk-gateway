package com.nexgen.esb.creditrisk.processor;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.RequestHeader;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreditRiskRequestValidator.
 * Covers all validation error codes CR-000 through CR-007 and happy-path cases.
 */
@ExtendWith(MockitoExtension.class)
class CreditRiskRequestValidatorTest {

    private CreditRiskRequestValidator validator;

    @Mock
    Exchange exchange;

    @Mock
    Message message;

    @BeforeEach
    void setUp() {
        validator = new CreditRiskRequestValidator();
        when(exchange.getIn()).thenReturn(message);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private CreditRiskReqType validRequest() {
        CreditRiskReqType req = new CreditRiskReqType();
        req.setApplicantId("APP-001");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDateOfBirth("1985-06-15");
        req.setSocialInsuranceNumber("123-456-789");
        req.setProvince("ON");
        req.setPostalCode("M5V 1J1");
        RequestHeader header = new RequestHeader();
        header.setTransactionId("TX-001");
        header.setSourceSystem("REST");
        req.setRequestHeader(header);
        return req;
    }

    // -----------------------------------------------------------------------
    // Happy-path tests
    // -----------------------------------------------------------------------

    @Test
    void testValidRequest() throws Exception {
        when(message.getBody(CreditRiskReqType.class)).thenReturn(validRequest());
        assertDoesNotThrow(() -> validator.process(exchange));
        verify(exchange).setProperty(eq("VALIDATED_REQUEST"), any(CreditRiskReqType.class));
    }

    @Test
    void testValidSinWithoutDashes() throws Exception {
        CreditRiskReqType req = validRequest();
        req.setSocialInsuranceNumber("123456789");   // no dashes – still valid
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        assertDoesNotThrow(() -> validator.process(exchange));
    }

    @Test
    void testValidPostalCodeWithSpace() throws Exception {
        CreditRiskReqType req = validRequest();
        req.setPostalCode("K1A 0B1");
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        assertDoesNotThrow(() -> validator.process(exchange));
    }

    // -----------------------------------------------------------------------
    // Error-code tests
    // -----------------------------------------------------------------------

    @Test
    void testNullRequest_throwsCR000() {
        when(message.getBody(CreditRiskReqType.class)).thenReturn(null);
        CreditRiskRequestValidator.ValidationException ex = assertThrows(
                CreditRiskRequestValidator.ValidationException.class,
                () -> validator.process(exchange));
        assertEquals("CR-000", ex.getErrorCode());
    }

    @Test
    void testBlankApplicantId_throwsCR001() {
        CreditRiskReqType req = validRequest();
        req.setApplicantId("   ");
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        CreditRiskRequestValidator.ValidationException ex = assertThrows(
                CreditRiskRequestValidator.ValidationException.class,
                () -> validator.process(exchange));
        assertEquals("CR-001", ex.getErrorCode());
    }

    @Test
    void testBlankFirstName_throwsCR002() {
        CreditRiskReqType req = validRequest();
        req.setFirstName("");
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        CreditRiskRequestValidator.ValidationException ex = assertThrows(
                CreditRiskRequestValidator.ValidationException.class,
                () -> validator.process(exchange));
        assertEquals("CR-002", ex.getErrorCode());
    }

    @Test
    void testBlankLastName_throwsCR003() {
        CreditRiskReqType req = validRequest();
        req.setLastName(null);
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        CreditRiskRequestValidator.ValidationException ex = assertThrows(
                CreditRiskRequestValidator.ValidationException.class,
                () -> validator.process(exchange));
        assertEquals("CR-003", ex.getErrorCode());
    }

    @Test
    void testBlankDob_throwsCR004() {
        CreditRiskReqType req = validRequest();
        req.setDateOfBirth("");
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        CreditRiskRequestValidator.ValidationException ex = assertThrows(
                CreditRiskRequestValidator.ValidationException.class,
                () -> validator.process(exchange));
        assertEquals("CR-004", ex.getErrorCode());
    }

    @Test
    void testInvalidDobFormat_throwsCR004() {
        CreditRiskReqType req = validRequest();
        req.setDateOfBirth("15-06-1985");   // wrong format
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        CreditRiskRequestValidator.ValidationException ex = assertThrows(
                CreditRiskRequestValidator.ValidationException.class,
                () -> validator.process(exchange));
        assertEquals("CR-004", ex.getErrorCode());
    }

    @Test
    void testBlankSin_throwsCR005() {
        CreditRiskReqType req = validRequest();
        req.setSocialInsuranceNumber("   ");
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        CreditRiskRequestValidator.ValidationException ex = assertThrows(
                CreditRiskRequestValidator.ValidationException.class,
                () -> validator.process(exchange));
        assertEquals("CR-005", ex.getErrorCode());
    }

    @Test
    void testInvalidSinFormat_throwsCR005() {
        CreditRiskReqType req = validRequest();
        req.setSocialInsuranceNumber("12-34-5678");   // wrong format
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        CreditRiskRequestValidator.ValidationException ex = assertThrows(
                CreditRiskRequestValidator.ValidationException.class,
                () -> validator.process(exchange));
        assertEquals("CR-005", ex.getErrorCode());
    }

    @Test
    void testInvalidProvince_throwsCR006() {
        CreditRiskReqType req = validRequest();
        req.setProvince("ZZ");
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        CreditRiskRequestValidator.ValidationException ex = assertThrows(
                CreditRiskRequestValidator.ValidationException.class,
                () -> validator.process(exchange));
        assertEquals("CR-006", ex.getErrorCode());
    }

    @Test
    void testInvalidPostalCode_throwsCR007() {
        CreditRiskReqType req = validRequest();
        req.setPostalCode("12345");   // US format – invalid
        when(message.getBody(CreditRiskReqType.class)).thenReturn(req);
        CreditRiskRequestValidator.ValidationException ex = assertThrows(
                CreditRiskRequestValidator.ValidationException.class,
                () -> validator.process(exchange));
        assertEquals("CR-007", ex.getErrorCode());
    }
}
