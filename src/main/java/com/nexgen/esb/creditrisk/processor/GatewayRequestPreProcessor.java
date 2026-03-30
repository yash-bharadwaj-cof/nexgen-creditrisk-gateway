package com.nexgen.esb.creditrisk.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.RequestHeader;

/**
 * Pre-processes requests from the Gateway (GW) SOAP endpoint.
 * Maps external request format to internal format when coming from
 * integration systems like policy admin platforms.
 */
public class GatewayRequestPreProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayRequestPreProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        CreditRiskReqType request = exchange.getIn().getBody(CreditRiskReqType.class);

        if (request != null) {
            if (request.getRequestHeader() == null) {
                RequestHeader header = new RequestHeader();
                header.setSourceSystem("GATEWAY");
                header.setTimestamp(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date()));
                header.setTransactionId(java.util.UUID.randomUUID().toString());
                request.setRequestHeader(header);
            }

            if (request.getRequestChannel() == null) {
                request.setRequestChannel("API");
            }

            exchange.getIn().setBody(request);
            LOG.info("Gateway request pre-processed for applicant: {}", request.getApplicantId());
        }
    }
}
