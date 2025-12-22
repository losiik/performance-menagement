package ru.klosep.performance_management.benchmark;

import org.openjdk.jmh.annotations.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class MapBenchmark {

    private ConcurrentHashMap<String, String> concurrentMap;
    private Map<String, String> synchronizedMap;

    @Setup
    public void setup() {
        concurrentMap = new ConcurrentHashMap<>();
        synchronizedMap = Collections.synchronizedMap(new HashMap<>());
    }

    @Benchmark
    public void testConcurrentHashMap() {
        concurrentMap.put("key", "value");
        concurrentMap.get("key");
    }

    @Benchmark
    public void testSynchronizedMap() {
        synchronizedMap.put("key", "value");
        synchronizedMap.get("key");
    }
}
