package com.nexgen.esb.creditrisk.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.scoring.ScoringStrategy;
import com.nexgen.esb.creditrisk.scoring.StandardScoringStrategy;
import com.nexgen.esb.creditrisk.scoring.ConservativeScoringStrategy;
import com.nexgen.esb.creditrisk.scoring.AggressiveScoringStrategy;

/**
 * Strategy Pattern: Selects the appropriate scoring strategy based on
 * product type and request channel. Different from the original service's
 * procedural approach - uses Strategy pattern for extensible scoring logic.
 */
public class ScoringStrategyProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(ScoringStrategyProcessor.class);

    private String defaultStrategy;

    @Override
    public void process(Exchange exchange) throws Exception {
        CreditRiskReqType request = (CreditRiskReqType) exchange.getProperty("VALIDATED_REQUEST");

        ScoringStrategy strategy = resolveStrategy(request);
        exchange.setProperty("SCORING_STRATEGY", strategy);

        LOG.info("Selected scoring strategy: {} for product: {}, channel: {}",
                strategy.getStrategyName(), request.getProductType(), request.getRequestChannel());
    }

    private ScoringStrategy resolveStrategy(CreditRiskReqType request) {
        String productType = request.getProductType();

        if ("MORTGAGE".equalsIgnoreCase(productType) || "AUTO_LOAN".equalsIgnoreCase(productType)) {
            return new ConservativeScoringStrategy();
        }

        if ("CREDIT_CARD".equalsIgnoreCase(productType) || "LINE_OF_CREDIT".equalsIgnoreCase(productType)) {
            return new AggressiveScoringStrategy();
        }

        if ("conservative".equalsIgnoreCase(defaultStrategy)) {
            return new ConservativeScoringStrategy();
        }

        return new StandardScoringStrategy();
    }

    public String getDefaultStrategy() { return defaultStrategy; }
    public void setDefaultStrategy(String defaultStrategy) { this.defaultStrategy = defaultStrategy; }
}
