package com.root.root.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DailyPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_plan_id", nullable = false)
    private WeeklyPlan weeklyPlan;

    private Integer dayNumber;

    private LocalDate studyDate;

    @Column(length = 255)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer estimatedHours;

    @Column(nullable = false)
    private boolean isRest = false;

    @Column(nullable = false)
    private boolean isCompleted = false;
}
