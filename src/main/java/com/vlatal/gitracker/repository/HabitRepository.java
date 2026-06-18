package com.vlatal.gitracker.repository;

import com.vlatal.gitracker.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findAllByUserIdOrderByCreatedAtAsc(String userId);
}
