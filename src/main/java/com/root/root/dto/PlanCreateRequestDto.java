package com.root.root.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class PlanCreateRequestDto {
    private Long examTaskId; // 어떤 자격증의 플랜을 짤 것인지 식별
    private LocalDate examDate; // 유저가 선택한 시험일(D-Day)
    private String certificationName; // 자격증 명칭
    private int daily; // 주중 일일 학습 시간
    private int weekly; // 주간 총 학습 시간
    private String skillLevel; // 실력 자가 진단(상/중/하)
}
