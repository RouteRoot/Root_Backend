package com.root.root.dto;

import com.root.root.entity.ExamTask;
import com.root.root.entity.Phase;
import com.root.root.entity.Roadmap;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RoadmapResponseDto {
    private Long roadmapId;
    private String roadmapHope;
    private List<PhaseDto>  phases;
    // Roadmap -> DTO
    public RoadmapResponseDto(Roadmap roadmap){
        this.roadmapId = roadmap.getId();
        this.roadmapHope = roadmap.getHope();
        this.phases = roadmap.getPhases().stream().map(PhaseDto::new).collect(Collectors.toList());
    }

    @Getter
    public static class PhaseDto{
        private Long phaseId;
        private int phaseNumber;
        private String phaseTitle;
        private int estimatedWeeks;
        private List<ExamTaskDto> tasks;

        public PhaseDto(Phase phase){
            this.phaseId = phase.getId();
            this.phaseNumber = phase.getPhase();
            this.phaseTitle = phase.getPhaseTitle();
            this.estimatedWeeks = phase.getEstimatedWeeks();
            this.tasks = phase.getTasks().stream().map(ExamTaskDto::new).collect(Collectors.toList());
        }
    }

    @Getter
    public static class ExamTaskDto {
        private Long taskId;
        private String taskName;
        private String description;
        private String status;

        public ExamTaskDto(ExamTask task) {
            this.taskId = task.getId();
            this.taskName = task.getTaskName();
            this.description = task.getDescription();
            this.status = task.getStatus().name();
        }
    }
}
