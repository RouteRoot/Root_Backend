package com.root.root.repository;

import com.root.root.entity.DailyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {
    @Query("""
           SELECT d FROM DailyPlan d
           JOIN d.weeklyPlan w
           JOIN w.examTask e
           JOIN e.phase p
           JOIN p.roadmap r
           JOIN r.user u
           WHERE u.id = :userId
           """)
    List<DailyPlan> findAllByUserId(@Param("userId") Long userId);

    @Query("""
           SELECT d FROM DailyPlan d
           JOIN d.weeklyPlan w
           JOIN w.examTask e
           JOIN e.phase p
           JOIN p.roadmap r
           JOIN r.user u
           WHERE u.id = :userId AND e.id = :examTaskId
           """)
    List<DailyPlan> findByUserIdAndExamTaskId(@Param("userId") Long userId, @Param("examTaskId") Long examTaskId);

    @Query("SELECT d FROM DailyPlan d WHERE d.weeklyPlan.examTask.id = :examTaskId AND d.studyDate = :studyDate")
    Optional<DailyPlan> findByTaskAndDate(@Param("examTaskId") Long examTaskId, @Param("studyDate") LocalDate studyDate);
}
