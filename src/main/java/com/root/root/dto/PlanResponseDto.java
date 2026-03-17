package com.root.root.dto;

import lombok.Setter;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PlanResponseDto {
    private String targetExam;
    private int totalWeeks;
    private List<WeeklyPlanDto> weeklyPlans;

    @Getter
    @Setter
    public static class WeeklyPlanDto{
        private int weekNumber;
        private String weeklyGoal;
        private List<DailyPlanDto> dailyPlans;
    }

    @Getter
    @Setter
    public static class DailyPlanDto{
        private Long dailyPlanId;
        private int dayNumber;
        private String topic;
        private String description;
        private int estimatedHours;
        private boolean isRest;
        private LocalDate studyDate;
        private boolean isCompleted;
    }
}
