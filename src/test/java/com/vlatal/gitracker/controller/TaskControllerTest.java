package com.vlatal.gitracker.controller;

import com.vlatal.gitracker.bom.TaskDTO;
import com.vlatal.gitracker.bom.TaskLogDTO;
import com.vlatal.gitracker.domain.Difficulty;
import com.vlatal.gitracker.serivce.JwtService;
import com.vlatal.gitracker.serivce.TaskLogService;
import com.vlatal.gitracker.serivce.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.vlatal.gitracker.config.SecurityConfig.class))
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TaskLogService taskLogService;

    @Test
    public void createTest_success() throws Exception {
        TaskDTO inputDto = TaskDTO.builder()
                .name("Clean room")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .build();
        TaskDTO savedDto = TaskDTO.builder()
                .id(1L)
                .name("Clean room")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .active(true)
                .build();

        when(taskService.create(any(TaskDTO.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Clean room"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    public void getTasks_all_success() throws Exception {
        TaskDTO task = TaskDTO.builder()
                .id(1L)
                .name("Workout")
                .difficulty(Difficulty.HARD)
                .recurring(true)
                .scheduleDays(List.of(1, 3, 5))
                .active(true)
                .build();

        when(taskService.getAll()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Workout"));
    }

    @Test
    public void getTasks_date_success() throws Exception {
        TaskDTO task = TaskDTO.builder()
                .id(1L)
                .name("Clean room")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .active(true)
                .build();

        when(taskService.getAllForDate(LocalDate.of(2026, 6, 14))).thenReturn(List.of(task));

        mockMvc.perform(get("/api/task?date=2026-06-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Clean room"));
    }

    @Test
    public void getById_success() throws Exception {
        TaskDTO task = TaskDTO.builder()
                .id(1L)
                .name("Clean room")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .active(true)
                .build();

        when(taskService.getById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Clean room"));
    }

    @Test
    public void update_success() throws Exception {
        TaskDTO inputDto = TaskDTO.builder()
                .name("Clean kitchen")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .build();
        TaskDTO savedDto = TaskDTO.builder()
                .id(1L)
                .name("Clean kitchen")
                .difficulty(Difficulty.MEDIUM)
                .recurring(false)
                .dueDate(LocalDate.of(2026, 6, 14))
                .active(true)
                .build();

        when(taskService.update(eq(1L), any(TaskDTO.class))).thenReturn(savedDto);

        mockMvc.perform(put("/api/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Clean kitchen"));
    }

    @Test
    public void delete_success() throws Exception {
        doNothing().when(taskService).delete(1L);

        mockMvc.perform(delete("/api/task/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void logTask_success() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 14);
        TaskLogDTO inputDto = TaskLogDTO.builder().logDate(date).build();
        TaskLogDTO savedDto = TaskLogDTO.builder().id(10L).logDate(date).build();

        when(taskLogService.logTask(1L, date)).thenReturn(savedDto);

        mockMvc.perform(post("/api/task/1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.logDate").value("2026-06-14"));
    }

    @Test
    public void cancelLog_success() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 14);
        TaskLogDTO inputDto = TaskLogDTO.builder().logDate(date).build();

        doNothing().when(taskLogService).cancelLog(1L, date);

        mockMvc.perform(delete("/api/task/1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNoContent());
    }
}
