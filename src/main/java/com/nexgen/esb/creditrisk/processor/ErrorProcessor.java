package com.nexgen.esb.creditrisk.processor;

import javax.ws.rs.core.Response;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import com.nexgen.esb.creditrisk.model.ResponseHeader;

/**
 * Centralized error handling processor.
 * Maps exceptions to appropriate HTTP status codes and error responses.
 */
public class ErrorProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

        CreditRiskResType errorResponse = new CreditRiskResType();
        ResponseHeader header = new ResponseHeader();
        int httpStatus;

        if (cause instanceof CreditRiskRequestValidator.ValidationException) {
            CreditRiskRequestValidator.ValidationException validationEx =
                    (CreditRiskRequestValidator.ValidationException) cause;
            header.setStatusCode(validationEx.getErrorCode());
            header.setStatusMessage(validationEx.getMessage());
            httpStatus = Response.Status.BAD_REQUEST.getStatusCode();

            LOG.warn("Validation error: {} - {}", validationEx.getErrorCode(), validationEx.getMessage());
        } else {
            header.setStatusCode("CR-500");
            header.setStatusMessage("Internal service error occurred. Please contact support.");
            httpStatus = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

            LOG.error("Unexpected error in credit risk processing", cause);
        }

        header.setTimestamp(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date()));
        errorResponse.setResponseHeader(header);
        errorResponse.setRiskCategory("ERROR");
        errorResponse.setRecommendation("REFER_TO_UNDERWRITER");

        exchange.getIn().setBody(errorResponse);
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, httpStatus);
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/xml");

        exchange.setProperty("ERROR_CODE", header.getStatusCode());
        exchange.setProperty("ERROR_MESSAGE", header.getStatusMessage());
    }
}
