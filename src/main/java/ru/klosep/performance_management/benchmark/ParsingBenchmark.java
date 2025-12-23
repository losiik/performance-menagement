package ru.klosep.performance_management.benchmark;

import org.openjdk.jmh.annotations.*;
import ru.klosep.performance_management.model.Task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ParsingBenchmark {

    private static final String TEST_FILE = "test-data-10k.txt";

    @Benchmark
    public List<Task> parseWithForLoop() throws Exception {
        List<Task> tasks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE))) {
            String line = reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    tasks.add(new Task(
                            Long.parseLong(parts[0].trim()),
                            parts[1].trim(),
                            parts[2].trim(),
                            Boolean.parseBoolean(parts[3].trim())
                    ));
                }
            }
        }

        return tasks;
    }

    @Benchmark
    public List<Task> parseWithStream() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE))) {
            return reader.lines()
                    .skip(1) // Skip header
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length >= 4)
                    .map(parts -> new Task(
                            Long.parseLong(parts[0].trim()),
                            parts[1].trim(),
                            parts[2].trim(),
                            Boolean.parseBoolean(parts[3].trim())
                    ))
                    .toList();
        }
    }

    @Benchmark
    public List<Task> parseWithParallelStream() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE))) {
            return reader.lines()
                    .skip(1)
                    .parallel()
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length >= 4)
                    .map(parts -> new Task(
                            Long.parseLong(parts[0].trim()),
                            parts[1].trim(),
                            parts[2].trim(),
                            Boolean.parseBoolean(parts[3].trim())
                    ))
                    .toList();
        }
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}