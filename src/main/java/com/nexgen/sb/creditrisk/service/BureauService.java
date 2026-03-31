package com.nexgen.sb.creditrisk.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nexgen.esb.creditrisk.generated.BureauInquiryRequest;
import com.nexgen.esb.creditrisk.generated.BureauInquiryResponse;
import com.nexgen.esb.creditrisk.generated.BureauSubscriber;
import com.nexgen.esb.creditrisk.generated.BureauSubject;
import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditScoreDetail;
import com.nexgen.sb.creditrisk.config.BureauProperties;

/**
 * Spring service that consolidates the legacy {@code BureauRequestBuilder},
 * bureau CXF SOAP call, and {@code BureauResponseMapper} into a single bean.
 *
 * <p>The actual SOAP call is <em>stubbed</em>: when
 * {@code nexgen.bureau.stub-enabled=true} (the default), a hardcoded
 * successful response is returned instead of invoking the real endpoint.
 */
@Service
public class BureauService {

    private static final Logger LOG = LoggerFactory.getLogger(BureauService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final BureauProperties properties;

    public BureauService(BureauProperties properties) {
        this.properties = properties;
    }

    /**
     * Builds a {@link BureauInquiryRequest} from the validated credit-risk
     * request.  Mirrors the field mapping performed by the legacy
     * {@code BureauRequestBuilder} Camel processor.
     */
    public BureauInquiryRequest buildRequest(CreditRiskReqType request) {
        BureauInquiryRequest bureauReq = new BureauInquiryRequest();

        BureauSubscriber subscriber = new BureauSubscriber();
        subscriber.setSubscriberCode(properties.getSubscriberCode());
        subscriber.setSubscriberName(properties.getSubscriberName());
        bureauReq.setSubscriber(subscriber);

        BureauSubject subject = new BureauSubject();
        subject.setFirstName(request.getFirstName());
        subject.setLastName(request.getLastName());
        subject.setDateOfBirth(request.getDateOfBirth());
        subject.setSocialInsuranceNumber(request.getSocialInsuranceNumber());
        subject.setProvince(request.getProvince());
        subject.setPostalCode(request.getPostalCode());
        bureauReq.setSubject(subject);

        bureauReq.setRequestId(properties.getRequestIdPrefix() + UUID.randomUUID().toString());
        bureauReq.setTimestamp(ZonedDateTime.now().format(TIMESTAMP_FORMAT));
        bureauReq.setProductType(request.getProductType());

        LOG.info("Bureau request built with requestId: {}", bureauReq.getRequestId());
        return bureauReq;
    }

    /**
     * Calls the credit bureau.  When {@code nexgen.bureau.stub-enabled=true}
     * a hardcoded stub response is returned; otherwise an
     * {@link UnsupportedOperationException} is thrown (real SOAP integration
     * is a future implementation task).
     */
    public BureauInquiryResponse callBureau(BureauInquiryRequest request) {
        if (properties.isStubEnabled()) {
            return createStubResponse(request);
        }
        throw new UnsupportedOperationException("Real bureau integration not implemented");
    }

    /**
     * Maps a {@link BureauInquiryResponse} to a {@link BureauResult}.
     * Preserves the error-handling semantics of the legacy
     * {@code BureauResponseMapper}:
     * <ul>
     *   <li>Null response → {@code hasError=true, errorCode="CR-301"}</li>
     *   <li>Error code in response → {@code hasError=true, errorCode="CR-302"}</li>
     * </ul>
     */
    public BureauResult mapResponse(BureauInquiryResponse response) {
        if (response == null) {
            LOG.warn("Bureau response is null, setting error properties");
            return new BureauResult(null, true, "CR-301", "Bureau response was null");
        }

        if (response.getErrorCode() != null) {
            LOG.warn("Bureau returned error: {} - {}", response.getErrorCode(), response.getErrorMessage());
            return new BureauResult(null, true, "CR-302", response.getErrorMessage());
        }

        CreditScoreDetail creditDetail = new CreditScoreDetail();
        creditDetail.setBureauScore(response.getCreditScore());
        creditDetail.setBureauScoreRange(mapScoreRange(response.getCreditScore()));
        creditDetail.setDelinquencyCount(response.getDelinquencyCount());
        creditDetail.setInquiryCount(response.getInquiryCount());
        creditDetail.setOpenAccountCount(response.getOpenTradelineCount());
        creditDetail.setTotalCreditLimit(response.getTotalCreditLimit());
        creditDetail.setTotalBalance(response.getTotalBalance());

        if (response.getTotalCreditLimit() != null && response.getTotalCreditLimit() > 0
                && response.getTotalBalance() != null) {
            creditDetail.setUtilizationRate(
                    response.getTotalBalance() / response.getTotalCreditLimit());
        } else {
            creditDetail.setUtilizationRate(0.0);
        }

        LOG.info("Bureau response mapped successfully. Bureau score: {}", response.getCreditScore());
        return new BureauResult(creditDetail, false, null, null);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String mapScoreRange(Integer score) {
        if (score == null) return "UNKNOWN";
        if (score >= 800) return "EXCEPTIONAL";
        if (score >= 740) return "VERY_GOOD";
        if (score >= 670) return "GOOD";
        if (score >= 580) return "FAIR";
        return "POOR";
    }

    private BureauInquiryResponse createStubResponse(BureauInquiryRequest request) {
        BureauInquiryResponse stub = new BureauInquiryResponse();
        stub.setRequestId(request.getRequestId());
        stub.setResponseId("STUB-" + UUID.randomUUID().toString());
        stub.setCreditScore(720);
        stub.setDelinquencyCount(0);
        stub.setInquiryCount(2);
        stub.setOpenTradelineCount(5);
        stub.setTotalCreditLimit(50000.0);
        stub.setTotalBalance(15000.0);
        return stub;
    }
}
