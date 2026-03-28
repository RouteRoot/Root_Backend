package com.root.root.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

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

    private LocalDate registrationStart; // 접수 시작일
    private LocalDate registrationEnd;   // 접수 마감일
    private LocalDate examDate;          // 시험일
 
   private LocalDate resultDate;        // 발표일
}