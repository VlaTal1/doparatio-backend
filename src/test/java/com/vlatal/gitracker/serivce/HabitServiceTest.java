package com.vlatal.gitracker.serivce;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.domain.LogType;
import com.vlatal.gitracker.entity.Habit;
import com.vlatal.gitracker.exception.NotFoundException;
import com.vlatal.gitracker.exception.PermissionDeniedException;
import com.vlatal.gitracker.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class HabitServiceTest {

    @Autowired
    private HabitService habitService;

    @Autowired
    private HabitRepository habitRepository;

    @MockitoBean
    private UserService userService;

    @Autowired
    private HabitLogService habitLogService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() throws Exception {
        when(userService.getCurrentUserId()).thenReturn("test-user-id");
        habitRepository.deleteAll();
    }

    @Test
    public void createHabit_success() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Read Books")
                .icon("book")
                .color("FF0000")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 3, 5))
                .build();

        HabitDTO result = habitService.create(habitDTO);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Read Books");
        assertThat(result.getIcon()).isEqualTo("book");
        assertThat(result.getColor()).isEqualTo("FF0000");
        assertThat(result.getLogType()).isEqualTo(LogType.NUMERIC);
        assertThat(result.getTargetValue()).isEqualTo(5);
        assertThat(result.getScheduleDays()).containsExactly(1, 3, 5);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUserId()).isEqualTo("test-user-id");

        // Verify it was actually saved in the test database
        Optional<Habit> savedHabit = habitRepository.findById(result.getId());
        assertThat(savedHabit).isPresent();
        assertThat(savedHabit.get().getName()).isEqualTo("Read Books");
    }

    @Test
    public void createHabit_binaryHabitDefaultTargetValue() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("No Sugar")
                .icon("nosugar")
                .color("00FF00")
                .logType(LogType.BINARY)
                .targetValue(0) // targetValue 0 should be set to 1
                .scheduleDays(List.of(2, 4, 6))
                .build();

        HabitDTO result = habitService.create(habitDTO);

        assertThat(result.getTargetValue()).isEqualTo(1);
    }

    @Test
    public void createHabit_binaryHabitTargetValueGreaterThanOne_throwsException() {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("No Sugar")
                .icon("nosugar")
                .color("00FF00")
                .logType(LogType.BINARY)
                .targetValue(2) // Binary habit cannot have target value > 1
                .build();

        assertThatThrownBy(() -> habitService.create(habitDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target value of binary habit can not be more then 1");
    }

    @Test
    public void createHabit_invalidScheduleDays_throwsException() {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Exercise")
                .icon("gym")
                .color("0000FF")
                .logType(LogType.NUMERIC)
                .targetValue(1)
                .scheduleDays(List.of(0, 8)) // Invalid days
                .build();

        assertThatThrownBy(() -> habitService.create(habitDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule days must be between 1 and 7");
    }

    @Test
    public void createHabit_nullScheduleDays_success() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Meditate")
                .icon("mind")
                .color("00FFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(null) // null schedule days is valid
                .build();

        HabitDTO result = habitService.create(habitDTO);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getScheduleDays()).isNull();
    }

    @Test
    public void createHabit_nullTargetValue_defaultsToOne() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Meditate")
                .icon("mind")
                .color("00FFFF")
                .logType(LogType.BINARY)
                .targetValue(null) // null targetValue should default to 1
                .scheduleDays(List.of(1))
                .build();

        HabitDTO result = habitService.create(habitDTO);

        assertThat(result.getTargetValue()).isEqualTo(1);
    }

    @Test
    public void createHabit_scheduleDaysBoundaries_success() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Jogging")
                .icon("run")
                .color("FF00FF")
                .logType(LogType.NUMERIC)
                .targetValue(2)
                .scheduleDays(List.of(1, 7)) // Boundary days (Monday=1, Sunday=7)
                .build();

        HabitDTO result = habitService.create(habitDTO);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getScheduleDays()).containsExactly(1, 7);
    }

    @Test
    public void createHabit_userServiceThrowsException_propagatesException() throws Exception {
        when(userService.getCurrentUserId()).thenThrow(new Exception("Unable to get userId"));

        HabitDTO habitDTO = HabitDTO.builder()
                .name("Jogging")
                .icon("run")
                .color("FF00FF")
                .logType(LogType.NUMERIC)
                .targetValue(2)
                .scheduleDays(List.of(1, 2))
                .build();

        assertThatThrownBy(() -> habitService.create(habitDTO))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Unable to get userId");
    }

    @Test
    public void deleteHabit_success() throws Exception {
        // Create habit
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Quit Smoking")
                .icon("smoke")
                .color("aaaaaa")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO saved = habitService.create(habitDTO);
        assertThat(saved.getId()).isNotNull();

        // Delete habit
        habitService.delete(saved.getId());

        // Verify deleted
        Optional<Habit> found = habitRepository.findById(saved.getId());
        assertThat(found).isNotPresent();
    }

    @Test
    public void deleteHabit_notFound_throwsException() {
        assertThatThrownBy(() -> habitService.delete(9999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Habit not found");
    }

    @Test
    public void deleteHabit_permissionDenied_throwsException() throws Exception {
        // Create habit owned by test-user-id
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Quit Smoking")
                .icon("smoke")
                .color("aaaaaa")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO saved = habitService.create(habitDTO);

        // Switch user context to someone else
        when(userService.getCurrentUserId()).thenReturn("other-user-id");

        // Try deleting and expect access denied
        assertThatThrownBy(() -> habitService.delete(saved.getId()))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("You do not have permission to delete this habit");

        // Verify the habit is still there in the database
        Optional<Habit> found = habitRepository.findById(saved.getId());
        assertThat(found).isPresent();
    }

    @Test
    public void updateHabit_success() throws Exception {
        // Create habit
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Quit Smoking")
                .icon("smoke")
                .color("aaaaaa")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO saved = habitService.create(habitDTO);

        // Update habit
        HabitDTO updateDTO = HabitDTO.builder()
                .name("Quit Smoking Completely")
                .icon("nosmoke")
                .color("bbbbbb")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3, 4))
                .active(false)
                .build();

        HabitDTO result = habitService.update(saved.getId(), updateDTO);

        // Verify result
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getName()).isEqualTo("Quit Smoking Completely");
        assertThat(result.getIcon()).isEqualTo("nosmoke");
        assertThat(result.getColor()).isEqualTo("bbbbbb");
        assertThat(result.getScheduleDays()).containsExactly(1, 2, 3, 4);
        assertThat(result.isActive()).isFalse();

        // Verify in DB
        Optional<Habit> found = habitRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Quit Smoking Completely");
        assertThat(found.get().isActive()).isFalse();
    }

    @Test
    public void updateHabit_notFound_throwsException() {
        HabitDTO updateDTO = HabitDTO.builder()
                .name("Quit Smoking Completely")
                .icon("nosmoke")
                .color("bbbbbb")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3, 4))
                .active(false)
                .build();

        assertThatThrownBy(() -> habitService.update(9999L, updateDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Habit not found");
    }

    @Test
    public void updateHabit_permissionDenied_throwsException() throws Exception {
        // Create habit owned by test-user-id
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Quit Smoking")
                .icon("smoke")
                .color("aaaaaa")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO saved = habitService.create(habitDTO);

        // Switch user context to someone else
        when(userService.getCurrentUserId()).thenReturn("other-user-id");

        HabitDTO updateDTO = HabitDTO.builder()
                .name("Quit Smoking Completely")
                .icon("nosmoke")
                .color("bbbbbb")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3, 4))
                .active(false)
                .build();

        // Try updating and expect access denied
        assertThatThrownBy(() -> habitService.update(saved.getId(), updateDTO))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("You do not have permission to update this habit");

        // Verify database remains unchanged
        Optional<Habit> found = habitRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Quit Smoking");
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    public void updateHabit_changeLogTypeWithLogs_throwsException() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("0000FF")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO saved = habitService.create(habitDTO);

        habitLogService.logHabit(saved.getId(), java.time.LocalDate.of(2026, 6, 14));

        HabitDTO updateDTO = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("0000FF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        assertThatThrownBy(() -> habitService.update(saved.getId(), updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot change log type of a habit that has logged entries");
    }


    @Test
    public void getAll_success() throws Exception {
        // Create two habits
        HabitDTO habit1 = HabitDTO.builder()
                .name("Read Books")
                .icon("book")
                .color("FF0000")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 3, 5))
                .build();
        HabitDTO saved1 = habitService.create(habit1);

        HabitDTO habit2 = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("0000FF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO saved2 = habitService.create(habit2);

        // Add logs for different days
        habitLogService.logHabit(saved1.getId(), java.time.LocalDate.of(2026, 6, 13));
        habitLogService.logHabit(saved1.getId(), java.time.LocalDate.of(2026, 6, 14));
        habitLogService.logHabit(saved2.getId(), java.time.LocalDate.of(2026, 6, 14));

        entityManager.flush();
        entityManager.clear();

        // Get all habits
        List<HabitDTO> result = habitService.getAll();

        assertThat(result).hasSize(2);

        HabitDTO dto1 = result.stream().filter(h -> h.getId().equals(saved1.getId())).findFirst().orElseThrow();
        assertThat(dto1.getLogs()).hasSize(2);

        HabitDTO dto2 = result.stream().filter(h -> h.getId().equals(saved2.getId())).findFirst().orElseThrow();
        assertThat(dto2.getLogs()).hasSize(1);
        assertThat(dto2.getLogs().get(0).getLogDate()).isEqualTo(java.time.LocalDate.of(2026, 6, 14));
    }

    @Test
    public void getById_success() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Read Books")
                .icon("book")
                .color("FF0000")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 3, 5))
                .build();
        HabitDTO saved = habitService.create(habitDTO);

        habitLogService.logHabit(saved.getId(), java.time.LocalDate.of(2026, 6, 14));

        entityManager.flush();
        entityManager.clear();

        HabitDTO result = habitService.getById(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getName()).isEqualTo("Read Books");
        assertThat(result.getLogs()).hasSize(1);
        assertThat(result.getLogs().get(0).getLogDate()).isEqualTo(java.time.LocalDate.of(2026, 6, 14));
    }

    @Test
    public void getById_notFound_throwsException() {
        assertThatThrownBy(() -> habitService.getById(9999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Habit not found");
    }

    @Test
    public void getById_permissionDenied_throwsException() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Read Books")
                .icon("book")
                .color("FF0000")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 3, 5))
                .build();
        HabitDTO saved = habitService.create(habitDTO);

        // Switch user context
        when(userService.getCurrentUserId()).thenReturn("other-user-id");

        assertThatThrownBy(() -> habitService.getById(saved.getId()))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("You do not have permission to view this habit");
    }
}
