package com.root.root.repository;

import com.root.root.entity.ExamTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamTaskRepository extends JpaRepository<ExamTask, Long> {
    @Query("SELECT COUNT(e) > 0 FROM ExamTask e " + "JOIN e.phase p JOIN p.roadmap r JOIN r.user u " + "WHERE e.id = :examTaskId AND u.loginId = :loginId")
    boolean isOwnerOfTask(@Param("examTaskId") Long examTaskId, @Param("loginId") String loginId);
}
