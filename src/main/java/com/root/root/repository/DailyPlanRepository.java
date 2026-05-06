package com.root.root.repository;

import com.root.root.entity.DailyPlan;
import com.root.root.entity.WeeklyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT d FROM DailyPlan d WHERE d.weeklyPlan.examTask.id = :examTaskId AND d.isCompleted = false ORDER BY d.studyDate ASC")
    List<DailyPlan> findIncompletePlansByTaskId(@Param("examTaskId") Long examTaskId);

    @Modifying
    @Query("DELETE FROM DailyPlan d WHERE d.weeklyPlan.examTask.id = :examTaskId AND d.studyDate >= :today")
    void deletePlansFromToday(@Param("examTaskId") Long examTaskId, @Param("today") LocalDate today);

    @Query("SELECT w FROM WeeklyPlan w JOIN w.dailyPlans d WHERE w.examTask.id = :examTaskId AND d.studyDate = :today")
    java.util.Optional<WeeklyPlan> findWeeklyPlanByDate(@Param("examTaskId") Long examTaskId, @Param("today") LocalDate today);

    @Modifying
    @Query("DELETE FROM WeeklyPlan w WHERE w.examTask.id = :examTaskId AND NOT EXISTS (SELECT d FROM DailyPlan d WHERE d.weeklyPlan = w)")
    void deleteEmptyWeeklyPlans(@Param("examTaskId") Long examTaskId);
}
