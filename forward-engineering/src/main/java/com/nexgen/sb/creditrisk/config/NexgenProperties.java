package com.nexgen.sb.creditrisk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nexgen")
public class NexgenProperties {

    private String supportedProvinces = "ON,BC,AB,QC";
    private MongoProps mongo = new MongoProps();

    public String getSupportedProvinces() {
        return supportedProvinces;
    }

    public void setSupportedProvinces(String supportedProvinces) {
        this.supportedProvinces = supportedProvinces;
    }

    public MongoProps getMongo() {
        return mongo;
    }

    public void setMongo(MongoProps mongo) {
        this.mongo = mongo;
    }

    public static class MongoProps {

        private String collection = "transactions";

        public String getCollection() {
            return collection;
        }

        public void setCollection(String collection) {
            this.collection = collection;
        }
    }
}
