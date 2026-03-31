package com.nexgen.esb.creditrisk.processor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgen.esb.creditrisk.generated.BureauInquiryRequest;
import com.nexgen.esb.creditrisk.generated.BureauSubscriber;
import com.nexgen.esb.creditrisk.generated.BureauSubject;
import com.nexgen.esb.creditrisk.model.CreditRiskReqType;

/**
 * Builds the outbound SOAP request to the external credit bureau service.
 * Maps internal request fields to the bureau's expected format.
 */
public class BureauRequestBuilder implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(BureauRequestBuilder.class);
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private String requestIdPrefix;
    private String subscriberCode;
    private String subscriberName;

    @Override
    public void process(Exchange exchange) throws Exception {
        CreditRiskReqType request = (CreditRiskReqType) exchange.getProperty("VALIDATED_REQUEST");

        BureauInquiryRequest bureauRequest = new BureauInquiryRequest();

        // Build subscriber info
        BureauSubscriber subscriber = new BureauSubscriber();
        subscriber.setSubscriberCode(subscriberCode);
        subscriber.setSubscriberName(subscriberName);
        bureauRequest.setSubscriber(subscriber);

        // Build subject info from applicant
        BureauSubject subject = new BureauSubject();
        subject.setFirstName(request.getFirstName());
        subject.setLastName(request.getLastName());
        subject.setDateOfBirth(request.getDateOfBirth());
        subject.setSocialInsuranceNumber(request.getSocialInsuranceNumber());
        subject.setProvince(request.getProvince());
        subject.setPostalCode(request.getPostalCode());
        bureauRequest.setSubject(subject);

        // Set request metadata
        String requestId = requestIdPrefix + UUID.randomUUID().toString();
        bureauRequest.setRequestId(requestId);
        bureauRequest.setTimestamp(TIMESTAMP_FORMAT.format(new Date()));
        bureauRequest.setProductType(request.getProductType());

        // Store for later reference
        exchange.setProperty("BUREAU_REQUEST_ID", requestId);

        Message outMessage = exchange.getMessage();
        outMessage.setBody(bureauRequest);
        outMessage.setHeader("operationName", "inquire");
        outMessage.setHeader("operationNamespace", "http://ws.esb.nexgen.com/bureau/v1");

        LOG.info("Bureau request built with requestId: {}", requestId);
    }

    public String getRequestIdPrefix() { return requestIdPrefix; }
    public void setRequestIdPrefix(String requestIdPrefix) { this.requestIdPrefix = requestIdPrefix; }
    public String getSubscriberCode() { return subscriberCode; }
    public void setSubscriberCode(String subscriberCode) { this.subscriberCode = subscriberCode; }
    public String getSubscriberName() { return subscriberName; }
    public void setSubscriberName(String subscriberName) { this.subscriberName = subscriberName; }
}
