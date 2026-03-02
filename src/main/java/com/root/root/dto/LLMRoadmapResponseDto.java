package com.root.root.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LLMRoadmapResponseDto {
    private List<PhaseDto> roadmap;

    @Getter
    @Setter
    public static class PhaseDto{
        private int phase;
        private String phaseTitle;
        private int estimatedWeeks;
        private List<TaskDto> tasks;
    }

    @Getter
    @Setter
    public static class TaskDto{
        private String taskName;
        private String description;
    }
}
