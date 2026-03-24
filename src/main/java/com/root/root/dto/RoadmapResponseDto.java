package com.root.root.dto;

import com.root.root.entity.ExamTask;
import com.root.root.entity.Phase;
import com.root.root.entity.Roadmap;
import com.root.root.entity.User;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RoadmapResponseDto {
    private Long roadmapId;
    private List<PhaseDto>  phases;
    private int daily;
    private int weekly;
    private String mylevel;
    // Roadmap -> DTO
    public RoadmapResponseDto(Roadmap roadmap, User user){
        this.roadmapId = roadmap.getId();
        this.phases = roadmap.getPhases().stream().map(PhaseDto::new).collect(Collectors.toList());
        this.daily = user.getDaily();
        this.weekly = user.getWeekly();
        this.mylevel = user.getMylevel();
    }

    @Getter
    public static class PhaseDto{
        private Long phaseId;
        private Integer phaseNumber;
        private String phaseTitle;
        private Integer estimatedWeeks;
        private List<ExamTaskDto> tasks;

        public PhaseDto(Phase phase){
            this.phaseId = phase.getId();
            this.phaseNumber = phase.getPhaseNumber();
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
            this.status = task.getStatus();
        }
    }
}
