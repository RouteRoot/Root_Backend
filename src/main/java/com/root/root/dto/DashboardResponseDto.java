package com.root.root.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DashboardResponseDto {
    private CurrentStudyPlanDto currentStudyPlan;
    private PlanProgressDto planProgress;
    private RoadmapProgressDto roadmapProgress;

    @Getter
    @Builder
    public static class CurrentStudyPlanDto{
        private Long weeklyPlanId;
        private Integer weekNumber;
        private String weeklyGoal;

        private Long dailyPlanId;
        private LocalDate date;
        private String topic;
        private boolean isCompleted;
    }

    @Getter
    @Builder
    public static class PlanProgressDto{
        private int totalPlanDays;
        private int completedPlanDays;
    }

    @Getter
    @Builder
    public static class RoadmapProgressDto{
        private int totalTasks;
        private int completedTasks;
    }
}
