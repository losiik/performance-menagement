package ru.klosep.performance_management.benchmark;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class FileChannelBenchmark {

    private static final int BUFFER_SIZE = 8192 * 4; // 32 KB buffer

    public static BenchmarkResult runWrite(int recordCount) {
        String filename = "fc-test-" + recordCount + ".dat";
        long startTime = System.nanoTime();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        try (RandomAccessFile file = new RandomAccessFile(filename, "rw");
             FileChannel channel = file.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            for (int i = 0; i < recordCount; i++) {
                String record = formatRecord(i);
                byte[] bytes = record.getBytes(StandardCharsets.UTF_8);

                if (buffer.remaining() < bytes.length) {
                    buffer.flip();
                    channel.write(buffer);
                    buffer.clear();
                }

                buffer.put(bytes);

                if (i % 10_000 == 0 && i > 0) {
                    System.out.printf("  [FileChannel Write] Progress: %,d / %,d\n", i, recordCount);
                }
            }

            // Записываем остаток
            if (buffer.position() > 0) {
                buffer.flip();
                channel.write(buffer);
            }

            channel.force(true); // Синхронизация с диском

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        return new BenchmarkResult("FileChannel", "WRITE", recordCount,
                duration, memoryAfter - memoryBefore);
    }

    public static BenchmarkResult runSequentialRead(int recordCount) {
        String filename = "fc-test-" + recordCount + ".dat";
        long startTime = System.nanoTime();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        int bytesRead = 0;
        try (RandomAccessFile file = new RandomAccessFile(filename, "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (channel.read(buffer) != -1) {
                bytesRead += buffer.position();
                buffer.clear();
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        return new BenchmarkResult("FileChannel", "SEQUENTIAL READ", bytesRead,
                duration, memoryAfter - memoryBefore);
    }

    private static String formatRecord(int id) {
        return String.format("ID:%010d|TASK_%d|DESC_%d|%b\n",
                id, id, id, id % 3 == 0);
    }
}
