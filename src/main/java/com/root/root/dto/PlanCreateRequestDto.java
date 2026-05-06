package com.root.root.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class PlanCreateRequestDto {
    private Long examTaskId; // 어떤 자격증의 플랜을 짤 것인지 식별
    private LocalDate examDate; // 유저가 선택한 시험일(D-Day)
    private String certificationName; // 자격증 명칭
    private Map<String, Integer> weeklySchedule; // 요일별 학습 시간
    private String skillLevel; // 실력 자가 진단(상/중/하)
    private String personalStory; // 서술형 고민 입력 창
}
