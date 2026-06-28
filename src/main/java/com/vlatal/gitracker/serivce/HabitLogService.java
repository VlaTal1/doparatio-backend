package com.vlatal.gitracker.serivce;

import com.vlatal.gitracker.bom.HabitLogDTO;
import com.vlatal.gitracker.converter.HabitLogConverter;
import com.vlatal.gitracker.domain.LogType;
import com.vlatal.gitracker.entity.Habit;
import com.vlatal.gitracker.entity.HabitLog;
import com.vlatal.gitracker.exception.NotFoundException;
import com.vlatal.gitracker.exception.PermissionDeniedException;
import com.vlatal.gitracker.repository.HabitLogRepository;
import com.vlatal.gitracker.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HabitLogService {

    private final HabitRepository habitRepository;

    private final HabitLogRepository habitLogRepository;

    private final UserService userService;

    private final HabitLogConverter habitLogConverter;

    private final UserBalanceService userBalanceService;

    @Transactional
    public HabitLogDTO logHabit(Long habitId, LocalDate logDate) throws Exception {
        if (logDate == null) {
            throw new IllegalArgumentException("Log date cannot be null");
        }

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new NotFoundException("Habit not found"));

        String userId = userService.getCurrentUserId();
        if (!habit.getUserId().equals(userId)) {
            throw new PermissionDeniedException("You do not have permission to log this habit");
        }

        Optional<HabitLog> existingLogOpt = habitLogRepository.findByHabitIdAndLogDate(habitId, logDate);

        HabitLog habitLog;
        int oldValue = 0;
        int newValue;

        if (habit.getLogType() == LogType.BINARY) {
            if (existingLogOpt.isPresent()) {
                throw new IllegalArgumentException("Habit is already logged for " + logDate);
            }
            newValue = 1;
            habitLog = HabitLog.builder()
                    .habit(habit)
                    .logDate(logDate)
                    .currentValue(newValue)
                    .build();
        } else { // NUMERIC / COUNTER
            if (existingLogOpt.isPresent()) {
                habitLog = existingLogOpt.get();
                oldValue = habitLog.getCurrentValue();
                newValue = oldValue + 1;
                habitLog.setCurrentValue(newValue);
            } else {
                newValue = 1;
                habitLog = HabitLog.builder()
                        .habit(habit)
                        .logDate(logDate)
                        .currentValue(newValue)
                        .build();
            }
        }

        int target = habit.getTargetValue() != null ? habit.getTargetValue() : 1;
        if (oldValue < target && newValue >= target) {
            int streak = calculateStreak(habit, logDate);
            int minutes = getHabitMinutesEarned(streak);
            habitLog.setMinutesEarned(minutes);
            userBalanceService.addSeconds(userId, minutes * 60);
        }

        HabitLog savedLog = habitLogRepository.save(habitLog);
        return habitLogConverter.toDTO(savedLog);
    }

    @Transactional
    public HabitLogDTO cancelLog(Long habitId, LocalDate logDate) throws Exception {
        if (logDate == null) {
            throw new IllegalArgumentException("Log date cannot be null");
        }

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new NotFoundException("Habit not found"));

        String userId = userService.getCurrentUserId();
        if (!habit.getUserId().equals(userId)) {
            throw new PermissionDeniedException("You do not have permission to cancel logs for this habit");
        }

        HabitLog habitLog = habitLogRepository.findByHabitIdAndLogDate(habitId, logDate)
                .orElseThrow(() -> new NotFoundException("Habit log not found"));

        if (habit.getLogType() == LogType.BINARY) {
            userBalanceService.subtractSeconds(userId, habitLog.getMinutesEarned() * 60);
            habitLogRepository.delete(habitLog);
            return null;
        } else { // NUMERIC
            int oldValue = habitLog.getCurrentValue();
            int newValue = oldValue - 1;
            int target = habit.getTargetValue() != null ? habit.getTargetValue() : 1;

            if (oldValue >= target && newValue < target) {
                userBalanceService.subtractSeconds(userId, habitLog.getMinutesEarned() * 60);
                habitLog.setMinutesEarned(0);
            }

            if (newValue <= 0) {
                habitLogRepository.delete(habitLog);
                return null;
            } else {
                habitLog.setCurrentValue(newValue);
                HabitLog savedLog = habitLogRepository.save(habitLog);
                return habitLogConverter.toDTO(savedLog);
            }
        }
    }

    private int calculateStreak(Habit habit, LocalDate date) {
        int streak = 1;
        LocalDate checkDate = date.minusDays(1);
        int target = habit.getTargetValue() != null ? habit.getTargetValue() : 1;
        while (true) {
            Optional<HabitLog> logOpt = habitLogRepository.findByHabitIdAndLogDate(habit.getId(), checkDate);
            if (logOpt.isPresent() && logOpt.get().getCurrentValue() >= target) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private int getHabitMinutesEarned(int streak) {
        if (streak >= 31) {
            return 20;
        } else if (streak >= 15) {
            return 17;
        } else if (streak >= 8) {
            return 15;
        } else if (streak >= 4) {
            return 12;
        } else {
            return 10;
        }
    }
}
