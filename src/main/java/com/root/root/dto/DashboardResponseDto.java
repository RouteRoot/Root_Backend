package com.root.root.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DashboardResponseDto {
    private CurrentStudyPlanDto currentStudyPlan; // 학습 플랜 합쳐서 전달
    private PlanProgressDto planProgress; // 학습 플랜 진행도
    private RoadmapProgressDto roadmapProgress; // 로드맵 진행도

    @Getter
    @Builder
    public static class CurrentStudyPlanDto{
        // 주간 정보
        private Long weeklyPlanId;
        private Integer weekNumber; // n주차
        private String weeklyGoal; // 주간 목표

        // 오늘의 정보
        private Long dailyPlanId;
        private LocalDate date; // 오늘 날짜
        private String topic; // 오늘 목표
        private boolean isCompleted; // 완료 여부
    }

    @Getter
    @Builder
    public static class PlanProgressDto{
        private int totalPlanDays; // 전체 일간 플랜 개수
        private int completedPlanDays; // 완료된 일간 플랜 개수
    }

    @Getter
    @Builder
    public static class RoadmapProgressDto{
        private int totalTasks; // 로드맵의 전체 자격증 개수
        private int completedTasks; // 취득한 자격증 개수
    }
}
