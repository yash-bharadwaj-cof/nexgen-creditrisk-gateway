package com.nexgen.esb.creditrisk.scoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConservativeScoringStrategy.
 * Verifies tighter thresholds used for mortgages and auto-loans.
 */
class ConservativeScoringStrategyTest {

    private ConservativeScoringStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ConservativeScoringStrategy();
    }

    // -----------------------------------------------------------------------
    // categorizeRisk – conservative thresholds (higher scores required)
    // -----------------------------------------------------------------------

    @Test
    void testCategorizeRisk_Excellent() {
        // bureau >= 780, DTI < 0.25, util < 0.25
        assertEquals("EXCELLENT", strategy.categorizeRisk(790, 0.20, 0.20));
    }

    @Test
    void testCategorizeRisk_Good() {
        // bureau >= 720 AND DTI < 0.35
        assertEquals("GOOD", strategy.categorizeRisk(730, 0.30, 0.50));
    }

    @Test
    void testCategorizeRisk_Fair() {
        assertEquals("FAIR", strategy.categorizeRisk(670, 0.45, 0.60));
    }

    @Test
    void testCategorizeRisk_Poor() {
        assertEquals("POOR", strategy.categorizeRisk(620, 0.50, 0.70));
    }

    @Test
    void testCategorizeRisk_VeryPoor() {
        assertEquals("VERY_POOR", strategy.categorizeRisk(550, 0.65, 0.80));
    }

    // -----------------------------------------------------------------------
    // calculateOverallScore
    // -----------------------------------------------------------------------

    @Test
    void testCalculateOverallScore_PrimeBorrower() {
        int score = strategy.calculateOverallScore(800, 0.10, 0.10, "FULL_TIME");
        assertTrue(score >= 75, "Expected score >= 75 for prime borrower, got: " + score);
    }

    @Test
    void testCalculateOverallScore_SubprimeBorrower() {
        int score = strategy.calculateOverallScore(550, 0.55, 0.75, "UNEMPLOYED");
        assertTrue(score <= 20, "Expected score <= 20 for subprime borrower, got: " + score);
    }

    @Test
    void testCalculateOverallScore_BoundedBetween0And100() {
        int low  = strategy.calculateOverallScore(300, 1.0, 1.0, "UNEMPLOYED");
        int high = strategy.calculateOverallScore(850, 0.0, 0.0, "FULL_TIME");
        assertTrue(low  >= 0);
        assertTrue(high <= 100);
    }

    // -----------------------------------------------------------------------
    // determineRecommendation
    // -----------------------------------------------------------------------

    @Test
    void testDetermineRecommendation_Excellent_LowRatio_Approve() {
        assertEquals("APPROVE", strategy.determineRecommendation("EXCELLENT", 200000, 80000));
    }

    @Test
    void testDetermineRecommendation_Excellent_HighRatio_ApproveWithConditions() {
        // ratio > 5 → APPROVE_WITH_CONDITIONS
        assertEquals("APPROVE_WITH_CONDITIONS",
                strategy.determineRecommendation("EXCELLENT", 600000, 80000));
    }

    @Test
    void testDetermineRecommendation_Fair_ReferToUnderwriter() {
        assertEquals("REFER_TO_UNDERWRITER",
                strategy.determineRecommendation("FAIR", 200000, 80000));
    }

    @Test
    void testDetermineRecommendation_Poor_Decline() {
        assertEquals("DECLINE", strategy.determineRecommendation("POOR", 200000, 80000));
    }

    @Test
    void testDetermineRecommendation_VeryPoor_Decline() {
        assertEquals("DECLINE", strategy.determineRecommendation("VERY_POOR", 200000, 80000));
    }

    // -----------------------------------------------------------------------
    // determineAffordabilityRating – conservative thresholds
    // -----------------------------------------------------------------------

    @Test
    void testAffordabilityRating_Comfortable() {
        assertEquals("COMFORTABLE", strategy.determineAffordabilityRating(0.20));
    }

    @Test
    void testAffordabilityRating_Manageable() {
        assertEquals("MANAGEABLE", strategy.determineAffordabilityRating(0.28));
    }

    @Test
    void testAffordabilityRating_Stretched() {
        assertEquals("STRETCHED", strategy.determineAffordabilityRating(0.36));
    }

    @Test
    void testAffordabilityRating_Overextended() {
        assertEquals("OVEREXTENDED", strategy.determineAffordabilityRating(0.45));
    }

    // -----------------------------------------------------------------------
    // getStrategyName
    // -----------------------------------------------------------------------

    @Test
    void testGetStrategyName() {
        assertEquals("CONSERVATIVE", strategy.getStrategyName());
    }
}
