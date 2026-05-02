package com.root.root.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PlanResponseDto {
    private Long examTaskId;
    private String taskName;
    private int totalWeeks;
    private String status;
    private List<WeeklyPlanDto> weeklyPlans;

    @Getter
    @Setter
    public static class WeeklyPlanDto{
        private Long weeklyPlanId;
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
        private Boolean isCompleted;
        private Boolean isRest;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate studyDate;
    }
}
