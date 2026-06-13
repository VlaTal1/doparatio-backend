package com.vlatal.gitracker.bom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HabitLogDTO {

    private Long id;

    private HabitDTO habit;

    private LocalDate logDate;

    private Integer currentValue = 0;
}
