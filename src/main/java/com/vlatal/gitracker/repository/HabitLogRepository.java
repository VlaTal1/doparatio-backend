package com.vlatal.gitracker.repository;

import com.vlatal.gitracker.entity.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    Optional<HabitLog> findByHabitIdAndLogDate(Long habitId, LocalDate logDate);
}
