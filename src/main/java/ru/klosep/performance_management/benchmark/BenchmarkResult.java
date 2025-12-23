package ru.klosep.performance_management.benchmark;

public class BenchmarkResult {
    private final String method;
    private final String operation;
    private final int recordCount;
    private final long durationMs;
    private final long memoryUsedBytes;

    public BenchmarkResult(String method, String operation, int recordCount,
                           long durationMs, long memoryUsedBytes) {
        this.method = method;
        this.operation = operation;
        this.recordCount = recordCount;
        this.durationMs = durationMs;
        this.memoryUsedBytes = memoryUsedBytes;
    }

    public double getThroughput() {
        return (recordCount * 1000.0) / durationMs; // records/sec
    }

    public void print() {
        System.out.printf("%-20s | %-20s | Records: %,10d | Time: %,8d ms | " +
                        "Throughput: %,10.0f ops/sec | Memory: %.2f MB\n",
                method, operation, recordCount, durationMs, getThroughput(),
                memoryUsedBytes / (1024.0 * 1024.0));
    }

    // Геттеры
    public String getMethod() { return method; }
    public String getOperation() { return operation; }
    public long getDurationMs() { return durationMs; }
    public long getMemoryUsedBytes() { return memoryUsedBytes; }
}
