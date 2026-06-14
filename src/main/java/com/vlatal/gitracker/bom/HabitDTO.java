package com.vlatal.gitracker.bom;

import com.vlatal.gitracker.domain.LogType;
import com.vlatal.gitracker.validation.groups.OnCreate;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HabitDTO {

    private Long id;

    @NotBlank(groups = OnCreate.class)
    @Size(min = 1, max = 50)
    private String name;

    @NotBlank(groups = OnCreate.class)
    @Size(min = 1, max = 50)
    private String icon;

    @NotBlank(groups = OnCreate.class)
    @Pattern(
            regexp = "^[0-9A-Fa-f]{6}$",
            message = "Color must be a valid 6-character HEX code without #",
            groups = OnCreate.class
    )
    private String color;

    @NotNull(groups = OnCreate.class)
    private LogType logType;

    @NotNull(groups = OnCreate.class)
    @Max(99)
    private Integer targetValue;

    @Size(min = 1, max = 7)
    private List<Integer> scheduleDays;

    private boolean active = true;

    private String userId;

    private List<HabitLogDTO> logs;
}