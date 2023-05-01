package org.example;

public class Job {
    public Status status;
    public String[] data;

    public Job(Status status, String[] data) {
        this.status = status;
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public String[] getData() {
        return data;
    }
}
