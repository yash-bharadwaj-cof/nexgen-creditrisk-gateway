package com.nexgen.esb.creditrisk.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;

/**
 * Logs credit risk transactions to MongoDB.
 * Mirrors the Wire Tap logging pattern from the original Peril Score Gateway.
 */
public class TransactionLogger {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionLogger.class);
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private String mongoHost;
    private String mongoPort;
    private String mongoDb;
    private String mongoCollection;

    public void logTransaction(Exchange exchange) {
        try {
            Document logDoc = new Document();
            logDoc.append("transactionId", exchange.getProperty(LoggerConstants.LOG_TRANSACTION_ID, String.class));
            logDoc.append("applicantId", exchange.getProperty(LoggerConstants.LOG_APPLICANT_ID, String.class));
            logDoc.append("province", exchange.getProperty(LoggerConstants.LOG_PROVINCE, String.class));
            logDoc.append("productType", exchange.getProperty(LoggerConstants.LOG_PRODUCT_TYPE, String.class));
            logDoc.append("requestChannel", exchange.getProperty(LoggerConstants.LOG_REQUEST_CHANNEL, String.class));
            logDoc.append("sourceSystem", exchange.getProperty(LoggerConstants.LOG_SOURCE_SYSTEM, String.class));
            logDoc.append("timestamp", TIMESTAMP_FORMAT.format(new Date()));

            CreditRiskResType response = (CreditRiskResType) exchange.getProperty("RISK_RESPONSE");
            if (response != null) {
                logDoc.append("riskCategory", response.getRiskCategory());
                logDoc.append("overallScore", response.getOverallScore());
                logDoc.append("recommendation", response.getRecommendation());
                logDoc.append("status", "success");
            } else {
                logDoc.append("status", "error");
                logDoc.append("errorCode", exchange.getProperty("ERROR_CODE", String.class));
                logDoc.append("errorMessage", exchange.getProperty("ERROR_MESSAGE", String.class));
            }

            MongoClient client = new MongoClient(mongoHost, Integer.parseInt(mongoPort));
            try {
                MongoDatabase database = client.getDatabase(mongoDb);
                MongoCollection<Document> collection = database.getCollection(mongoCollection);
                collection.insertOne(logDoc);
                LOG.info("Transaction logged to MongoDB: {}", logDoc.getString("transactionId"));
            } finally {
                client.close();
            }

        } catch (Exception e) {
            LOG.error("Failed to log transaction to MongoDB", e);
        }
    }

    public String getMongoHost() { return mongoHost; }
    public void setMongoHost(String mongoHost) { this.mongoHost = mongoHost; }
    public String getMongoPort() { return mongoPort; }
    public void setMongoPort(String mongoPort) { this.mongoPort = mongoPort; }
    public String getMongoDb() { return mongoDb; }
    public void setMongoDb(String mongoDb) { this.mongoDb = mongoDb; }
    public String getMongoCollection() { return mongoCollection; }
    public void setMongoCollection(String mongoCollection) { this.mongoCollection = mongoCollection; }
}
