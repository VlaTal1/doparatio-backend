package com.vlatal.gitracker.serivce;

import com.vlatal.gitracker.bom.TaskLogDTO;
import com.vlatal.gitracker.entity.Task;
import com.vlatal.gitracker.entity.TaskLog;
import com.vlatal.gitracker.exception.NotFoundException;
import com.vlatal.gitracker.exception.PermissionDeniedException;
import com.vlatal.gitracker.repository.TaskLogRepository;
import com.vlatal.gitracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskLogService {

    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final UserService userService;
    private final UserBalanceService userBalanceService;

    public TaskLogDTO logTask(Long taskId, LocalDate logDate) throws Exception {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        String userId = userService.getCurrentUserId();
        if (!task.getUserId().equals(userId)) {
            throw new PermissionDeniedException("You do not have permission to log this task");
        }

        if (!task.isActive()) {
            throw new IllegalArgumentException("Cannot log inactive task");
        }

        if (!task.isRecurring()) {
            // One-time task can only have one completion log total
            if (taskLogRepository.existsByTaskId(taskId)) {
                throw new IllegalArgumentException("One-time task can only be completed once");
            }
        } else {
            // Recurring task can be completed once per logDate
            Optional<TaskLog> existing = taskLogRepository.findByTaskIdAndLogDate(taskId, logDate);
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Task is already completed for " + logDate);
            }
        }

        int minutes = getTaskMinutesEarned(task.getDifficulty());

        TaskLog taskLog = TaskLog.builder()
                .task(task)
                .logDate(logDate)
                .minutesEarned(minutes)
                .build();

        TaskLog saved = taskLogRepository.save(taskLog);

        userBalanceService.addMinutes(userId, minutes);

        return TaskLogDTO.builder()
                .id(saved.getId())
                .logDate(saved.getLogDate())
                .build();
    }

    public void cancelLog(Long taskId, LocalDate logDate) throws Exception {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        String userId = userService.getCurrentUserId();
        if (!task.getUserId().equals(userId)) {
            throw new PermissionDeniedException("You do not have permission to cancel log for this task");
        }

        TaskLog log = taskLogRepository.findByTaskIdAndLogDate(taskId, logDate)
                .orElseThrow(() -> new NotFoundException("Task log not found"));

        userBalanceService.subtractMinutes(userId, log.getMinutesEarned());
        taskLogRepository.delete(log);
    }

    private int getTaskMinutesEarned(com.vlatal.gitracker.domain.Difficulty difficulty) {
        return switch (difficulty) {
            case ROUTINE -> 5;
            case MEDIUM -> 15;
            case HARD -> 30;
        };
    }
}
