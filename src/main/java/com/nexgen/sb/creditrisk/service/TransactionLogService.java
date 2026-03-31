package com.nexgen.sb.creditrisk.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;

/**
 * Asynchronously logs credit risk transactions to MongoDB.
 * Replaces the Camel wireTap + {@code TransactionLogger} route (Route 4).
 *
 * <p>Both methods are annotated with {@link Async} so they execute on a
 * separate thread pool without blocking the caller, mirroring the non-blocking
 * behaviour of the original Camel wireTap pattern.</p>
 */
@Service
public class TransactionLogService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionLogService.class);
    private static final SimpleDateFormat TIMESTAMP_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Value("${mongo.host:localhost}")
    private String mongoHost;

    @Value("${mongo.port:27017}")
    private int mongoPort;

    @Value("${mongo.db:nexgen_creditrisk}")
    private String mongoDb;

    @Value("${mongo.collection:transactions}")
    private String mongoCollection;

    private MongoClient mongoClient;

    @PostConstruct
    public void init() {
        mongoClient = new MongoClient(mongoHost, mongoPort);
    }

    @PreDestroy
    public void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    /**
     * Asynchronously logs a successful transaction.
     *
     * @param request  the validated credit risk request
     * @param response the calculated risk response
     * @param channel  the originating channel (REST, SOAP, GATEWAY)
     */
    @Async
    public void logAsync(CreditRiskReqType request, CreditRiskResType response, String channel) {
        try {
            Document doc = buildBaseDocument(request, channel);
            if (response != null) {
                doc.append("riskCategory", response.getRiskCategory());
                doc.append("overallScore", response.getOverallScore());
                doc.append("recommendation", response.getRecommendation());
            }
            doc.append("status", "success");
            persist(doc);
        } catch (Exception e) {
            LOG.error("Failed to log transaction to MongoDB", e);
        }
    }

    /**
     * Asynchronously logs an error during request processing.
     *
     * @param request      the credit risk request (may be {@code null} on early failures)
     * @param errorCode    the error code (e.g. CR-001, CR-500)
     * @param errorMessage human-readable error description
     * @param channel      the originating channel
     */
    @Async
    public void logErrorAsync(CreditRiskReqType request, String errorCode,
                               String errorMessage, String channel) {
        try {
            Document doc = buildBaseDocument(request, channel);
            doc.append("status", "error");
            doc.append("errorCode", errorCode);
            doc.append("errorMessage", errorMessage);
            persist(doc);
        } catch (Exception e) {
            LOG.error("Failed to log error transaction to MongoDB", e);
        }
    }

    private Document buildBaseDocument(CreditRiskReqType request, String channel) {
        Document doc = new Document();
        doc.append("timestamp", TIMESTAMP_FORMAT.format(new Date()));
        doc.append("requestChannel", channel);
        if (request != null) {
            doc.append("applicantId", request.getApplicantId());
            doc.append("province", request.getProvince());
            doc.append("productType", request.getProductType());
            if (request.getRequestHeader() != null) {
                doc.append("transactionId", request.getRequestHeader().getTransactionId());
                doc.append("sourceSystem", request.getRequestHeader().getSourceSystem());
            }
        }
        return doc;
    }

    private void persist(Document doc) {
        MongoDatabase database = mongoClient.getDatabase(mongoDb);
        MongoCollection<Document> collection = database.getCollection(mongoCollection);
        collection.insertOne(doc);
        LOG.info("Transaction logged to MongoDB: {}", doc.getString("transactionId"));
    }
}
