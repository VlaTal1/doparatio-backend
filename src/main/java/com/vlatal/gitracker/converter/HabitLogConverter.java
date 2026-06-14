package com.vlatal.gitracker.converter;

import com.vlatal.gitracker.bom.HabitLogDTO;
import com.vlatal.gitracker.entity.HabitLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HabitLogConverter {

    private final HabitConverter habitConverter;

    public HabitLog fromDTO(HabitLogDTO entry) {
        if (entry == null) {
            return null;
        }
        return HabitLog.builder()
                .id(entry.getId())
                .habit(entry.getHabit() != null ? habitConverter.fromDTO(entry.getHabit()) : null)
                .logDate(entry.getLogDate())
                .currentValue(entry.getCurrentValue())
                .build();
    }

    public HabitLogDTO toDTO(HabitLog entry) {
        if (entry == null) {
            return null;
        }
        return HabitLogDTO.builder()
                .id(entry.getId())
                .habit(entry.getHabit() != null ? habitConverter.toDTO(entry.getHabit()) : null)
                .logDate(entry.getLogDate())
                .currentValue(entry.getCurrentValue())
                .build();
    }
}
