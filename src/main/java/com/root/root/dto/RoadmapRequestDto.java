package com.root.root.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoadmapRequestDto {
    //private Long userId;
    private String educationStatus; // 학위 상태(고졸, 2년제 재학, 2년제 졸업, 3년제 재학, 3년제 졸업, 4년제 재학, 4년제 졸업)
    private int grade; // 학년(0, 1, 2, 3, 4)
    private String major; // 전공
    private String hope; // 희망 직무(자세하게 (예)백엔드 개발자)
    private boolean isMajorRelated; // 전공과 희망 직무 관련 여부
    private int career; // 해당 직무 관련 실무 경력(연 단위)
    private List<String> acquired; // 기취즉 자격증 리스트
    private int daily; // 투자 가능한 주중 학습 시간(하루 기준)
    private int weekly; // 투자 가능한 주간 학습 시간(주말 포함 한 주 기준)
    private String mylevel; // 실력 자가 진단(상 중 하)
    private String target; // 목표 기업 형태(사기업, 공기업, 스타트업)
}
