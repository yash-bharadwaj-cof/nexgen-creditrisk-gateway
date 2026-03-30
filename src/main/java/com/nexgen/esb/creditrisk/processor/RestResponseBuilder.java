package com.nexgen.esb.creditrisk.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgen.esb.creditrisk.model.CreditRiskResType;

/**
 * Builds REST-specific response format from the common risk response.
 */
public class RestResponseBuilder implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(RestResponseBuilder.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        CreditRiskResType response = (CreditRiskResType) exchange.getProperty("RISK_RESPONSE");

        if (response == null) {
            response = exchange.getIn().getBody(CreditRiskResType.class);
        }

        if (response != null) {
            exchange.getIn().setBody(response);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/xml");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        }

        LOG.info("REST response built for applicant: {}",
                response != null ? response.getApplicantId() : "N/A");
    }
}
