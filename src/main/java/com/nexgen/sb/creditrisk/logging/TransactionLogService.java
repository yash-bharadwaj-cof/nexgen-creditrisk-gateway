package com.nexgen.sb.creditrisk.logging;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import com.nexgen.sb.creditrisk.config.NexgenProperties;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Asynchronous MongoDB transaction logger.
 * Replaces the legacy {@code TransactionLogger} which created a new MongoClient on every call.
 * Uses a shared Spring-managed {@link MongoTemplate} and {@code @Async} to avoid blocking the
 * request/response path.  applicantId is PII-masked before persistence.
 */
@Service
public class TransactionLogService {

    private static final Logger log = LoggerFactory.getLogger(TransactionLogService.class);

    private final MongoTemplate mongoTemplate;
    private final NexgenProperties properties;

    public TransactionLogService(MongoTemplate mongoTemplate, NexgenProperties properties) {
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
    }

    /**
     * Logs a successful credit-risk assessment asynchronously.
     *
     * @param request  incoming request (may be {@code null})
     * @param response assessment response
     * @param channel  originating request channel
     */
    @Async
    public void logAsync(CreditRiskReqType request, CreditRiskResType response, String channel) {
        try {
            Document doc = new Document();
            doc.append("transactionId", getTransactionId(request));
            doc.append("applicantId", maskPii(request != null ? request.getApplicantId() : null));
            doc.append("province", request != null ? request.getProvince() : null);
            doc.append("productType", request != null ? request.getProductType() : null);
            doc.append("requestChannel", channel);
            doc.append("sourceSystem", getSourceSystem(request));
            doc.append("timestamp", Instant.now().toString());
            doc.append("riskCategory", response.getRiskCategory());
            doc.append("overallScore", response.getOverallScore());
            doc.append("recommendation", response.getRecommendation());
            doc.append("status", "success");
            mongoTemplate.insert(doc, properties.getMongo().getCollection());
        } catch (Exception e) {
            log.error("Failed to log transaction to MongoDB", e);
        }
    }

    /**
     * Logs a failed credit-risk assessment asynchronously.
     *
     * @param request    incoming request (may be {@code null} on early failures)
     * @param errorCode  machine-readable error code
     * @param errorMsg   human-readable error description
     * @param channel    originating request channel
     */
    @Async
    public void logErrorAsync(CreditRiskReqType request, String errorCode, String errorMsg, String channel) {
        try {
            Document doc = new Document();
            doc.append("transactionId", getTransactionId(request));
            doc.append("applicantId", maskPii(request != null ? request.getApplicantId() : null));
            doc.append("status", "error");
            doc.append("errorCode", errorCode);
            doc.append("errorMessage", errorMsg);
            doc.append("requestChannel", channel);
            doc.append("timestamp", Instant.now().toString());
            mongoTemplate.insert(doc, properties.getMongo().getCollection());
        } catch (Exception e) {
            log.error("Failed to log error transaction to MongoDB", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Masks a PII value, retaining only the last 4 characters.
     * Returns {@code "****"} when the value is {@code null} or shorter than 4 characters.
     */
    private String maskPii(String value) {
        if (value == null || value.length() <= 4) return "****";
        return "****" + value.substring(value.length() - 4);
    }

    private String getTransactionId(CreditRiskReqType request) {
        if (request == null || request.getRequestHeader() == null) {
            return null;
        }
        return request.getRequestHeader().getTransactionId();
    }

    private String getSourceSystem(CreditRiskReqType request) {
        if (request == null || request.getRequestHeader() == null) {
            return null;
        }
        return request.getRequestHeader().getSourceSystem();
    }
}
