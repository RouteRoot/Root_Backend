package com.root.root.entity;

import com.root.root.util.WeeklyScheduleConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ExamTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id", nullable = false)
    private Phase phase;

    // ExamSchedule과 연결
    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "exam_schedule_id")
    //private ExamSchedule examSchedule;

    @Column(length = 100)
    private String taskName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer totalWeeks;

    @Column(length = 20)
    private String status = "NOT_STARTED";

    @OneToMany(mappedBy = "examTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeeklyPlan> weeklyPlans = new ArrayList<>();

    @Column
    private String certificationName;

    @Column
    private LocalDate examDate;

    @Column(length = 20)
    private String skillLevel;

    @Column(columnDefinition = "TEXT")
    private String personalStory;

    @Convert(converter = WeeklyScheduleConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Integer> weeklySchedule;
}
