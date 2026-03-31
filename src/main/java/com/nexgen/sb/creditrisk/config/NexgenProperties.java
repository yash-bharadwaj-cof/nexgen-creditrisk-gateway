package com.nexgen.sb.creditrisk.config;

import org.springframework.stereotype.Component;

/**
 * Centralised application properties for the NexGen Credit Risk Spring Boot module.
 * Holds MongoDB configuration consumed by TransactionLogService.
 */
@Component
public class NexgenProperties {

    private MongoProperties mongo = new MongoProperties();

    public MongoProperties getMongo() {
        return mongo;
    }

    public void setMongo(MongoProperties mongo) {
        this.mongo = mongo;
    }

    public static class MongoProperties {

        private String host = "localhost";
        private int port = 27017;
        private String database = "nexgen_creditrisk";
        private String collection = "transactions";

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }

        public String getCollection() { return collection; }
        public void setCollection(String collection) { this.collection = collection; }
    }
}
