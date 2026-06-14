package com.vlatal.gitracker.repository;

import com.vlatal.gitracker.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserId(String userId);
}
