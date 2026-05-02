package com.root.root.dto;

import com.root.root.entity.WeeklyPlan;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlanDetailResponseDto {
    private String status;
    private List<WeeklyPlan> weeklyPlans;
}
