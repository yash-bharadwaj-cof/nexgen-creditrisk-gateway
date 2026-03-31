package com.nexgen.sb.creditrisk.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import com.nexgen.esb.creditrisk.model.ResponseHeader;
import com.nexgen.esb.creditrisk.service.CreditRiskServiceException;

/**
 * Orchestration service that processes credit risk assessment requests
 * received from SOAP endpoints.
 */
@Service
public class CreditRiskOrchestrationService {

    private static final Logger LOG = LoggerFactory.getLogger(CreditRiskOrchestrationService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public CreditRiskResType processRequest(CreditRiskReqType request, String channel)
            throws CreditRiskServiceException {
        LOG.info("Processing credit risk request for applicant: {} via channel: {}",
                request.getApplicantId(), channel);

        if (request.getApplicantId() == null || request.getApplicantId().isBlank()) {
            CreditRiskServiceException.DetailException detail = new CreditRiskServiceException.DetailException();
            detail.setErrorCode("INVALID_REQUEST");
            detail.setErrorMessage("applicantId is required");
            throw new CreditRiskServiceException("Invalid request", detail);
        }

        CreditRiskResType response = new CreditRiskResType();

        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setTransactionId(request.getRequestHeader() != null
                ? request.getRequestHeader().getTransactionId()
                : UUID.randomUUID().toString());
        responseHeader.setTimestamp(LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        responseHeader.setStatusCode("SUCCESS");
        responseHeader.setStatusMessage("Credit risk assessment completed");

        response.setResponseHeader(responseHeader);
        response.setApplicantId(request.getApplicantId());
        response.setScoringModelVersion("1.0");

        return response;
    }
}
