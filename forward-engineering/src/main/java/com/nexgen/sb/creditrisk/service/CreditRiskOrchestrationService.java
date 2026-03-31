package com.nexgen.sb.creditrisk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nexgen.sb.creditrisk.generated.BureauInquiryRequest;
import com.nexgen.sb.creditrisk.generated.BureauInquiryResponse;
import com.nexgen.sb.creditrisk.model.CreditRiskReqType;
import com.nexgen.sb.creditrisk.model.CreditRiskResType;
import com.nexgen.sb.creditrisk.scoring.ScoringStrategy;
import com.nexgen.sb.creditrisk.exception.CreditRiskProcessingException;
import com.nexgen.sb.creditrisk.exception.ValidationException;
import com.nexgen.sb.creditrisk.logging.TransactionLogService;

/**
 * Central orchestration service that replaces the four legacy Apache Camel XML routes:
 *
 * <ol>
 *   <li><strong>REST route</strong>: validate → strategy → bureau → risk → restResponse → wireTapLog</li>
 *   <li><strong>SOAP route</strong>: validate → strategy → bureau → risk → wireTapLog</li>
 *   <li><strong>Gateway route</strong>: gwPreProcess → validate → strategy → bureau → risk → wireTapLog</li>
 *   <li><strong>Logging route</strong>: direct:logTransaction → transactionLogger.logTransaction()</li>
 * </ol>
 *
 * <p>All four routes collapse into a single {@link #processRequest} call.
 * Asynchronous logging mirrors the Camel wireTap pattern.</p>
 */
@Service
public class CreditRiskOrchestrationService {

    private static final Logger LOG = LoggerFactory.getLogger(CreditRiskOrchestrationService.class);

    private final ValidationService validationService;
    private final ScoringStrategyService scoringStrategyService;
    private final BureauService bureauService;
    private final RiskCalculationService riskCalculationService;
    private final GatewayPreProcessService gatewayPreProcessService;
    private final TransactionLogService transactionLogService;

    public CreditRiskOrchestrationService(ValidationService validationService,
                                           ScoringStrategyService scoringStrategyService,
                                           BureauService bureauService,
                                           RiskCalculationService riskCalculationService,
                                           GatewayPreProcessService gatewayPreProcessService,
                                           TransactionLogService transactionLogService) {
        this.validationService = validationService;
        this.scoringStrategyService = scoringStrategyService;
        this.bureauService = bureauService;
        this.riskCalculationService = riskCalculationService;
        this.gatewayPreProcessService = gatewayPreProcessService;
        this.transactionLogService = transactionLogService;
    }

    /**
     * Processes a credit risk assessment request end-to-end.
     *
     * @param request the incoming credit risk request
     * @param channel the originating channel: {@code "REST"}, {@code "SOAP"}, or {@code "GATEWAY"}
     * @return the completed credit risk assessment response
     * @throws ValidationException           if the request fails business validation (re-thrown after async error log)
     * @throws CreditRiskProcessingException if an unexpected runtime error occurs
     */
    public CreditRiskResType processRequest(CreditRiskReqType request, String channel) {
        try {
            // 1. Gateway pre-processing (only for GATEWAY channel)
            if ("GATEWAY".equalsIgnoreCase(channel)) {
                request = gatewayPreProcessService.preProcess(request);
            }

            // 2. Validate request
            validationService.validate(request);

            // 3. Resolve scoring strategy
            ScoringStrategy strategy = scoringStrategyService.resolveStrategy(request);

            // 4. Build and call bureau (stubbed), then map the response
            BureauInquiryRequest bureauReq = bureauService.buildRequest(request);
            BureauInquiryResponse bureauResp = bureauService.callBureau(bureauReq);
            BureauResult bureauResult = bureauService.mapResponse(bureauResp);

            // 5. Calculate risk
            CreditRiskResType response = riskCalculationService.calculate(
                    request, bureauResult.creditDetail(), strategy, bureauResult.hasError());

            // 6. Async logging (replaces Camel wireTap)
            transactionLogService.logAsync(request, response, channel);

            LOG.info("Credit risk assessment completed for applicant: {}, channel: {}",
                    request.getApplicantId(), channel);
            return response;

        } catch (ValidationException e) {
            transactionLogService.logErrorAsync(request, e.getErrorCode(), e.getMessage(), channel);
            throw e;  // Let GlobalExceptionHandler handle it
        } catch (Exception e) {
            transactionLogService.logErrorAsync(request, "CR-500", e.getMessage(), channel);
            throw new CreditRiskProcessingException("Internal service error", e);
        }
    }
}
