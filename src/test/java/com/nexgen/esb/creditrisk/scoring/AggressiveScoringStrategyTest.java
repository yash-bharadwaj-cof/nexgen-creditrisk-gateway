package com.nexgen.esb.creditrisk.scoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AggressiveScoringStrategy.
 * Verifies relaxed thresholds used for credit cards and lines of credit.
 */
class AggressiveScoringStrategyTest {

    private AggressiveScoringStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AggressiveScoringStrategy();
    }

    // -----------------------------------------------------------------------
    // categorizeRisk – aggressive (relaxed) thresholds
    // -----------------------------------------------------------------------

    @Test
    void testCategorizeRisk_Excellent() {
        // bureau >= 720, DTI < 0.40
        assertEquals("EXCELLENT", strategy.categorizeRisk(730, 0.30, 0.50));
    }

    @Test
    void testCategorizeRisk_Excellent_DtiBoundary_Fails() {
        // bureau >= 720 but DTI >= 0.40 → falls to GOOD check
        assertEquals("GOOD", strategy.categorizeRisk(730, 0.45, 0.50));
    }

    @Test
    void testCategorizeRisk_Good() {
        assertEquals("GOOD", strategy.categorizeRisk(660, 0.45, 0.60));
    }

    @Test
    void testCategorizeRisk_Fair() {
        assertEquals("FAIR", strategy.categorizeRisk(600, 0.50, 0.70));
    }

    @Test
    void testCategorizeRisk_Poor() {
        assertEquals("POOR", strategy.categorizeRisk(540, 0.55, 0.75));
    }

    @Test
    void testCategorizeRisk_VeryPoor() {
        assertEquals("VERY_POOR", strategy.categorizeRisk(500, 0.65, 0.85));
    }

    // -----------------------------------------------------------------------
    // calculateOverallScore
    // -----------------------------------------------------------------------

    @Test
    void testCalculateOverallScore_StrongProfile() {
        int score = strategy.calculateOverallScore(780, 0.15, 0.15, "FULL_TIME");
        assertTrue(score >= 75, "Expected score >= 75 for strong profile, got: " + score);
    }

    @Test
    void testCalculateOverallScore_WeakProfile() {
        int score = strategy.calculateOverallScore(480, 0.60, 0.90, "UNEMPLOYED");
        assertTrue(score <= 30, "Expected score <= 30 for weak profile, got: " + score);
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
    void testDetermineRecommendation_Excellent_Approve() {
        assertEquals("APPROVE", strategy.determineRecommendation("EXCELLENT", 10000, 60000));
    }

    @Test
    void testDetermineRecommendation_Good_Approve() {
        assertEquals("APPROVE", strategy.determineRecommendation("GOOD", 10000, 60000));
    }

    @Test
    void testDetermineRecommendation_Fair_ApproveWithConditions() {
        assertEquals("APPROVE_WITH_CONDITIONS",
                strategy.determineRecommendation("FAIR", 10000, 60000));
    }

    @Test
    void testDetermineRecommendation_Poor_Refer() {
        assertEquals("REFER_TO_UNDERWRITER",
                strategy.determineRecommendation("POOR", 10000, 60000));
    }

    @Test
    void testDetermineRecommendation_VeryPoor_Decline() {
        assertEquals("DECLINE", strategy.determineRecommendation("VERY_POOR", 10000, 60000));
    }

    // -----------------------------------------------------------------------
    // determineAffordabilityRating – relaxed thresholds
    // -----------------------------------------------------------------------

    @Test
    void testAffordabilityRating_Comfortable() {
        assertEquals("COMFORTABLE", strategy.determineAffordabilityRating(0.30));
    }

    @Test
    void testAffordabilityRating_Manageable() {
        assertEquals("MANAGEABLE", strategy.determineAffordabilityRating(0.40));
    }

    @Test
    void testAffordabilityRating_Stretched() {
        assertEquals("STRETCHED", strategy.determineAffordabilityRating(0.50));
    }

    @Test
    void testAffordabilityRating_Overextended() {
        assertEquals("OVEREXTENDED", strategy.determineAffordabilityRating(0.60));
    }

    // -----------------------------------------------------------------------
    // getStrategyName
    // -----------------------------------------------------------------------

    @Test
    void testGetStrategyName() {
        assertEquals("AGGRESSIVE", strategy.getStrategyName());
    }
}
