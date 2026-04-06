package com.root.root.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.root.root.entity.ExamData;
import com.root.root.entity.ExamSchedule;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {
    Optional<ExamSchedule> findByExamDataAndRound(ExamData examData, String round);
}