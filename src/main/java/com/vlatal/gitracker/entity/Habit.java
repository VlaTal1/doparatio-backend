package com.vlatal.gitracker.entity;

import com.vlatal.gitracker.domain.LogType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Habit")
public class Habit extends Audit {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon", nullable = false)
    private String icon;

    @Column(name = "color", nullable = false, length = 6)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "logType", nullable = false, length = 20)
    private LogType logType;

    @Column(name = "targetValue", nullable = false)
    private Integer targetValue;

    @Column(name = "scheduleDays")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private Integer[] scheduleDays;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HabitLog> logs = new ArrayList<>();
}