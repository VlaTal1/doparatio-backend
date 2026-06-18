package com.vlatal.gitracker.converter;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.bom.HabitLogDTO;
import com.vlatal.gitracker.domain.LogType;
import com.vlatal.gitracker.entity.Habit;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HabitConverter {

    public Habit fromDTO(HabitDTO entry) {
        if (entry == null) {
            return null;
        }
        return Habit.builder()
                .id(entry.getId())
                .name(entry.getName())
                .icon(entry.getIcon())
                .color(entry.getColor())
                .logType(entry.getLogType())
                .targetValue(entry.getTargetValue())
                .scheduleDays(entry.getScheduleDays())
                .active(entry.isActive())
                .userId(entry.getUserId())
                .build();
    }

    public HabitDTO toDTO(Habit entry) {
        if (entry == null) {
            return null;
        }
        return HabitDTO.builder()
                .id(entry.getId())
                .name(entry.getName())
                .icon(entry.getIcon())
                .color(entry.getColor())
                .logType(entry.getLogType())
                .targetValue(entry.getTargetValue())
                .scheduleDays(entry.getScheduleDays())
                .active(entry.isActive())
                .userId(entry.getUserId())
                .logs(entry.getLogs() != null ? entry.getLogs().stream().map(log -> {
                    HabitLogDTO dto = HabitLogDTO.builder()
                            .id(log.getId())
                            .logDate(log.getLogDate())
                            .currentValue(log.getCurrentValue())
                            .build();
                    return dto;
                }).toList() : null)
                .totalExecutions(calculateTotalExecutions(entry))
                .completedDays(calculateCompletedDays(entry))
                .bestStreak(calculateBestStreak(entry))
                .build();
    }

    private int calculateTotalExecutions(Habit habit) {
        if (habit.getLogs() == null) {
            return 0;
        }
        if (habit.getLogType() == LogType.BINARY) {
            return habit.getLogs().size();
        } else {
            return habit.getLogs().stream()
                    .filter(log -> log.getCurrentValue() != null)
                    .mapToInt(com.vlatal.gitracker.entity.HabitLog::getCurrentValue)
                    .sum();
        }
    }

    private int calculateCompletedDays(Habit habit) {
        if (habit.getLogs() == null) {
            return 0;
        }
        if (habit.getLogType() == LogType.BINARY) {
            return habit.getLogs().size();
        } else {
            return (int) habit.getLogs().stream()
                    .filter(log -> log.getCurrentValue() != null && log.getCurrentValue() >= 1)
                    .count();
        }
    }

    private int calculateBestStreak(Habit habit) {
        if (habit.getLogs() == null || habit.getLogs().isEmpty()) {
            return 0;
        }

        List<java.time.LocalDate> completedDates = habit.getLogs().stream()
                .filter(log -> {
                    if (habit.getLogType() == LogType.BINARY) {
                        return true;
                    } else {
                        return log.getCurrentValue() != null && log.getCurrentValue() >= 1;
                    }
                })
                .map(com.vlatal.gitracker.entity.HabitLog::getLogDate)
                .sorted()
                .toList();

        if (completedDates.isEmpty()) {
            return 0;
        }

        int maxStreak = 0;
        int currentStreak = 0;
        java.time.LocalDate lastDate = null;

        for (java.time.LocalDate date : completedDates) {
            if (lastDate == null) {
                currentStreak = 1;
            } else if (date.equals(lastDate.plusDays(1))) {
                currentStreak++;
            } else if (!date.equals(lastDate)) {
                currentStreak = 1;
            }
            maxStreak = Math.max(maxStreak, currentStreak);
            lastDate = date;
        }

        return maxStreak;
    }
}
