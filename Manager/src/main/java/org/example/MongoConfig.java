package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {
    @Bean
    public MongoCollection<Document> jobsCollection() {
        MongoClient mongoClient = MongoClients.create("mongodb://mongodb1:27017,mongodb2:27017,mongodb3:27017");
        MongoDatabase database = mongoClient.getDatabase("manager");
        List<String> collectionNames = database.listCollectionNames().into(new ArrayList<>());
        if (!collectionNames.contains("jobs")) {
            database.createCollection("jobs");
        }
        return database.getCollection("jobs");
    }

    @Bean
    public MongoCollection<Document> requestsCollection() {
        MongoClient mongoClient = MongoClients.create("mongodb://mongodb1:27017,mongodb2:27017,mongodb3:27017");
        MongoDatabase database = mongoClient.getDatabase("manager");
        List<String> collectionNames = database.listCollectionNames().into(new ArrayList<>());
        if (!collectionNames.contains("requests")) {
            database.createCollection("requests");
        }
        return database.getCollection("requests");
    }
}
