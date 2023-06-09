package org.example;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.mongodb.client.model.Filters;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ManagerRepository {
    private final MongoCollection<Document> jobsCollection;
    private ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    @Autowired
    public ManagerRepository(MongoCollection<Document> jobsCollection) {
        this.jobsCollection = jobsCollection;
        jobsCollection.find().forEach(document -> {
            String requestId = document.getString("requestId");
            Status jobStatus = Status.valueOf(document.get("jobStatus", String.class));
            List<String> jobData = document.getList("jobData", String.class);
            List<Boolean> parts = document.getList("parts", Boolean.class);
            jobs.put(requestId, new Job(jobStatus, jobData, parts));
        });
    }

    private void saveJob2Db(String requestId, Job job) {
        Document document = new Document();
        document.append("requestId", requestId);
        document.append("jobStatus", job.getStatus());
        document.append("jobData", job.getData());
        document.append("parts", job.getParts());
        jobsCollection.updateOne(
                Filters.eq("requestId", requestId),
                new Document("$set", document),
                new UpdateOptions().upsert(true)
        );
    }

    public void insertJob(String requestId, Job job) {
        jobs.put(requestId, job);
        saveJob2Db(requestId, job);
    }

    public Job getJob(String requestId) {
        return jobs.get(requestId);
    }

    public void makeJobPartDone(String requestId, List<String> words, int partNumber) {
        Job job = jobs.get(requestId);
        job.getParts().set(partNumber, true);
        if (job.getParts().stream().allMatch(bool -> bool)) {
            job.setStatus(Status.READY);
        }
        job.data.addAll(words);
        saveJob2Db(requestId, job);
    }
}
