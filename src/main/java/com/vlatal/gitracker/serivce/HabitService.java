package com.vlatal.gitracker.serivce;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.converter.HabitConverter;
import com.vlatal.gitracker.domain.LogType;
import com.vlatal.gitracker.entity.Habit;
import com.vlatal.gitracker.exception.NotFoundException;
import com.vlatal.gitracker.exception.PermissionDeniedException;
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
        validate(habitDTO);

        habitDTO.setActive(true);
        habitDTO.setUserId(userService.getCurrentUserId());

        Habit saved = habitRepository.save(habitConverter.fromDTO(habitDTO));
        return habitConverter.toDTO(saved);
    }

    public void delete(Long id) throws Exception {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Habit not found"));

        if (!habit.getUserId().equals(userService.getCurrentUserId())) {
            throw new PermissionDeniedException("You do not have permission to delete this habit");
        }

        habitRepository.delete(habit);
    }

    public HabitDTO update(Long id, HabitDTO habitDTO) throws Exception {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Habit not found"));

        if (!habit.getUserId().equals(userService.getCurrentUserId())) {
            throw new PermissionDeniedException("You do not have permission to update this habit");
        }

        validate(habitDTO);

        habit.setName(habitDTO.getName());
        habit.setIcon(habitDTO.getIcon());
        habit.setColor(habitDTO.getColor());
        habit.setLogType(habitDTO.getLogType());
        habit.setTargetValue(habitDTO.getTargetValue());
        habit.setScheduleDays(habitDTO.getScheduleDays());
        habit.setActive(habitDTO.isActive());

        Habit saved = habitRepository.save(habit);
        return habitConverter.toDTO(saved);
    }

    private void validate(HabitDTO habitDTO) {
        if (habitDTO.getTargetValue() == null || habitDTO.getTargetValue() == 0) {
            habitDTO.setTargetValue(1);
        }

        if (habitDTO.getLogType() == LogType.BINARY && habitDTO.getTargetValue() > 1) {
            throw new IllegalArgumentException("Target value of binary habit can not be more then 1");
        }
        if (!CollectionUtils.isEmpty(habitDTO.getScheduleDays())
                && habitDTO.getScheduleDays().stream().anyMatch(d -> d < 1 || d > 7)
        ) {
            throw new IllegalArgumentException("Schedule days must be between 1 and 7");
        }
    }
}
