package com.vlatal.gitracker.serivce;

import com.vlatal.gitracker.bom.TaskDTO;
import com.vlatal.gitracker.converter.TaskConverter;
import com.vlatal.gitracker.entity.Task;
import com.vlatal.gitracker.exception.NotFoundException;
import com.vlatal.gitracker.exception.PermissionDeniedException;
import com.vlatal.gitracker.repository.TaskLogRepository;
import com.vlatal.gitracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskConverter taskConverter;
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;

    public List<TaskDTO> getAll() throws Exception {
        String currentUserId = userService.getCurrentUserId();
        return taskRepository.findAllByUserId(currentUserId).stream()
                .map(taskConverter::toDTO)
                .toList();
    }

    public List<TaskDTO> getAllForDate(LocalDate date) throws Exception {
        String currentUserId = userService.getCurrentUserId();
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1 (Monday) to 7 (Sunday)

        return taskRepository.findAllByUserId(currentUserId).stream()
                .filter(Task::isActive)
                .filter(task -> {
                    if (task.isRecurring()) {
                        return task.getScheduleDays() != null && task.getScheduleDays().contains(dayOfWeek);
                    } else {
                        return date.equals(task.getDueDate());
                    }
                })
                .map(taskConverter::toDTO)
                .toList();
    }

    public TaskDTO getById(Long id) throws Exception {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!task.getUserId().equals(userService.getCurrentUserId())) {
            throw new PermissionDeniedException("You do not have permission to view this task");
        }

        return taskConverter.toDTO(task);
    }

    public TaskDTO create(TaskDTO taskDTO) throws Exception {
        validate(taskDTO);

        taskDTO.setActive(true);
        taskDTO.setUserId(userService.getCurrentUserId());

        Task saved = taskRepository.save(taskConverter.fromDTO(taskDTO));
        return taskConverter.toDTO(saved);
    }

    public void delete(Long id) throws Exception {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!task.getUserId().equals(userService.getCurrentUserId())) {
            throw new PermissionDeniedException("You do not have permission to delete this task");
        }

        taskRepository.delete(task);
    }

    public TaskDTO update(Long id, TaskDTO taskDTO) throws Exception {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!task.getUserId().equals(userService.getCurrentUserId())) {
            throw new PermissionDeniedException("You do not have permission to update this task");
        }

        if (task.isRecurring() != taskDTO.getRecurring() && taskLogRepository.existsByTaskId(id)) {
            throw new IllegalArgumentException("Cannot change recurring type of a task that has logged completions");
        }

        validate(taskDTO);

        task.setName(taskDTO.getName());
        task.setDifficulty(taskDTO.getDifficulty());
        task.setRecurring(taskDTO.getRecurring());
        task.setDueDate(taskDTO.getDueDate());
        task.setScheduleDays(taskDTO.getScheduleDays());
        task.setActive(taskDTO.isActive());

        Task saved = taskRepository.save(task);
        return taskConverter.toDTO(saved);
    }

    private void validate(TaskDTO taskDTO) {
        if (Boolean.TRUE.equals(taskDTO.getRecurring())) {
            taskDTO.setDueDate(null);
            if (CollectionUtils.isEmpty(taskDTO.getScheduleDays())) {
                throw new IllegalArgumentException("Recurring task must have schedule days");
            }
            if (taskDTO.getScheduleDays().stream().anyMatch(d -> d < 1 || d > 7)) {
                throw new IllegalArgumentException("Schedule days must be between 1 and 7");
            }
        } else {
            taskDTO.setScheduleDays(null);
            if (taskDTO.getDueDate() == null) {
                throw new IllegalArgumentException("One-time task must have a due date");
            }
        }
    }
}
