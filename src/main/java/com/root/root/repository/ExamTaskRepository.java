package com.root.root.repository;

import com.root.root.entity.ExamTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamTaskRepository extends JpaRepository<ExamTask, Long> {
    @Query("""
           SELECT COUNT(e) > 0 FROM ExamTask e
           JOIN e.phase p JOIN p.roadmap r JOIN r.user u
           WHERE e.id = :examTaskId AND u.loginId = :loginId
           """)
    boolean isOwnerOfTask(@Param("examTaskId") Long examTaskId, @Param("loginId") String loginId);

    @Query("""
           SELECT e FROM ExamTask e
           JOIN e.phase p JOIN p.roadmap r
           WHERE r.user.loginId = :loginId
           AND e.status = 'IN_PROGRESS'
           ORDER BY e.id ASC
           """)
    List<ExamTask> findTasksWithPlansByUserLoginId(@Param("loginId") String loginId);
}
