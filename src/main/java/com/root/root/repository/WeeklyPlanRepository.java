package com.root.root.repository;

import com.root.root.entity.WeeklyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyPlanRepository extends JpaRepository<WeeklyPlan, Long> {
    // 중복 생성 방지
    boolean existsByExamTaskId(Long examTaskId);

    // 플랜 조회
    @Query("""
           SELECT DISTINCT w FROM WeeklyPlan w
           JOIN FETCH w.dailyPlans
           WHERE w.examTask.id = :examTaskId
           ORDER BY w.weekNumber ASC
           """)
    List<WeeklyPlan> findByExamTaskIdOrderByWeekNumberAsc(@Param("examTaskId") Long examTaskId);

    Optional<WeeklyPlan> findByExamTaskIdAndWeekNumber(Long examTaskId, Integer weekNumber);
}
