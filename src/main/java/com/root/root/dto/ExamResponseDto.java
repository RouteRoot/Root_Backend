package com.root.root.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;

@Data

@NoArgsConstructor
@AllArgsConstructor
public class ExamResponseDto {

    private String examCode;
    private String examName;
    private List<ScheduleDto> schedules;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDto {

        private String round;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate examDate;
        private Long dDay;
    }
}
