package com.vlatal.gitracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "HabitLog",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_HabitLog_habit_logDate", columnNames = {"habitId", "logDate"})
        }
)
public class HabitLog extends Audit {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitId", nullable = false)
    private Habit habit;

    @Column(name = "logDate", nullable = false)
    private LocalDate logDate;

    @Column(name = "currentValue", nullable = false)
    private Integer currentValue = 0;
}