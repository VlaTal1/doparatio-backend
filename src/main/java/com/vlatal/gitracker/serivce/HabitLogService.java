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

    @Transactional
    public HabitLogDTO logHabit(Long habitId, LocalDate logDate) throws Exception {
        if (logDate == null) {
            throw new IllegalArgumentException("Log date cannot be null");
        }

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new NotFoundException("Habit not found"));

        if (!habit.getUserId().equals(userService.getCurrentUserId())) {
            throw new PermissionDeniedException("You do not have permission to log this habit");
        }

        Optional<HabitLog> existingLogOpt = habitLogRepository.findByHabitIdAndLogDate(habitId, logDate);

        HabitLog habitLog;
        if (habit.getLogType() == LogType.BINARY) {
            if (existingLogOpt.isPresent()) {
                throw new IllegalArgumentException("Habit is already logged for " + logDate);
            }
            habitLog = HabitLog.builder()
                    .habit(habit)
                    .logDate(logDate)
                    .currentValue(1)
                    .build();
        } else { // NUMERIC / COUNTER
            if (existingLogOpt.isPresent()) {
                habitLog = existingLogOpt.get();
                habitLog.setCurrentValue(habitLog.getCurrentValue() + 1);
            } else {
                habitLog = HabitLog.builder()
                        .habit(habit)
                        .logDate(logDate)
                        .currentValue(1)
                        .build();
            }
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

        if (!habit.getUserId().equals(userService.getCurrentUserId())) {
            throw new PermissionDeniedException("You do not have permission to cancel logs for this habit");
        }

        HabitLog habitLog = habitLogRepository.findByHabitIdAndLogDate(habitId, logDate)
                .orElseThrow(() -> new NotFoundException("Habit log not found"));

        if (habit.getLogType() == LogType.BINARY) {
            habitLogRepository.delete(habitLog);
            return null;
        } else { // NUMERIC
            int newValue = habitLog.getCurrentValue() - 1;
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
}
