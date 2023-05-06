package org.example;

import java.util.List;

public class Job {
    public Status status;
    public List<String> data;

    public Job(Status status, List<String> data) {
        this.status = status;
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public List<String> getData() {
        return data;
    }
}
