package com.nexgen.sb.creditrisk.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nexgen.esb.creditrisk.generated.BureauInquiryRequest;
import com.nexgen.esb.creditrisk.generated.BureauInquiryResponse;
import com.nexgen.esb.creditrisk.generated.BureauSubscriber;
import com.nexgen.esb.creditrisk.generated.BureauSubject;
import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditScoreDetail;

/**
 * Handles all interactions with the external credit bureau.
 * Migrated from legacy {@code BureauRequestBuilder} and {@code BureauResponseMapper}
 * Camel Processors.
 *
 * <p>The {@link #callBureau(BureauInquiryRequest)} method is currently stubbed;
 * in a production deployment it would invoke the external bureau SOAP endpoint.</p>
 */
@Service
public class BureauService {

    private static final Logger LOG = LoggerFactory.getLogger(BureauService.class);
    private static final SimpleDateFormat TIMESTAMP_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Value("${bureau.request-id-prefix:nexgen-}")
    private String requestIdPrefix;

    @Value("${bureau.subscriber-code:NEXGEN-001}")
    private String subscriberCode;

    @Value("${bureau.subscriber-name:NexGen Financial}")
    private String subscriberName;

    /**
     * Builds the outbound bureau inquiry request from the validated credit risk request.
     */
    public BureauInquiryRequest buildRequest(CreditRiskReqType request) {
        BureauInquiryRequest bureauRequest = new BureauInquiryRequest();

        BureauSubscriber subscriber = new BureauSubscriber();
        subscriber.setSubscriberCode(subscriberCode);
        subscriber.setSubscriberName(subscriberName);
        bureauRequest.setSubscriber(subscriber);

        BureauSubject subject = new BureauSubject();
        subject.setFirstName(request.getFirstName());
        subject.setLastName(request.getLastName());
        subject.setDateOfBirth(request.getDateOfBirth());
        subject.setSocialInsuranceNumber(request.getSocialInsuranceNumber());
        subject.setProvince(request.getProvince());
        subject.setPostalCode(request.getPostalCode());
        bureauRequest.setSubject(subject);

        String requestId = requestIdPrefix + UUID.randomUUID().toString();
        bureauRequest.setRequestId(requestId);
        bureauRequest.setTimestamp(TIMESTAMP_FORMAT.format(new Date()));
        bureauRequest.setProductType(request.getProductType());

        LOG.info("Bureau request built with requestId: {}", requestId);
        return bureauRequest;
    }

    /**
     * Calls the external credit bureau service.
     *
     * <p><strong>Stub implementation</strong>: returns a synthetic response.
     * Replace this method body with the real bureau SOAP client call in production.</p>
     */
    public BureauInquiryResponse callBureau(BureauInquiryRequest request) {
        LOG.info("Bureau call (stubbed) for requestId: {}", request.getRequestId());
        BureauInquiryResponse response = new BureauInquiryResponse();
        response.setRequestId(request.getRequestId());
        response.setResponseId(UUID.randomUUID().toString());
        response.setCreditScore(720);
        response.setDelinquencyCount(0);
        response.setInquiryCount(2);
        response.setOpenTradelineCount(5);
        response.setTotalCreditLimit(50000.0);
        response.setTotalBalance(12500.0);
        return response;
    }

    /**
     * Maps the bureau response to a {@link BureauResult} containing the
     * {@link CreditScoreDetail} and an error flag.
     */
    public BureauResult mapResponse(BureauInquiryResponse bureauResponse) {
        if (bureauResponse == null) {
            LOG.warn("Bureau response is null — marking as error");
            return new BureauResult(null, true);
        }

        if (bureauResponse.getErrorCode() != null) {
            LOG.warn("Bureau returned error: {} - {}",
                    bureauResponse.getErrorCode(), bureauResponse.getErrorMessage());
            return new BureauResult(null, true);
        }

        CreditScoreDetail creditDetail = new CreditScoreDetail();
        creditDetail.setBureauScore(bureauResponse.getCreditScore());
        creditDetail.setBureauScoreRange(mapScoreRange(bureauResponse.getCreditScore()));
        creditDetail.setDelinquencyCount(bureauResponse.getDelinquencyCount());
        creditDetail.setInquiryCount(bureauResponse.getInquiryCount());
        creditDetail.setOpenAccountCount(bureauResponse.getOpenTradelineCount());
        creditDetail.setTotalCreditLimit(bureauResponse.getTotalCreditLimit());
        creditDetail.setTotalBalance(bureauResponse.getTotalBalance());

        if (bureauResponse.getTotalCreditLimit() != null && bureauResponse.getTotalCreditLimit() > 0) {
            creditDetail.setUtilizationRate(
                    bureauResponse.getTotalBalance() / bureauResponse.getTotalCreditLimit());
        } else {
            creditDetail.setUtilizationRate(0.0);
        }

        LOG.info("Bureau response mapped successfully. Bureau score: {}", bureauResponse.getCreditScore());
        return new BureauResult(creditDetail, false);
    }

    private String mapScoreRange(Integer score) {
        if (score == null) return "UNKNOWN";
        if (score >= 800) return "EXCEPTIONAL";
        if (score >= 740) return "VERY_GOOD";
        if (score >= 670) return "GOOD";
        if (score >= 580) return "FAIR";
        return "POOR";
    }
}
