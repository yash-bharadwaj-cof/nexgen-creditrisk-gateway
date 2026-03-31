package com.nexgen.sb.creditrisk.service;

import com.nexgen.esb.creditrisk.model.CreditScoreDetail;

/**
 * Wrapper returned by {@link BureauService#mapResponse} carrying either the
 * successfully mapped {@link CreditScoreDetail} or error information when the
 * bureau call failed.
 */
public class BureauResult {

    private final CreditScoreDetail creditDetail;
    private final boolean hasError;
    private final String errorCode;
    private final String errorMessage;

    public BureauResult(CreditScoreDetail creditDetail, boolean hasError, String errorCode, String errorMessage) {
        this.creditDetail = creditDetail;
        this.hasError = hasError;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public CreditScoreDetail creditDetail() { return creditDetail; }
    public boolean hasError() { return hasError; }
    public String errorCode() { return errorCode; }
    public String errorMessage() { return errorMessage; }
}
