package com.root.root.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ExamData {

    @Id
    private String examCode; // 종목 코드
    private String examName; // 시험명

    private String examGroup; // 그룹명

    private String examType; // 필기 / 실기 / 단일

    private Long prerequisiteId; // 선수 시험 ID

    private String category; // 분야

    private String organization; // 주관 기관

    @Column(columnDefinition = "TEXT")
    private String description; // 시험 설명

    private String officialUrl; // 공식 링크
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true; // 활성화 여부

    @OneToMany(mappedBy = "examData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExamSchedule> schedules = new ArrayList<>();
}
