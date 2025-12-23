package ru.klosep.performance_management.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.klosep.performance_management.postgres.entity.TaskEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskJpaRepository extends JpaRepository<TaskEntity, Long> {

    List<TaskEntity> findByCompleted(Boolean completed);

    List<TaskEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM TaskEntity t WHERE t.completed = :completed " +
            "AND t.createdAt >= :start ORDER BY t.createdAt DESC")
    List<TaskEntity> findCompletedTasksAfterDate(Boolean completed, LocalDateTime start);
}
