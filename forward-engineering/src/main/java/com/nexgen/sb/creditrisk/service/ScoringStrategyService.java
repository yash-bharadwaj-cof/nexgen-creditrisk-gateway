package com.nexgen.sb.creditrisk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nexgen.sb.creditrisk.config.ScoringProperties;
import com.nexgen.sb.creditrisk.model.CreditRiskReqType;
import com.nexgen.sb.creditrisk.scoring.AggressiveScoringStrategy;
import com.nexgen.sb.creditrisk.scoring.ConservativeScoringStrategy;
import com.nexgen.sb.creditrisk.scoring.ScoringStrategy;
import com.nexgen.sb.creditrisk.scoring.StandardScoringStrategy;

/**
 * Resolves the appropriate {@link ScoringStrategy} for a given credit-risk
 * request.  Replaces the legacy {@code ScoringStrategyProcessor} (Camel
 * {@code Processor} / {@code Exchange}) with a plain Spring {@code @Service}.
 *
 * <p>Resolution logic (mirrors legacy exactly):
 * <ol>
 *   <li>productType == MORTGAGE || AUTO_LOAN  → ConservativeScoringStrategy</li>
 *   <li>productType == CREDIT_CARD || LINE_OF_CREDIT → AggressiveScoringStrategy</li>
 *   <li>defaultStrategy == "conservative"  → ConservativeScoringStrategy</li>
 *   <li>else → StandardScoringStrategy</li>
 * </ol>
 */
@Service
public class ScoringStrategyService {

    private static final Logger LOG = LoggerFactory.getLogger(ScoringStrategyService.class);

    private final ScoringProperties scoringProperties;
    private final ConservativeScoringStrategy conservativeStrategy;
    private final AggressiveScoringStrategy aggressiveStrategy;
    private final StandardScoringStrategy standardStrategy;

    public ScoringStrategyService(ScoringProperties scoringProperties,
                                   ConservativeScoringStrategy conservativeStrategy,
                                   AggressiveScoringStrategy aggressiveStrategy,
                                   StandardScoringStrategy standardStrategy) {
        this.scoringProperties = scoringProperties;
        this.conservativeStrategy = conservativeStrategy;
        this.aggressiveStrategy = aggressiveStrategy;
        this.standardStrategy = standardStrategy;
    }

    /**
     * Resolves the scoring strategy for the given request.
     * The request must have been validated before calling this method;
     * a null {@code productType} is treated as absent and falls through
     * to the default-strategy check.
     *
     * @param request the incoming credit-risk request
     * @return the resolved {@link ScoringStrategy}
     */
    public ScoringStrategy resolveStrategy(CreditRiskReqType request) {
        String productType = request.getProductType();

        if ("MORTGAGE".equalsIgnoreCase(productType) || "AUTO_LOAN".equalsIgnoreCase(productType)) {
            LOG.info("Selected scoring strategy: {} for product: {}", conservativeStrategy.getStrategyName(), productType);
            return conservativeStrategy;
        }

        if ("CREDIT_CARD".equalsIgnoreCase(productType) || "LINE_OF_CREDIT".equalsIgnoreCase(productType)) {
            LOG.info("Selected scoring strategy: {} for product: {}", aggressiveStrategy.getStrategyName(), productType);
            return aggressiveStrategy;
        }

        if ("conservative".equalsIgnoreCase(scoringProperties.getDefaultStrategy())) {
            LOG.info("Selected scoring strategy: {} (default)", conservativeStrategy.getStrategyName());
            return conservativeStrategy;
        }

        LOG.info("Selected scoring strategy: {} (default)", standardStrategy.getStrategyName());
        return standardStrategy;
    }
}
