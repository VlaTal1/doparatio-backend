package com.vlatal.gitracker.controller;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.domain.LogType;
import com.vlatal.gitracker.exception.NotFoundException;
import com.vlatal.gitracker.exception.PermissionDeniedException;
import com.vlatal.gitracker.bom.HabitLogDTO;
import com.vlatal.gitracker.serivce.HabitLogService;
import com.vlatal.gitracker.serivce.HabitService;
import com.vlatal.gitracker.serivce.JwtService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HabitController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.vlatal.gitracker.config.SecurityConfig.class))
public class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HabitService habitService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private HabitLogService habitLogService;

    @Test
    public void createTest_success() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        HabitDTO savedDto = HabitDTO.builder()
                .id(1L)
                .name("Drink Water")
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .active(true)
                .userId("test-user-id")
                .build();

        when(habitService.create(any(HabitDTO.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drink Water"))
                .andExpect(jsonPath("$.icon").value("water"))
                .andExpect(jsonPath("$.color").value("FFFFFF"))
                .andExpect(jsonPath("$.logType").value("BINARY"))
                .andExpect(jsonPath("$.targetValue").value(1))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.userId").value("test-user-id"));
    }

    @Test
    public void createTest_invalidName_nullOrBlank() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("") // blank name
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTest_invalidName_tooLong() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("a".repeat(51)) // > 50 characters
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTest_invalidIcon_nullOrBlank() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon(null) // null icon
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTest_invalidIcon_tooLong() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("a".repeat(51)) // > 50 characters
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTest_invalidColor_nullOrBlank() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("   ") // blank color
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTest_invalidColor_invalidPattern() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("#FFFFFF") // invalid pattern (starts with #)
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTest_invalidLogType_null() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("FFFFFF")
                .logType(null) // null logType
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTest_invalidTargetValue_null() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(null) // null targetValue
                .scheduleDays(List.of(1, 2, 3))
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTest_invalidTargetValue_tooLarge() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(100) // > 99
                .scheduleDays(List.of(1, 2, 3))
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTest_invalidScheduleDays_tooMany() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3, 4, 5, 6, 7, 8)) // > 7 elements
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTest_invalidScheduleDays_empty() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of()) // < 1 element
                .build();

        mockMvc.perform(post("/api/habit/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteTest_success() throws Exception {
        doNothing().when(habitService).delete(1L);

        mockMvc.perform(delete("/api/habit/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTest_notFound() throws Exception {
        doThrow(new NotFoundException("Habit not found")).when(habitService).delete(1L);

        mockMvc.perform(delete("/api/habit/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found"));
    }

    @Test
    public void deleteTest_permissionDenied() throws Exception {
        doThrow(new PermissionDeniedException("You do not have permission")).when(habitService).delete(1L);

        mockMvc.perform(delete("/api/habit/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    public void updateTest_success() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water Updated")
                .icon("water")
                .color("000000")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .active(false)
                .build();

        HabitDTO updatedDto = HabitDTO.builder()
                .id(1L)
                .name("Drink Water Updated")
                .icon("water")
                .color("000000")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .active(false)
                .userId("test-user-id")
                .build();

        when(habitService.update(eq(1L), any(HabitDTO.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/habit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drink Water Updated"))
                .andExpect(jsonPath("$.color").value("000000"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    public void updateTest_validationFailed() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("") // blank name
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        mockMvc.perform(put("/api/habit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateTest_notFound() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        when(habitService.update(eq(1L), any(HabitDTO.class))).thenThrow(new NotFoundException("Habit not found"));

        mockMvc.perform(put("/api/habit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found"));
    }

    @Test
    public void updateTest_permissionDenied() throws Exception {
        HabitDTO inputDto = HabitDTO.builder()
                .name("Drink Water")
                .icon("water")
                .color("FFFFFF")
                .logType(LogType.BINARY)
                .targetValue(1)
                .scheduleDays(List.of(1, 2, 3))
                .build();

        when(habitService.update(eq(1L), any(HabitDTO.class)))
                .thenThrow(new PermissionDeniedException("You do not have permission"));

        mockMvc.perform(put("/api/habit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    public void logHabitTest_success() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 14);
        HabitLogDTO inputDto = HabitLogDTO.builder()
                .logDate(date)
                .build();
        HabitLogDTO savedDto = HabitLogDTO.builder()
                .id(10L)
                .logDate(date)
                .currentValue(1)
                .build();

        when(habitLogService.logHabit(1L, date)).thenReturn(savedDto);

        mockMvc.perform(post("/api/habit/1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.currentValue").value(1))
                .andExpect(jsonPath("$.logDate").value("2026-06-14"));
    }

    @Test
    public void logHabitTest_notFound() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 14);
        HabitLogDTO inputDto = HabitLogDTO.builder()
                .logDate(date)
                .build();
        when(habitLogService.logHabit(1L, date)).thenThrow(new NotFoundException("Habit not found"));

        mockMvc.perform(post("/api/habit/1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found"));
    }

    @Test
    public void logHabitTest_permissionDenied() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 14);
        HabitLogDTO inputDto = HabitLogDTO.builder()
                .logDate(date)
                .build();
        when(habitLogService.logHabit(1L, date)).thenThrow(new PermissionDeniedException("You do not have permission"));

        mockMvc.perform(post("/api/habit/1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    public void logHabitTest_invalidArgument() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 14);
        HabitLogDTO inputDto = HabitLogDTO.builder()
                .logDate(date)
                .build();
        when(habitLogService.logHabit(1L, date)).thenThrow(new IllegalArgumentException("Habit is already logged for 2026-06-14"));

        mockMvc.perform(post("/api/habit/1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Habit is already logged for 2026-06-14"));
    }

    @Test
    public void cancelLogTest_deleted() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 14);
        HabitLogDTO inputDto = HabitLogDTO.builder()
                .logDate(date)
                .build();

        when(habitLogService.cancelLog(1L, date)).thenReturn(null);

        mockMvc.perform(delete("/api/habit/1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void cancelLogTest_decremented() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 14);
        HabitLogDTO inputDto = HabitLogDTO.builder()
                .logDate(date)
                .build();
        HabitLogDTO savedDto = HabitLogDTO.builder()
                .id(10L)
                .logDate(date)
                .currentValue(2)
                .build();

        when(habitLogService.cancelLog(1L, date)).thenReturn(savedDto);

        mockMvc.perform(delete("/api/habit/1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.currentValue").value(2));
    }

    @Test
    public void cancelLogTest_notFound() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 14);
        HabitLogDTO inputDto = HabitLogDTO.builder()
                .logDate(date)
                .build();

        when(habitLogService.cancelLog(1L, date)).thenThrow(new NotFoundException("Habit log not found"));

        mockMvc.perform(delete("/api/habit/1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit log not found"));
    }

    @Test
    public void cancelLogTest_permissionDenied() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 14);
        HabitLogDTO inputDto = HabitLogDTO.builder()
                .logDate(date)
                .build();

        when(habitLogService.cancelLog(1L, date)).thenThrow(new PermissionDeniedException("You do not have permission"));

        mockMvc.perform(delete("/api/habit/1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }
}
