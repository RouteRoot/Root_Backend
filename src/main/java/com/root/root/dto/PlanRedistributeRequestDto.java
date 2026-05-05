package com.root.root.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Getter
@NoArgsConstructor
public class PlanRedistributeRequestDto {
    private Long examTaskId;
    private LocalDate newExamDate;
    private Map<String, Integer> weeklySchedule;
}
