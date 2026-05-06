package com.root.root.dto;

import com.root.root.entity.DailyPlan;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DailyPlanResponseDto {
    private Long id;
    private Integer dayNumber;
    private LocalDate studyDate;
    private String topic;
    private String description;
    private Integer estimatedHours;
    private boolean isRest;
    private boolean isCompleted;

    // Entity -> DTO 변환 메서드
    public static DailyPlanResponseDto fromEntity(DailyPlan entity) {
        DailyPlanResponseDto dto = new DailyPlanResponseDto();
        dto.setId(entity.getId());
        dto.setDayNumber(entity.getDayNumber());
        dto.setStudyDate(entity.getStudyDate());
        dto.setTopic(entity.getTopic());
        dto.setDescription(entity.getDescription());
        dto.setEstimatedHours(entity.getEstimatedHours());
        dto.setRest(entity.isRest());
        dto.setCompleted(entity.isCompleted());
        return dto;
    }
}
