package com.nexgen.sb.creditrisk.service;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import com.nexgen.esb.creditrisk.model.ResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Orchestration service that coordinates the credit risk assessment workflow.
 * Receives a fully-built {@link CreditRiskReqType}, delegates to the scoring
 * and bureau-integration pipeline, and returns a {@link CreditRiskResType}.
 *
 * <p>Corresponds to forward-engineering task FE-011.  The current implementation
 * is a stub that can be wired to the actual scoring strategies and bureau client
 * once those components are migrated to the Spring Boot module.</p>
 */
@Service
public class CreditRiskOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(CreditRiskOrchestrationService.class);

    /**
     * Process a credit risk assessment request.
     *
     * @param request      fully-populated request (including {@code RequestHeader})
     * @param sourceSystem originating channel, e.g. {@code "REST"} or {@code "SOAP"}
     * @return credit risk response
     */
    public CreditRiskResType processRequest(CreditRiskReqType request, String sourceSystem) {
        log.info("Processing credit risk request for applicantId={} via sourceSystem={}",
                request.getApplicantId(), sourceSystem);

        CreditRiskResType response = new CreditRiskResType();

        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setTransactionId(
                request.getRequestHeader() != null
                        ? request.getRequestHeader().getTransactionId()
                        : null);
        responseHeader.setTimestamp(Instant.now().toString());
        responseHeader.setStatusCode("SUCCESS");
        responseHeader.setStatusMessage("Credit risk assessment completed");
        response.setResponseHeader(responseHeader);

        response.setApplicantId(request.getApplicantId());

        return response;
    }
}
