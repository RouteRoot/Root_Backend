package com.root.root.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanTabResponseDto {
    private Long examTaskId;
    private String taskName;
}
