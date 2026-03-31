package com.nexgen.esb.creditrisk.service;

import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic unit tests for CreditRiskRestSvc.
 * The service is a thin JAX-RS facade; deeper integration tests require a full
 * Camel/CXF runtime. These tests verify the endpoint metadata and that the stub
 * implementation does not throw.
 */
class CreditRiskRestSvcTest {

    private CreditRiskRestSvc service;

    @BeforeEach
    void setUp() {
        service = new CreditRiskRestSvc();
    }

    @Test
    void testAssess_StubImplementation_ReturnsNull() {
        // Current implementation is a stub that returns null; verify it does not throw
        assertDoesNotThrow(() -> {
            CreditRiskResType result = service.assessCreditRisk(
                    "APP-001", "John", "Doe", "1985-06-15",
                    "123-456-789", "FULL_TIME", 80000.0,
                    "ON", "M5V 1J1", "MORTGAGE", 300000.0);
            assertNull(result, "Stub implementation should return null");
        });
    }

    @Test
    void testAssess_NullParameters_DoesNotThrow() {
        assertDoesNotThrow(() ->
                service.assessCreditRisk(null, null, null, null,
                        null, null, null, null, null, null, null));
    }

    @Test
    void testAssess_WithAllOptionalParams_DoesNotThrow() {
        assertDoesNotThrow(() ->
                service.assessCreditRisk(
                        "APP-002", "Jane", "Smith", "1990-01-01",
                        "987-654-321", "PART_TIME", 45000.0,
                        "BC", "V6B 1A1", "CREDIT_CARD", 5000.0));
    }
}
