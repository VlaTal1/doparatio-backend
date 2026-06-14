package com.vlatal.gitracker.serivce;

import com.vlatal.gitracker.bom.TaskDTO;
import com.vlatal.gitracker.domain.Difficulty;
import com.vlatal.gitracker.exception.PermissionDeniedException;
import com.vlatal.gitracker.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskLogService taskLogService;

    @Autowired
    private TaskRepository taskRepository;

    @MockitoBean
    private UserService userService;

    @Autowired
    private UserBalanceService userBalanceService;

    @Autowired
    private com.vlatal.gitracker.repository.UserBalanceRepository userBalanceRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() throws Exception {
        when(userService.getCurrentUserId()).thenReturn("test-user-id");
        taskRepository.deleteAll();
        userBalanceRepository.deleteAll();
    }

    @Test
    public void createOneTimeTask_success() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Clean room")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .build();

        TaskDTO result = taskService.create(taskDTO);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Clean room");
        assertThat(result.getDifficulty()).isEqualTo(Difficulty.MEDIUM);
        assertThat(result.getRecurring()).isFalse();
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2026, 6, 14));
        assertThat(result.getScheduleDays()).isNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUserId()).isEqualTo("test-user-id");
    }

    @Test
    public void createOneTimeTask_missingDueDate_throwsException() {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Clean room")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .build();

        assertThatThrownBy(() -> taskService.create(taskDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("One-time task must have a due date");
    }

    @Test
    public void createRecurringTask_success() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Workout")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(1, 3, 5))
                .build();

        TaskDTO result = taskService.create(taskDTO);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Workout");
        assertThat(result.getDifficulty()).isEqualTo(Difficulty.HARD);
        assertThat(result.getRecurring()).isTrue();
        assertThat(result.getDueDate()).isNull();
        assertThat(result.getScheduleDays()).containsExactly(1, 3, 5);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    public void createRecurringTask_invalidScheduleDays_throwsException() {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Workout")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(1, 8))
                .build();

        assertThatThrownBy(() -> taskService.create(taskDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule days must be between 1 and 7");
    }

    @Test
    public void createRecurringTask_missingScheduleDays_throwsException() {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Workout")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .build();

        assertThatThrownBy(() -> taskService.create(taskDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Recurring task must have schedule days");
    }

    @Test
    public void updateTask_success() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Workout")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(1, 3, 5))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        TaskDTO updateDTO = TaskDTO.builder()
                .name("Workout Gym")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(1, 3, 5, 7))
                .active(false)
                .build();

        TaskDTO result = taskService.update(saved.getId(), updateDTO);

        assertThat(result.getName()).isEqualTo("Workout Gym");
        assertThat(result.getScheduleDays()).containsExactly(1, 3, 5, 7);
        assertThat(result.isActive()).isFalse();
    }

    @Test
    public void updateTask_changeRecurringWithLogs_throwsException() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Clean room")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 14));

        TaskDTO updateDTO = TaskDTO.builder()
                .name("Clean room")
                .difficulty(Difficulty.MEDIUM)
                .recurring(true)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        assertThatThrownBy(() -> taskService.update(saved.getId(), updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot change recurring type of a task that has logged completions");
    }

    @Test
    public void getById_success() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Workout")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(1, 3, 5))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 14));

        entityManager.flush();
        entityManager.clear();

        TaskDTO result = taskService.getById(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getLogs()).hasSize(1);
        assertThat(result.getLogs().get(0).getLogDate()).isEqualTo(LocalDate.of(2026, 6, 14));
    }

    @Test
    public void getById_permissionDenied_throwsException() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Workout")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(1, 3, 5))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        when(userService.getCurrentUserId()).thenReturn("other-user-id");

        assertThatThrownBy(() -> taskService.getById(saved.getId()))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("You do not have permission to view this task");
    }

    @Test
    public void getTasksForDate_success() throws Exception {
        // One-time task for June 14, 2026 (Sunday)
        TaskDTO task1 = TaskDTO.builder()
                .name("Clean room")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .build();
        taskService.create(task1);

        // One-time task for another day
        TaskDTO task2 = TaskDTO.builder()
                .name("Clean kitchen")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 15))
                .build();
        taskService.create(task2);

        // Recurring task on Sunday (day 7)
        TaskDTO task3 = TaskDTO.builder()
                .name("Weekly review")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(7))
                .build();
        taskService.create(task3);

        // Recurring task on Mon/Wed/Fri (day 1, 3, 5)
        TaskDTO task4 = TaskDTO.builder()
                .name("Gym")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(1, 3, 5))
                .build();
        taskService.create(task4);

        List<TaskDTO> result = taskService.getAllForDate(LocalDate.of(2026, 6, 14));

        // Should return Clean room (due June 14) and Weekly review (Sunday is day 7)
        assertThat(result).hasSize(2);
        assertThat(result).extracting(TaskDTO::getName).containsExactlyInAnyOrder("Clean room", "Weekly review");
    }

    @Test
    public void logTask_oneTimeMultipleCompletions_throwsException() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Clean room")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 14));

        assertThatThrownBy(() -> taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 15)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("One-time task can only be completed once");
    }

    @Test
    public void logTask_recurringDuplicateDay_throwsException() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Workout")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(1, 3, 5))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 14));

        assertThatThrownBy(() -> taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 14)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task is already completed for 2026-06-14");
    }

    @Test
    public void cancelLog_success() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Workout")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(1, 3, 5))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 14));

        taskLogService.cancelLog(saved.getId(), LocalDate.of(2026, 6, 14));

        entityManager.flush();
        entityManager.clear();

        TaskDTO result = taskService.getById(saved.getId());
        assertThat(result.getLogs()).isEmpty();
    }

    @Test
    public void logTask_awardsMinutes_routine() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Tidy up desk")
                .difficulty(Difficulty.ROUTINE)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 14));

        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(5); // Routine is 5 mins
    }

    @Test
    public void logTask_awardsMinutes_medium() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Clean bathroom")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 14));

        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(15); // Medium is 15 mins
    }

    @Test
    public void logTask_awardsMinutes_hard() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Leetcode match")
                .difficulty(Difficulty.HARD)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 14));

        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(30); // Hard is 30 mins
    }

    @Test
    public void cancelLog_subtractsMinutes() throws Exception {
        TaskDTO taskDTO = TaskDTO.builder()
                .name("Leetcode match")
                .difficulty(Difficulty.HARD)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .build();
        TaskDTO saved = taskService.create(taskDTO);

        taskLogService.logTask(saved.getId(), LocalDate.of(2026, 6, 14));
        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(30);

        taskLogService.cancelLog(saved.getId(), LocalDate.of(2026, 6, 14));
        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(0);
    }
}
