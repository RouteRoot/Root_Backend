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

    private String examGroup;     // 그룹명 (예: 기사)
    private String category;      // 분야 (예: 정보기술)
    private String organization;  // 주관 기관 (예: 한국산업인력공단)
    private String description;   // 설명 (예: 정보통신 분야 자격증)
    private String imageUrl;       // images/custom_1.jpg 등
    private String officialUrl;    // 큐넷 등 외부 링크
    private Boolean isActive;      // 활성화 여부
    private Long viewCount;     // 조회수
    
    private List<ScheduleDto> schedules;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDto {

        private String round;
        // 필기 응답
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate docExamStart;
        private Long docDDay;

        // 실기 응답
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate pracExamStart;
        private Long pracDDay;
    }
}
