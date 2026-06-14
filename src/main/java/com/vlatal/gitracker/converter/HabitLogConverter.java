package com.vlatal.gitracker.converter;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.bom.HabitLogDTO;
import com.vlatal.gitracker.entity.HabitLog;
import org.springframework.stereotype.Component;

@Component
public class HabitLogConverter {

    public HabitLog fromDTO(HabitLogDTO entry) {
        if (entry == null) {
            return null;
        }
        return HabitLog.builder()
                .id(entry.getId())
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
                .habit(entry.getHabit() != null ? HabitDTO.builder().id(entry.getHabit().getId()).build() : null)
                .logDate(entry.getLogDate())
                .currentValue(entry.getCurrentValue())
                .build();
    }
}
