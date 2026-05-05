package com.root.root.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
public class PlanSettingsResponseDto {
    private Long examTaskId;
    private String certificationName;
    private LocalDate examDate;
    private String skillLevel;
    private String personalStory;
    private Map<String, Integer> weeklySchedule;
}
