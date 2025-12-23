package ru.klosep.performance_management.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.klosep.performance_management.mongo.document.TaskDocument;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskMongoRepository extends MongoRepository<TaskDocument, String> {

    List<TaskDocument> findByCompleted(Boolean completed);

    List<TaskDocument> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("{ 'completed': ?0, 'createdAt': { $gte: ?1 } }")
    List<TaskDocument> findCompletedTasksAfterDate(Boolean completed, LocalDateTime start);
}