package com.vlatal.gitracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(
        name = "task_log",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TaskLog_task_logDate", columnNames = {"task_id", "log_date"})
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaskLog extends Audit {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "minutes_earned", nullable = false)
    @lombok.Builder.Default
    private Integer minutesEarned = 0;
}
