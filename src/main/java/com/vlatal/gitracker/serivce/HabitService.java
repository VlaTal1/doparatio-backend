package com.vlatal.gitracker.serivce;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.bom.HabitLogDTO;
import com.vlatal.gitracker.converter.HabitConverter;
import com.vlatal.gitracker.domain.LogType;
import com.vlatal.gitracker.entity.Habit;
import com.vlatal.gitracker.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitConverter habitConverter;

    private final UserService userService;

    private final HabitRepository habitRepository;

    public HabitDTO create(HabitDTO habitDTO) throws Exception {
        if (habitDTO.getLogType() == LogType.BINARY && habitDTO.getTargetValue() > 1) {
            throw new IllegalArgumentException("Target value of binary habit can not be more then 1");
        }
        if (!CollectionUtils.isEmpty(habitDTO.getScheduleDays())
                && habitDTO.getScheduleDays().stream().anyMatch(d -> d < 1 || d > 7)
        ) {
            throw new IllegalArgumentException("Schedule days must be between 1 and 7");
        }

        if (habitDTO.getTargetValue() == null || habitDTO.getTargetValue() == 0) {
            habitDTO.setTargetValue(1);
        }

        habitDTO.setActive(true);
        habitDTO.setUserId(userService.getCurrentUserId());

        Habit saved = habitRepository.save(habitConverter.fromDTO(habitDTO));
        return habitConverter.toDTO(saved);
    }
}
