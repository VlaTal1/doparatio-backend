package com.vlatal.gitracker.controller;

import com.vlatal.gitracker.bom.TaskDTO;
import com.vlatal.gitracker.bom.TaskLogDTO;
import com.vlatal.gitracker.serivce.TaskLogService;
import com.vlatal.gitracker.serivce.TaskService;
import com.vlatal.gitracker.validation.groups.OnCreate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskLogService taskLogService;

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws Exception {
        if (date != null) {
            return ResponseEntity.ok(taskService.getAllForDate(date));
        }
        return ResponseEntity.ok(taskService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TaskDTO> create(@Validated(OnCreate.class) @RequestBody TaskDTO taskDTO) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(taskDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable Long id, @Validated(OnCreate.class) @RequestBody TaskDTO taskDTO) throws Exception {
        return ResponseEntity.ok(taskService.update(id, taskDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/log")
    public ResponseEntity<TaskLogDTO> logTask(@PathVariable Long id, @Validated @RequestBody TaskLogDTO taskLogDTO) throws Exception {
        return ResponseEntity.ok(taskLogService.logTask(id, taskLogDTO.getLogDate()));
    }

    @DeleteMapping("/{id}/log")
    public ResponseEntity<Void> cancelLog(@PathVariable Long id, @Validated @RequestBody TaskLogDTO taskLogDTO) throws Exception {
        taskLogService.cancelLog(id, taskLogDTO.getLogDate());
        return ResponseEntity.noContent().build();
    }
}
