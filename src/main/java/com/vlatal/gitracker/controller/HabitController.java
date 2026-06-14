package com.vlatal.gitracker.controller;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.bom.HabitLogDTO;
import com.vlatal.gitracker.serivce.HabitLogService;
import com.vlatal.gitracker.serivce.HabitService;
import com.vlatal.gitracker.validation.groups.OnCreate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/habit")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    private final HabitLogService habitLogService;

    @GetMapping
    public ResponseEntity<List<HabitDTO>> getAll() throws Exception {
        List<HabitDTO> habits = habitService.getAll();
        return ResponseEntity.ok(habits);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitDTO> getById(@PathVariable Long id) throws Exception {
        HabitDTO habit = habitService.getById(id);
        return ResponseEntity.ok(habit);
    }

    @PostMapping("/")
    public ResponseEntity<HabitDTO> create(@Validated(OnCreate.class) @RequestBody HabitDTO habitDTO) throws Exception {
        HabitDTO savedHabitDto = habitService.create(habitDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedHabitDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        habitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitDTO> update(@PathVariable Long id, @Validated(OnCreate.class) @RequestBody HabitDTO habitDTO) throws Exception {
        HabitDTO updated = habitService.update(id, habitDTO);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{habitId}/log")
    public ResponseEntity<HabitLogDTO> logHabit(
            @PathVariable Long habitId,
            @RequestBody HabitLogDTO request
    ) throws Exception {
        HabitLogDTO savedLog = habitLogService.logHabit(habitId, request.getLogDate());
        return ResponseEntity.ok(savedLog);
    }

    @DeleteMapping("/{habitId}/log")
    public ResponseEntity<HabitLogDTO> cancelLog(
            @PathVariable Long habitId,
            @RequestBody HabitLogDTO request
    ) throws Exception {
        HabitLogDTO result = habitLogService.cancelLog(habitId, request.getLogDate());
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}
