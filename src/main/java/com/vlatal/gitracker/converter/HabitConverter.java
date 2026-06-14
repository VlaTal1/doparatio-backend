package com.vlatal.gitracker.converter;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.bom.HabitLogDTO;
import com.vlatal.gitracker.entity.Habit;
import org.springframework.stereotype.Component;

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
                .build();
    }
}
