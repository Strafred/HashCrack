package org.example;

import java.util.ArrayList;
import java.util.List;

public class Job {
    public Status status;
    public List<String> data;
    public List<Boolean> parts;

    public Job(Status status, List<String> data, int partCount) {
        System.out.println("Job constructor");
        System.out.println(status);
        System.out.println(data);
        System.out.println(partCount);
        System.out.println("Job constructor");

        this.status = status;
        this.data = data;
        parts = new ArrayList<>(partCount);
        for (int i = 0; i < partCount; i++) {
            parts.add(false);
        }
    }

    public Job(Status status, List<String> data, List<Boolean> parts) {
        this.status = status;
        this.data = data;
        this.parts = parts;
    }

    public Status getStatus() {
        return status;
    }

    public List<String> getData() {
        return data;
    }

    public int getPartCount() {
        return parts.size();
    }

    public List<Boolean> getParts() {
        return parts;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
