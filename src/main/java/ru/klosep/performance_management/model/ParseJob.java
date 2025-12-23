package ru.klosep.performance_management.model;

public class ParseJob {
    private String id;
    private String status; // RUNNING, COMPLETED, FAILED
    private String result;
    private long startTime;
    private long endTime;

    public ParseJob(String id) {
        this.id = id;
        this.status = "RUNNING";
        this.startTime = System.currentTimeMillis();
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
}
