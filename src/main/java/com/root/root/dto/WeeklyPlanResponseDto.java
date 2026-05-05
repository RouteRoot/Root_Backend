package com.root.root.dto;

import com.root.root.entity.WeeklyPlan;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class WeeklyPlanResponseDto {
    private Long id;
    private Integer weekNumber;
    private String weeklyGoal;

    // Entity인 DailyPlan 대신, DTO인 DailyPlanResponseDto 리스트를 가짐 (순환 참조 원천 차단!)
    private List<DailyPlanResponseDto> dailyPlans;

    // Entity -> DTO 변환 메서드
    public static WeeklyPlanResponseDto fromEntity(WeeklyPlan entity) {
        WeeklyPlanResponseDto dto = new WeeklyPlanResponseDto();
        dto.setId(entity.getId());
        dto.setWeekNumber(entity.getWeekNumber());
        dto.setWeeklyGoal(entity.getWeeklyGoal());

        // DailyPlan 엔티티 리스트를 DailyPlanResponseDto 리스트로 변환
        List<DailyPlanResponseDto> dailyDtos = entity.getDailyPlans().stream()
                .map(DailyPlanResponseDto::fromEntity)
                .collect(Collectors.toList());

        dto.setDailyPlans(dailyDtos);
        return dto;
    }
}
