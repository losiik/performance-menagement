package ru.klosep.performance_management.benchmark;

import ru.klosep.performance_management.model.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public class DataGenerator {

    private static final String[] TASK_PREFIXES = {
            "Implement", "Fix", "Refactor", "Design", "Test",
            "Deploy", "Optimize", "Review", "Document", "Update"
    };

    private static final String[] TASK_SUBJECTS = {
            "authentication", "database", "API endpoint", "user interface",
            "caching layer", "monitoring", "logging", "security",
            "performance", "integration", "migration", "backup"
    };

    private static final Random RANDOM = new Random(42); // Фиксированный seed для воспроизводимости

    public static void main(String[] args) {
        System.out.println("=== Test Data Generator ===\n");

        // Генерируем разные размеры датасетов
        generateDataset(10_000, "test-data-10k.txt");
        generateDataset(50_000, "test-data-50k.txt");
        generateDataset(100_000, "test-data-100k.txt");

        System.out.println("\n✅ All datasets generated successfully!");
    }

    public static void generateDataset(int count, String filename) {
        System.out.printf("Generating %,d records to %s...\n", count, filename);
        long start = System.currentTimeMillis();

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filename), 8192 * 4)) {

            // Заголовок CSV
            writer.write("id,title,description,completed,createdAt\n");

            for (int i = 1; i <= count; i++) {
                Task task = generateRandomTask((long) i);
                String line = String.format("%d,%s,%s,%b,%s\n",
                        task.getId(),
                        escapeCSV(task.getTitle()),
                        escapeCSV(task.getDescription()),
                        task.isCompleted(),
                        LocalDateTime.now().minusDays(RANDOM.nextInt(365))
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
                writer.write(line);

                if (i % 10_000 == 0) {
                    System.out.printf("  Progress: %,d / %,d (%.1f%%)\n",
                            i, count, (i * 100.0) / count);
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        long duration = System.currentTimeMillis() - start;
        File file = new File(filename);

        System.out.printf("  ✓ Generated in %,d ms\n", duration);
        System.out.printf("  ✓ File size: %.2f MB\n", file.length() / (1024.0 * 1024.0));
        System.out.printf("  ✓ Average record size: %d bytes\n\n", file.length() / count);
    }

    private static Task generateRandomTask(Long id) {
        String title = TASK_PREFIXES[RANDOM.nextInt(TASK_PREFIXES.length)]
                + " "
                + TASK_SUBJECTS[RANDOM.nextInt(TASK_SUBJECTS.length)];

        String description = "Task description for " + title
                + ". Priority: " + (RANDOM.nextInt(5) + 1)
                + ". Estimated hours: " + (RANDOM.nextInt(40) + 1)
                + ". UUID: " + UUID.randomUUID().toString();

        boolean completed = RANDOM.nextDouble() < 0.33; // 33% выполнено

        return new Task(id, title, description, completed);
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Генерация бинарных данных для FileChannel бенчмарка
    public static void generateBinaryDataset(int count, String filename) {
        System.out.printf("Generating binary dataset: %,d records to %s...\n", count, filename);
        long start = System.currentTimeMillis();

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(filename), 8192 * 4))) {

            // Заголовок: количество записей
            dos.writeInt(count);

            for (int i = 1; i <= count; i++) {
                Task task = generateRandomTask((long) i);

                // Бинарный формат: id(8) + titleLen(4) + title + descLen(4) + desc + completed(1)
                dos.writeLong(task.getId());

                byte[] titleBytes = task.getTitle().getBytes(StandardCharsets.UTF_8);
                dos.writeInt(titleBytes.length);
                dos.write(titleBytes);

                byte[] descBytes = task.getDescription().getBytes(StandardCharsets.UTF_8);
                dos.writeInt(descBytes.length);
                dos.write(descBytes);

                dos.writeBoolean(task.isCompleted());

                if (i % 10_000 == 0) {
                    System.out.printf("  Progress: %,d / %,d\n", i, count);
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        long duration = System.currentTimeMillis() - start;
        File file = new File(filename);
        System.out.printf("  ✓ Generated in %,d ms (%.2f MB)\n\n",
                duration, file.length() / (1024.0 * 1024.0));
    }
}