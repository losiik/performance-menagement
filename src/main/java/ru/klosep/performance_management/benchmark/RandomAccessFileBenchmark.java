package ru.klosep.performance_management.benchmark;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class RandomAccessFileBenchmark {

    private static final int RECORD_SIZE = 256; // Фиксированный размер записи

    public static BenchmarkResult runWrite(int recordCount) {
        String filename = "raf-test-" + recordCount + ".dat";
        long startTime = System.nanoTime();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")) {
            for (int i = 0; i < recordCount; i++) {
                String record = formatRecord(i);
                raf.write(record.getBytes(StandardCharsets.UTF_8));

                if (i % 10_000 == 0 && i > 0) {
                    System.out.printf("  [RAF Write] Progress: %,d / %,d\n", i, recordCount);
                }
            }
            raf.getFD().sync(); // Форсируем запись на диск
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000; // ms
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;

        return new BenchmarkResult("RandomAccessFile", "WRITE", recordCount,
                duration, memoryUsed);
    }

    public static BenchmarkResult runSequentialRead(int recordCount) {
        String filename = "raf-test-" + recordCount + ".dat";
        long startTime = System.nanoTime();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        int readCount = 0;
        try (RandomAccessFile raf = new RandomAccessFile(filename, "r")) {
            byte[] buffer = new byte[RECORD_SIZE];

            while (raf.read(buffer) != -1) {
                readCount++;
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        return new BenchmarkResult("RandomAccessFile", "SEQUENTIAL READ", readCount,
                duration, memoryAfter - memoryBefore);
    }

    public static BenchmarkResult runRandomRead(int recordCount, int readCount) {
        String filename = "raf-test-" + recordCount + ".dat";
        long startTime = System.nanoTime();

        try (RandomAccessFile raf = new RandomAccessFile(filename, "r")) {
            byte[] buffer = new byte[RECORD_SIZE];
            java.util.Random random = new java.util.Random(42);

            for (int i = 0; i < readCount; i++) {
                long position = random.nextInt(recordCount) * RECORD_SIZE;
                raf.seek(position); // Произвольный доступ!
                raf.read(buffer);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;

        return new BenchmarkResult("RandomAccessFile", "RANDOM READ", readCount,
                duration, 0);
    }

    private static String formatRecord(int id) {
        String record = String.format("ID:%010d|TITLE:Task_%d|DESC:Description_for_task_%d|COMPLETED:%b",
                id, id, id, id % 3 == 0);

        // Дополняем до фиксированного размера
        while (record.length() < RECORD_SIZE - 1) {
            record += " ";
        }
        return record + "\n";
    }
}