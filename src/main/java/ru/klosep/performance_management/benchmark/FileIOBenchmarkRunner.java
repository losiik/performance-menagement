package ru.klosep.performance_management.benchmark;

import java.util.ArrayList;
import java.util.List;

public class FileIOBenchmarkRunner {

    public static void main(String[] args) {
        System.out.println("=".repeat(120));
        System.out.println("FILE I/O BENCHMARK: RandomAccessFile vs FileChannel vs Memory-Mapped");
        System.out.println("=".repeat(120));
        System.out.println();

        int[] testSizes = {10_000, 50_000, 100_000};
        List<BenchmarkResult> allResults = new ArrayList<>();

        for (int size : testSizes) {
            System.out.printf("\n### TESTING WITH %,d RECORDS ###\n\n", size);

            // Прогрев JVM
            System.out.println("Warming up JVM...");
            System.gc();
            try { Thread.sleep(1000); } catch (InterruptedException e) {}

            // RandomAccessFile
            System.out.println("\n[1/3] RandomAccessFile");
            BenchmarkResult rafWrite = RandomAccessFileBenchmark.runWrite(size);
            if (rafWrite != null) {
                rafWrite.print();
                allResults.add(rafWrite);
            }

            BenchmarkResult rafSeqRead = RandomAccessFileBenchmark.runSequentialRead(size);
            if (rafSeqRead != null) {
                rafSeqRead.print();
                allResults.add(rafSeqRead);
            }

            BenchmarkResult rafRandRead = RandomAccessFileBenchmark.runRandomRead(size, 1000);
            if (rafRandRead != null) {
                rafRandRead.print();
                allResults.add(rafRandRead);
            }

            System.gc();
            try { Thread.sleep(500); } catch (InterruptedException e) {}

            // FileChannel
            System.out.println("\n[2/3] FileChannel + ByteBuffer");
            BenchmarkResult fcWrite = FileChannelBenchmark.runWrite(size);
            if (fcWrite != null) {
                fcWrite.print();
                allResults.add(fcWrite);
            }

            BenchmarkResult fcRead = FileChannelBenchmark.runSequentialRead(size);
            if (fcRead != null) {
                fcRead.print();
                allResults.add(fcRead);
            }

            System.gc();
            try { Thread.sleep(500); } catch (InterruptedException e) {}

            // Memory-Mapped
            System.out.println("\n[3/3] Memory-Mapped File");
            BenchmarkResult mmapWrite = MemoryMappedBenchmark.runWrite(size);
            if (mmapWrite != null) {
                mmapWrite.print();
                allResults.add(mmapWrite);
            }

            BenchmarkResult mmapSeqRead = MemoryMappedBenchmark.runSequentialRead(size);
            if (mmapSeqRead != null) {
                mmapSeqRead.print();
                allResults.add(mmapSeqRead);
            }

            BenchmarkResult mmapRandRead = MemoryMappedBenchmark.runRandomRead(size, 1000);
            if (mmapRandRead != null) {
                mmapRandRead.print();
                allResults.add(mmapRandRead);
            }

            System.out.println("\n" + "-".repeat(120));
        }

        // Итоговая сводка
        printSummary(allResults);
    }

    private static void printSummary(List<BenchmarkResult> results) {
        System.out.println("\n" + "=".repeat(120));
        System.out.println("SUMMARY");
        System.out.println("=".repeat(120));

        System.out.println("\nBest Write Performance:");
        results.stream()
                .filter(r -> r.getOperation().equals("WRITE"))
                .min((a, b) -> Long.compare(a.getDurationMs(), b.getDurationMs()))
                .ifPresent(r -> System.out.printf("  %s: %,d ms\n", r.getMethod(), r.getDurationMs()));

        System.out.println("\nBest Sequential Read Performance:");
        results.stream()
                .filter(r -> r.getOperation().contains("SEQUENTIAL"))
                .min((a, b) -> Long.compare(a.getDurationMs(), b.getDurationMs()))
                .ifPresent(r -> System.out.printf("  %s: %,d ms\n", r.getMethod(), r.getDurationMs()));

        System.out.println("\nLowest Memory Usage (Write):");
        results.stream()
                .filter(r -> r.getOperation().equals("WRITE"))
                .min((a, b) -> Long.compare(a.getMemoryUsedBytes(), b.getMemoryUsedBytes()))
                .ifPresent(r -> System.out.printf("  %s: %.2f MB\n",
                        r.getMethod(), r.getMemoryUsedBytes() / (1024.0 * 1024.0)));

        System.out.println("\n" + "=".repeat(120));
    }
}
