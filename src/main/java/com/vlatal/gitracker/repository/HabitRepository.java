package com.vlatal.gitracker.repository;

import com.vlatal.gitracker.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<Habit, Long> {
}
