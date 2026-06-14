package com.vlatal.gitracker.controller;

import com.vlatal.gitracker.bom.HabitDTO;
import com.vlatal.gitracker.domain.LogType;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.vlatal.gitracker.exception.NotFoundException;
import com.vlatal.gitracker.exception.PermissionDeniedException;

@WebMvcTest(
        controllers = HabitController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.vlatal.gitracker.config.SecurityConfig.class)
)
public class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HabitService habitService;

    @MockitoBean
    private JwtService jwtService;

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
}
