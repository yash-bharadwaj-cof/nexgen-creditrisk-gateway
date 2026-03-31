package com.nexgen.sb.creditrisk.service;

import com.nexgen.sb.creditrisk.model.CreditScoreDetail;

/**
 * Holds the result of a credit bureau inquiry, combining the mapped
 * {@link CreditScoreDetail} and a flag indicating whether the bureau
 * returned an error.
 */
public class BureauResult {

    private final CreditScoreDetail creditDetail;
    private final boolean hasError;

    public BureauResult(CreditScoreDetail creditDetail, boolean hasError) {
        this.creditDetail = creditDetail;
        this.hasError = hasError;
    }

    /** The mapped credit score details from the bureau response, or {@code null} when {@link #hasError()} is true. */
    public CreditScoreDetail creditDetail() {
        return creditDetail;
    }

    /** Returns {@code true} if the bureau call failed or returned an error response. */
    public boolean hasError() {
        return hasError;
    }
}
