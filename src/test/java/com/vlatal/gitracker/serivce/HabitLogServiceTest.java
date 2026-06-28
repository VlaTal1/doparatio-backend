package com.vlatal.gitracker.serivce;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.bom.HabitLogDTO;
import com.vlatal.gitracker.domain.LogType;
import com.vlatal.gitracker.entity.HabitLog;
import com.vlatal.gitracker.exception.NotFoundException;
import com.vlatal.gitracker.exception.PermissionDeniedException;
import com.vlatal.gitracker.repository.HabitLogRepository;
import com.vlatal.gitracker.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class HabitLogServiceTest {

    @Autowired
    private HabitLogService habitLogService;

    @Autowired
    private HabitService habitService;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitLogRepository habitLogRepository;

    @MockitoBean
    private UserService userService;

    @Autowired
    private UserBalanceService userBalanceService;

    @Autowired
    private com.vlatal.gitracker.repository.UserBalanceRepository userBalanceRepository;

    private LocalDate testDate;

    @BeforeEach
    public void setUp() throws Exception {
        when(userService.getCurrentUserId()).thenReturn("test-user-id");
        habitLogRepository.deleteAll();
        habitRepository.deleteAll();
        userBalanceRepository.deleteAll();
        testDate = LocalDate.of(2026, 6, 14);
    }

    @Test
    public void logHabit_numeric_success_createNew() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("0000FF")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        HabitLogDTO result = habitLogService.logHabit(savedHabit.getId(), testDate);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getLogDate()).isEqualTo(testDate);
        assertThat(result.getCurrentValue()).isEqualTo(1);
        assertThat(result.getHabit().getId()).isEqualTo(savedHabit.getId());

        // Verify in database
        Optional<HabitLog> savedLog = habitLogRepository.findById(result.getId());
        assertThat(savedLog).isPresent();
        assertThat(savedLog.get().getCurrentValue()).isEqualTo(1);
    }

    @Test
    public void logHabit_numeric_success_incrementExisting() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("0000FF")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        HabitLogDTO result1 = habitLogService.logHabit(savedHabit.getId(), testDate);
        HabitLogDTO result2 = habitLogService.logHabit(savedHabit.getId(), testDate);

        // Verify it was updated and incremented (same ID, value = 2)
        assertThat(result2.getId()).isEqualTo(result1.getId());
        assertThat(result2.getCurrentValue()).isEqualTo(2);

        // Verify total logs in DB is 1
        assertThat(habitLogRepository.count()).isEqualTo(1);
    }

    @Test
    public void logHabit_binary_success_createNew() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("No Sugar")
                .icon("nosugar")
                .color("00FF00")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        HabitLogDTO result = habitLogService.logHabit(savedHabit.getId(), testDate);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getLogDate()).isEqualTo(testDate);
        assertThat(result.getCurrentValue()).isEqualTo(1);

        // Verify in database
        Optional<HabitLog> savedLog = habitLogRepository.findById(result.getId());
        assertThat(savedLog).isPresent();
        assertThat(savedLog.get().getCurrentValue()).isEqualTo(1);
    }

    @Test
    public void logHabit_binary_alreadyLogged_throwsException() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("No Sugar")
                .icon("nosugar")
                .color("00FF00")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        habitLogService.logHabit(savedHabit.getId(), testDate);

        // Attempting to log again on the same day should fail
        assertThatThrownBy(() -> habitLogService.logHabit(savedHabit.getId(), testDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Habit is already logged for");
    }

    @Test
    public void logHabit_nullDate_throwsException() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("No Sugar")
                .icon("nosugar")
                .color("00FF00")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        assertThatThrownBy(() -> habitLogService.logHabit(savedHabit.getId(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Log date cannot be null");
    }

    @Test
    public void logHabit_notFound_throwsException() {
        assertThatThrownBy(() -> habitLogService.logHabit(9999L, testDate))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Habit not found");
    }

    @Test
    public void logHabit_permissionDenied_throwsException() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("0000FF")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        // Switch user context
        when(userService.getCurrentUserId()).thenReturn("other-user-id");

        assertThatThrownBy(() -> habitLogService.logHabit(savedHabit.getId(), testDate))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("You do not have permission to log this habit");
    }

    @Test
    public void cancelLog_binary_success() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("No Sugar")
                .icon("nosugar")
                .color("00FF00")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        HabitLogDTO logged = habitLogService.logHabit(savedHabit.getId(), testDate);
        assertThat(habitLogRepository.findById(logged.getId())).isPresent();

        HabitLogDTO result = habitLogService.cancelLog(savedHabit.getId(), testDate);

        assertThat(result).isNull();
        assertThat(habitLogRepository.findById(logged.getId())).isEmpty();
    }

    @Test
    public void cancelLog_numeric_decrementToPositive() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("0000FF")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        habitLogService.logHabit(savedHabit.getId(), testDate);
        HabitLogDTO logged = habitLogService.logHabit(savedHabit.getId(), testDate);
        assertThat(logged.getCurrentValue()).isEqualTo(2);

        HabitLogDTO result = habitLogService.cancelLog(savedHabit.getId(), testDate);

        assertThat(result).isNotNull();
        assertThat(result.getCurrentValue()).isEqualTo(1);

        Optional<HabitLog> savedLog = habitLogRepository.findById(logged.getId());
        assertThat(savedLog).isPresent();
        assertThat(savedLog.get().getCurrentValue()).isEqualTo(1);
    }

    @Test
    public void cancelLog_numeric_deleteWhenZero() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("0000FF")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        HabitLogDTO logged = habitLogService.logHabit(savedHabit.getId(), testDate);
        assertThat(logged.getCurrentValue()).isEqualTo(1);

        HabitLogDTO result = habitLogService.cancelLog(savedHabit.getId(), testDate);

        assertThat(result).isNull();
        assertThat(habitLogRepository.findById(logged.getId())).isEmpty();
    }

    @Test
    public void cancelLog_notFound_throwsException() {
        assertThatThrownBy(() -> habitLogService.cancelLog(9999L, testDate))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Habit not found");
    }

    @Test
    public void cancelLog_permissionDenied_throwsException() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("0000FF")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);
        habitLogService.logHabit(savedHabit.getId(), testDate);

        // Switch user context
        when(userService.getCurrentUserId()).thenReturn("other-user-id");

        assertThatThrownBy(() -> habitLogService.cancelLog(savedHabit.getId(), testDate))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("You do not have permission to cancel logs for this habit");
    }

    @Test
    public void cancelLog_nullDate_throwsException() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("0000FF")
                .logType(LogType.NUMERIC)
                .targetValue(5)
                .scheduleDays(List.of(1, 2, 3))
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        assertThatThrownBy(() -> habitLogService.cancelLog(savedHabit.getId(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Log date cannot be null");
    }

    @Test
    public void logHabit_awardsMinutes_binary() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Read Book")
                .icon("book")
                .color("00FF00")
                .logType(LogType.BINARY)
                .targetValue(1)
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        habitLogService.logHabit(savedHabit.getId(), testDate);

        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(600); // Base reward is 10 mins = 600s
    }

    @Test
    public void logHabit_awardsMinutes_numeric_onlyOnCompletion() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Pushups")
                .icon("gym")
                .color("FF0000")
                .logType(LogType.NUMERIC)
                .targetValue(3)
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        // Increments:
        // 1st increment (1/3) -> no reward
        habitLogService.logHabit(savedHabit.getId(), testDate);
        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(0);

        // 2nd increment (2/3) -> no reward
        habitLogService.logHabit(savedHabit.getId(), testDate);
        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(0);

        // 3rd increment (3/3) -> completed! Base 10 mins = 600s awarded
        habitLogService.logHabit(savedHabit.getId(), testDate);
        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(600);

        // 4th increment (4/3) -> already completed, no extra reward
        habitLogService.logHabit(savedHabit.getId(), testDate);
        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(600);
    }

    @Test
    public void logHabit_streakBonus() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Meditation")
                .icon("peace")
                .color("FFFF00")
                .logType(LogType.BINARY)
                .targetValue(1)
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        // Day 1 (June 11) -> +10 mins (streak 1) = 600s
        habitLogService.logHabit(savedHabit.getId(), testDate.minusDays(3));
        // Day 2 (June 12) -> +10 mins (streak 2) = 600s
        habitLogService.logHabit(savedHabit.getId(), testDate.minusDays(2));
        // Day 3 (June 13) -> +10 mins (streak 3) = 600s
        habitLogService.logHabit(savedHabit.getId(), testDate.minusDays(1));
        
        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(1800);

        // Day 4 (June 14) -> +12 mins (streak 4: 4-7 days gets +2 mins bonus!) = 720s
        habitLogService.logHabit(savedHabit.getId(), testDate);

        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(2520); // 1800 + 720
    }

    @Test
    public void cancelLog_subtractsMinutes() throws Exception {
        HabitDTO habitDTO = HabitDTO.builder()
                .name("Read Book")
                .icon("book")
                .color("00FF00")
                .logType(LogType.BINARY)
                .targetValue(1)
                .build();
        HabitDTO savedHabit = habitService.create(habitDTO);

        // Complete habit -> user gets 10 mins = 600s
        habitLogService.logHabit(savedHabit.getId(), testDate);
        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(600);

        // Cancel log -> user loses 10 mins = 600s
        habitLogService.cancelLog(savedHabit.getId(), testDate);
        assertThat(userBalanceService.getBalance().getBalance()).isEqualTo(0);
    }
}
