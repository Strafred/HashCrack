package org.example;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RequestRepository {
    private final MongoCollection<Document> requestsCollection;

    @Autowired
    public RequestRepository(MongoCollection<Document> requestsCollection) {
        this.requestsCollection = requestsCollection;
    }

    public void saveRequest(String xml) {
        Document document = new Document();
        document.append("xmlRequest", xml);

        requestsCollection.insertOne(document);
    }

    public FindIterable<Document> findAll() {
        return requestsCollection.find();
    }

    public void deleteRequest(Document task) {
        requestsCollection.deleteOne(task);
    }
}
