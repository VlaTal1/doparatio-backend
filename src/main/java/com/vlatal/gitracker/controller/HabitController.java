package com.vlatal.gitracker.controller;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.serivce.HabitService;
import com.vlatal.gitracker.validation.groups.OnCreate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/habit")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

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
}
