package com.nexgen.sb.creditrisk.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.RequestHeader;

/**
 * Pre-processes requests arriving via the Gateway SOAP endpoint.
 * Enriches the request with gateway-specific metadata before
 * forwarding to the orchestration service.
 */
@Service
public class GatewayPreProcessService {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayPreProcessService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public CreditRiskReqType preProcess(CreditRiskReqType request) {
        if (request == null) {
            return null;
        }

        if (request.getRequestHeader() == null) {
            RequestHeader header = new RequestHeader();
            header.setSourceSystem("GATEWAY");
            header.setTimestamp(LocalDateTime.now().format(TIMESTAMP_FORMATTER));
            header.setTransactionId(UUID.randomUUID().toString());
            request.setRequestHeader(header);
        }

        if (request.getRequestChannel() == null) {
            request.setRequestChannel("API");
        }

        LOG.info("Gateway request pre-processed for applicant: {}", request.getApplicantId());
        return request;
    }
}
