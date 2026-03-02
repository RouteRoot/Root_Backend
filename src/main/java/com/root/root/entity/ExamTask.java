package com.root.root.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ExamTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String taskName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer totalWeeks;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.NOT_STARTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id")
    private Phase phase;

    @OneToMany(mappedBy = "examTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeeklyPlan> weeklyPlans = new ArrayList<>();

    public enum TaskStatus{
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }
}
