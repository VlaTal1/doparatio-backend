package com.vlatal.gitracker.converter;

import com.vlatal.gitracker.bom.TaskDTO;
import com.vlatal.gitracker.bom.TaskLogDTO;
import com.vlatal.gitracker.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskConverter {

    public Task fromDTO(TaskDTO entry) {
        if (entry == null) {
            return null;
        }
        return Task.builder()
                .id(entry.getId())
                .name(entry.getName())
                .difficulty(entry.getDifficulty())
                .recurring(entry.getRecurring())
                .dueDate(entry.getDueDate())
                .scheduleDays(entry.getScheduleDays())
                .active(entry.isActive())
                .userId(entry.getUserId())
                .build();
    }

    public TaskDTO toDTO(Task entry) {
        if (entry == null) {
            return null;
        }
        return TaskDTO.builder()
                .id(entry.getId())
                .name(entry.getName())
                .difficulty(entry.getDifficulty())
                .recurring(entry.isRecurring())
                .dueDate(entry.getDueDate())
                .scheduleDays(entry.getScheduleDays())
                .active(entry.isActive())
                .userId(entry.getUserId())
                .logs(entry.getLogs() != null ? entry.getLogs().stream().map(log -> {
                    TaskLogDTO dto = TaskLogDTO.builder()
                            .id(log.getId())
                            .logDate(log.getLogDate())
                            .build();
                    return dto;
                }).toList() : null)
                .build();
    }
}
