package com.vlatal.gitracker.entity;

import com.vlatal.gitracker.domain.Difficulty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Task extends Audit {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(name = "recurring", nullable = false)
    private boolean recurring;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "schedule_days", columnDefinition = "integer[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Array(length = 7)
    @lombok.Builder.Default
    private List<Integer> scheduleDays = new ArrayList<>();

    @Column(name = "active", nullable = false)
    @lombok.Builder.Default
    private boolean active = true;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.Builder.Default
    private List<TaskLog> logs = new ArrayList<>();
}
