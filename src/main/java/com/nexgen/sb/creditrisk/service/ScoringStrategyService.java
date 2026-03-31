package com.nexgen.sb.creditrisk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.scoring.AggressiveScoringStrategy;
import com.nexgen.esb.creditrisk.scoring.ConservativeScoringStrategy;
import com.nexgen.esb.creditrisk.scoring.ScoringStrategy;
import com.nexgen.esb.creditrisk.scoring.StandardScoringStrategy;

/**
 * Resolves the scoring strategy to apply based on the requested product type.
 * Migrated from legacy {@code ScoringStrategyProcessor} Camel Processor.
 */
@Service
public class ScoringStrategyService {

    private static final Logger LOG = LoggerFactory.getLogger(ScoringStrategyService.class);

    @Value("${scoring.default-strategy:standard}")
    private String defaultStrategy;

    /**
     * Returns the appropriate {@link ScoringStrategy} for the given request.
     * <ul>
     *   <li>MORTGAGE / AUTO_LOAN → Conservative</li>
     *   <li>CREDIT_CARD / LINE_OF_CREDIT → Aggressive</li>
     *   <li>All others → Standard (or Conservative if default is configured)</li>
     * </ul>
     */
    public ScoringStrategy resolveStrategy(CreditRiskReqType request) {
        String productType = request.getProductType();

        ScoringStrategy strategy;
        if ("MORTGAGE".equalsIgnoreCase(productType) || "AUTO_LOAN".equalsIgnoreCase(productType)) {
            strategy = new ConservativeScoringStrategy();
        } else if ("CREDIT_CARD".equalsIgnoreCase(productType)
                || "LINE_OF_CREDIT".equalsIgnoreCase(productType)) {
            strategy = new AggressiveScoringStrategy();
        } else if ("conservative".equalsIgnoreCase(defaultStrategy)) {
            strategy = new ConservativeScoringStrategy();
        } else {
            strategy = new StandardScoringStrategy();
        }

        LOG.info("Selected scoring strategy: {} for product: {}, channel: {}",
                strategy.getStrategyName(), request.getProductType(), request.getRequestChannel());
        return strategy;
    }
}
