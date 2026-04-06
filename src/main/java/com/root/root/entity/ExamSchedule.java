package com.root.root.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exam_schedule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 일정 자체의 고유 ID (순번)

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_code") // ExamData의 PK(종목코드)와 연결
    private ExamData examData;

    @Column(length = 100)
    private String round;                // 회차 (예: 2026년 정기 기사 1회)

    // --- 필기(doc) 일정 ---
    private LocalDate docRegStart;    // 필기 원서 접수 시작일
    private LocalDate docRegEnd;      // 필기 원서 접수 종료일
    private LocalDate docExamStart;   // 필기 시험 시작일
    private LocalDate docPassDate;    // 필기 합격자 발표일

    // --- 실기(prac) 일정 ---
    private LocalDate pracRegStart;   // 실기 원서 접수 시작일
    private LocalDate pracRegEnd;     // 실기 원서 접수 종료일
    private LocalDate pracExamStart;  // 실기 시험 시작일
    private LocalDate pracPassDate;   // 실기 합격자 발표일
}
