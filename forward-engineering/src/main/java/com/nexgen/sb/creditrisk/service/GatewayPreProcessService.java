package com.nexgen.sb.creditrisk.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nexgen.sb.creditrisk.model.CreditRiskReqType;
import com.nexgen.sb.creditrisk.model.RequestHeader;

/**
 * Pre-processes requests arriving via the GATEWAY channel.
 * Migrated from legacy {@code GatewayRequestPreProcessor} Camel Processor.
 *
 * <p>If the request header is absent, a new one is created with source system
 * {@code "GATEWAY"}, current timestamp, and a generated transaction ID.
 * If the request channel is not set, it defaults to {@code "API"}.</p>
 */
@Service
public class GatewayPreProcessService {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayPreProcessService.class);
    private static final SimpleDateFormat TIMESTAMP_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Ensures the request has a populated header and channel before processing continues.
     *
     * @param request the incoming gateway request (mutated in place and returned)
     * @return the same request object, with header/channel populated if they were absent
     */
    public CreditRiskReqType preProcess(CreditRiskReqType request) {
        if (request == null) {
            return null;
        }

        if (request.getRequestHeader() == null) {
            RequestHeader header = new RequestHeader();
            header.setSourceSystem("GATEWAY");
            header.setTimestamp(TIMESTAMP_FORMAT.format(new Date()));
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
