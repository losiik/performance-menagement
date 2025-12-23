package ru.klosep.performance_management.benchmark;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class MemoryMappedBenchmark {

    private static final int RECORD_SIZE = 128;

    public static BenchmarkResult runWrite(int recordCount) {
        String filename = "mmap-test-" + recordCount + ".dat";
        long fileSize = (long) recordCount * RECORD_SIZE;

        long startTime = System.nanoTime();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        try (RandomAccessFile file = new RandomAccessFile(filename, "rw");
             FileChannel channel = file.getChannel()) {

            MappedByteBuffer buffer = channel.map(
                    FileChannel.MapMode.READ_WRITE, 0, fileSize);

            for (int i = 0; i < recordCount; i++) {
                String record = formatRecord(i);
                buffer.put(record.getBytes(StandardCharsets.UTF_8));

                if (i % 10_000 == 0 && i > 0) {
                    System.out.printf("  [MMap Write] Progress: %,d / %,d\n", i, recordCount);
                }
            }

            buffer.force(); // Синхронизация

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }

        System.gc(); // Подсказка GC

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        return new BenchmarkResult("Memory-Mapped", "WRITE", recordCount,
                duration, memoryAfter - memoryBefore);
    }

    public static BenchmarkResult runSequentialRead(int recordCount) {
        String filename = "mmap-test-" + recordCount + ".dat";
        long fileSize = (long) recordCount * RECORD_SIZE;

        long startTime = System.nanoTime();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        try (RandomAccessFile file = new RandomAccessFile(filename, "r");
             FileChannel channel = file.getChannel()) {

            MappedByteBuffer buffer = channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, fileSize);

            byte[] temp = new byte[RECORD_SIZE];
            for (int i = 0; i < recordCount; i++) {
                buffer.get(temp);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        return new BenchmarkResult("Memory-Mapped", "SEQUENTIAL READ", recordCount,
                duration, memoryAfter - memoryBefore);
    }

    public static BenchmarkResult runRandomRead(int recordCount, int readCount) {
        String filename = "mmap-test-" + recordCount + ".dat";
        long fileSize = (long) recordCount * RECORD_SIZE;

        long startTime = System.nanoTime();

        try (RandomAccessFile file = new RandomAccessFile(filename, "r");
             FileChannel channel = file.getChannel()) {

            MappedByteBuffer buffer = channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, fileSize);

            byte[] temp = new byte[RECORD_SIZE];
            java.util.Random random = new java.util.Random(42);

            for (int i = 0; i < readCount; i++) {
                int position = random.nextInt(recordCount) * RECORD_SIZE;
                buffer.position(position);
                buffer.get(temp);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;

        return new BenchmarkResult("Memory-Mapped", "RANDOM READ", readCount,
                duration, 0);
    }

    private static String formatRecord(int id) {
        String record = String.format("REC:%08d|T:%d|C:%b", id, id, id % 3 == 0);
        while (record.length() < RECORD_SIZE) {
            record += " ";
        }
        return record.substring(0, RECORD_SIZE);
    }
}
