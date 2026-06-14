package com.vlatal.gitracker.repository;

import com.vlatal.gitracker.entity.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {
    Optional<TaskLog> findByTaskIdAndLogDate(Long taskId, LocalDate logDate);
    boolean existsByTaskId(Long taskId);
}
