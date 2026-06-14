package com.vlatal.gitracker.bom;

import com.vlatal.gitracker.domain.Difficulty;
import com.vlatal.gitracker.validation.groups.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private Long id;

    @NotBlank(groups = OnCreate.class)
    @Size(min = 1, max = 100)
    private String name;

    @NotNull(groups = OnCreate.class)
    private Difficulty difficulty;

    @NotNull(groups = OnCreate.class)
    private Boolean recurring;

    private LocalDate dueDate;

    @Size(min = 1, max = 7)
    private List<Integer> scheduleDays;

    @lombok.Builder.Default
    private boolean active = true;

    private String userId;

    private List<TaskLogDTO> logs;
}
