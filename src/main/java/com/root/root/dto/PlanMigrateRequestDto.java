package com.root.root.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PlanMigrateRequestDto {
    private Long dailyPlanId;
    private LocalDate targetDate;
}
