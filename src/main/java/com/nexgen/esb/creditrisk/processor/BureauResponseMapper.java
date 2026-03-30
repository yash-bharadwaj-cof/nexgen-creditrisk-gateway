package com.nexgen.esb.creditrisk.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgen.esb.creditrisk.generated.BureauInquiryResponse;
import com.nexgen.esb.creditrisk.model.*;

/**
 * Maps the external credit bureau SOAP response to the internal credit score details.
 */
public class BureauResponseMapper implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(BureauResponseMapper.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        BureauInquiryResponse bureauResponse = exchange.getIn().getBody(BureauInquiryResponse.class);

        if (bureauResponse == null) {
            LOG.warn("Bureau response is null, setting error properties");
            exchange.setProperty("BUREAU_ERROR", true);
            exchange.setProperty("BUREAU_ERROR_CODE", "CR-301");
            return;
        }

        if (bureauResponse.getErrorCode() != null) {
            LOG.warn("Bureau returned error: {} - {}", bureauResponse.getErrorCode(), bureauResponse.getErrorMessage());
            exchange.setProperty("BUREAU_ERROR", true);
            exchange.setProperty("BUREAU_ERROR_CODE", "CR-302");
            exchange.setProperty("BUREAU_ERROR_MSG", bureauResponse.getErrorMessage());
            return;
        }

        // Map credit score details
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

        exchange.setProperty("CREDIT_SCORE_DETAIL", creditDetail);
        exchange.setProperty("BUREAU_ERROR", false);

        LOG.info("Bureau response mapped successfully. Bureau score: {}", bureauResponse.getCreditScore());
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
