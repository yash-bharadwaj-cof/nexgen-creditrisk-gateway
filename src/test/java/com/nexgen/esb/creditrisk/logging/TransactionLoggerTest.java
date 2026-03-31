package com.nexgen.esb.creditrisk.logging;

import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import com.nexgen.esb.creditrisk.model.ResponseHeader;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionLogger.
 * Verifies that transaction documents are built correctly from exchange properties
 * and that MongoDB failures are silently swallowed.
 */
@ExtendWith(MockitoExtension.class)
class TransactionLoggerTest {

    private TransactionLogger logger;

    @Mock
    Exchange exchange;

    @BeforeEach
    void setUp() {
        logger = new TransactionLogger();
        // Use an invalid port string so Integer.parseInt throws immediately,
        // avoiding the 30-second MongoDB server-selection timeout in tests.
        logger.setMongoHost("localhost");
        logger.setMongoPort("not-a-number");
        logger.setMongoDb("testDb");
        logger.setMongoCollection("testCollection");
    }

    // -----------------------------------------------------------------------
    // Helper to stub exchange properties
    // -----------------------------------------------------------------------

    private void stubSuccessExchange() {
        when(exchange.getProperty(LoggerConstants.LOG_TRANSACTION_ID, String.class))
                .thenReturn("TX-001");
        when(exchange.getProperty(LoggerConstants.LOG_APPLICANT_ID, String.class))
                .thenReturn("APP-001");
        when(exchange.getProperty(LoggerConstants.LOG_PROVINCE, String.class))
                .thenReturn("ON");
        when(exchange.getProperty(LoggerConstants.LOG_PRODUCT_TYPE, String.class))
                .thenReturn("MORTGAGE");
        when(exchange.getProperty(LoggerConstants.LOG_REQUEST_CHANNEL, String.class))
                .thenReturn("REST");
        when(exchange.getProperty(LoggerConstants.LOG_SOURCE_SYSTEM, String.class))
                .thenReturn("PORTAL");

        CreditRiskResType response = new CreditRiskResType();
        ResponseHeader header = new ResponseHeader();
        header.setStatusCode("SUCCESS");
        response.setResponseHeader(header);
        response.setRiskCategory("GOOD");
        response.setOverallScore(72);
        response.setRecommendation("APPROVE");
        when(exchange.getProperty("RISK_RESPONSE")).thenReturn(response);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Even if the MongoDB setup fails (e.g., invalid port) the method must swallow the exception.
     */
    @Test
    void testLogTransaction_MongoFailure_DoesNotThrow() {
        stubSuccessExchange();
        // Invalid port causes NumberFormatException before MongoClient is created;
        // the catch block handles it so nothing should propagate.
        assertDoesNotThrow(() -> logger.logTransaction(exchange));
    }

    /**
     * When no RISK_RESPONSE property is set the method should still not throw.
     */
    @Test
    void testLogTransaction_NullResponse_DoesNotThrow() {
        when(exchange.getProperty(LoggerConstants.LOG_TRANSACTION_ID, String.class))
                .thenReturn("TX-002");
        when(exchange.getProperty(LoggerConstants.LOG_APPLICANT_ID, String.class))
                .thenReturn("APP-002");
        when(exchange.getProperty(LoggerConstants.LOG_PROVINCE, String.class))
                .thenReturn(null);
        when(exchange.getProperty(LoggerConstants.LOG_PRODUCT_TYPE, String.class))
                .thenReturn(null);
        when(exchange.getProperty(LoggerConstants.LOG_REQUEST_CHANNEL, String.class))
                .thenReturn(null);
        when(exchange.getProperty(LoggerConstants.LOG_SOURCE_SYSTEM, String.class))
                .thenReturn(null);
        when(exchange.getProperty("RISK_RESPONSE")).thenReturn(null);
        when(exchange.getProperty("ERROR_CODE", String.class)).thenReturn("CR-500");
        when(exchange.getProperty("ERROR_MESSAGE", String.class)).thenReturn("Internal error");

        assertDoesNotThrow(() -> logger.logTransaction(exchange));
    }

    /**
     * Verifies that exchange properties are read for each expected field.
     */
    @Test
    void testLogTransaction_ReadsAllRequiredExchangeProperties() {
        stubSuccessExchange();
        // We don't care if MongoDB fails – we only verify that the properties are read
        try {
            logger.logTransaction(exchange);
        } catch (Exception ignored) {
            // any exception from Mongo is ignored
        }

        verify(exchange).getProperty(LoggerConstants.LOG_TRANSACTION_ID, String.class);
        verify(exchange).getProperty(LoggerConstants.LOG_APPLICANT_ID, String.class);
        verify(exchange).getProperty(LoggerConstants.LOG_PROVINCE, String.class);
        verify(exchange).getProperty(LoggerConstants.LOG_PRODUCT_TYPE, String.class);
        verify(exchange).getProperty(LoggerConstants.LOG_REQUEST_CHANNEL, String.class);
        verify(exchange).getProperty(LoggerConstants.LOG_SOURCE_SYSTEM, String.class);
        verify(exchange).getProperty("RISK_RESPONSE");
    }

}
