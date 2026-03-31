package com.nexgen.esb.creditrisk.scoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StandardScoringStrategy.
 * Verifies risk categorisation, score calculation, recommendations, and affordability ratings.
 */
class StandardScoringStrategyTest {

    private StandardScoringStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new StandardScoringStrategy();
    }

    // -----------------------------------------------------------------------
    // categorizeRisk
    // -----------------------------------------------------------------------

    @Test
    void testCategorizeRisk_Excellent() {
        // bureau >= 750, DTI < 0.30, util < 0.30
        assertEquals("EXCELLENT", strategy.categorizeRisk(780, 0.20, 0.25));
    }

    @Test
    void testCategorizeRisk_ExcellentBoundary_FailsDtiCheck() {
        // bureau >= 750 but DTI >= 0.30 → falls through to GOOD check
        assertEquals("GOOD", strategy.categorizeRisk(760, 0.35, 0.25));
    }

    @Test
    void testCategorizeRisk_Good() {
        assertEquals("GOOD", strategy.categorizeRisk(700, 0.40, 0.50));
    }

    @Test
    void testCategorizeRisk_Fair() {
        assertEquals("FAIR", strategy.categorizeRisk(640, 0.45, 0.60));
    }

    @Test
    void testCategorizeRisk_Poor() {
        assertEquals("POOR", strategy.categorizeRisk(580, 0.50, 0.70));
    }

    @Test
    void testCategorizeRisk_VeryPoor() {
        assertEquals("VERY_POOR", strategy.categorizeRisk(520, 0.60, 0.80));
    }

    // -----------------------------------------------------------------------
    // calculateOverallScore
    // -----------------------------------------------------------------------

    @Test
    void testCalculateOverallScore_FullTimeExcellentProfile() {
        // High bureau score, low DTI, low util, full-time employment → high score
        int score = strategy.calculateOverallScore(800, 0.10, 0.10, "FULL_TIME");
        assertTrue(score >= 80, "Expected score >= 80 for excellent profile, got: " + score);
    }

    @Test
    void testCalculateOverallScore_UnemployedPoorProfile() {
        // Low bureau score, high DTI, high util, unemployed → low score
        int score = strategy.calculateOverallScore(500, 0.55, 0.80, "UNEMPLOYED");
        assertTrue(score <= 30, "Expected score <= 30 for poor profile, got: " + score);
    }

    @Test
    void testCalculateOverallScore_BoundedBetween0And100() {
        int low = strategy.calculateOverallScore(300, 1.0, 1.0, "UNEMPLOYED");
        int high = strategy.calculateOverallScore(850, 0.0, 0.0, "FULL_TIME");
        assertTrue(low >= 0);
        assertTrue(high <= 100);
    }

    // -----------------------------------------------------------------------
    // determineRecommendation
    // -----------------------------------------------------------------------

    @Test
    void testDetermineRecommendation_Approve_Excellent() {
        assertEquals("APPROVE", strategy.determineRecommendation("EXCELLENT", 50000, 80000));
    }

    @Test
    void testDetermineRecommendation_Decline_VeryPoor() {
        assertEquals("DECLINE", strategy.determineRecommendation("VERY_POOR", 50000, 80000));
    }

    @Test
    void testDetermineRecommendation_Good_HighRatio_ApproveWithConditions() {
        // ratio > 5 → APPROVE_WITH_CONDITIONS
        assertEquals("APPROVE_WITH_CONDITIONS",
                strategy.determineRecommendation("GOOD", 600000, 80000));
    }

    @Test
    void testDetermineRecommendation_Fair_ReferToUnderwriter() {
        // ratio > 3 → REFER
        assertEquals("REFER_TO_UNDERWRITER",
                strategy.determineRecommendation("FAIR", 400000, 80000));
    }

    // -----------------------------------------------------------------------
    // determineAffordabilityRating
    // -----------------------------------------------------------------------

    @Test
    void testAffordabilityRating_Comfortable() {
        assertEquals("COMFORTABLE", strategy.determineAffordabilityRating(0.25));
    }

    @Test
    void testAffordabilityRating_Manageable() {
        assertEquals("MANAGEABLE", strategy.determineAffordabilityRating(0.32));
    }

    @Test
    void testAffordabilityRating_Stretched() {
        assertEquals("STRETCHED", strategy.determineAffordabilityRating(0.40));
    }

    @Test
    void testAffordabilityRating_Overextended() {
        assertEquals("OVEREXTENDED", strategy.determineAffordabilityRating(0.50));
    }

    // -----------------------------------------------------------------------
    // getStrategyName
    // -----------------------------------------------------------------------

    @Test
    void testGetStrategyName() {
        assertEquals("STANDARD", strategy.getStrategyName());
    }
}
