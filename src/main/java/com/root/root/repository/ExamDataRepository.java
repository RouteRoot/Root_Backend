package com.root.root.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.root.root.entity.ExamData;

@Repository
public interface ExamDataRepository extends JpaRepository<ExamData, String> {

    @Query("SELECT DISTINCT e FROM ExamData e "
            + "LEFT JOIN FETCH e.schedules s "
            + "WHERE e.examName LIKE %:keyword% "
            + "ORDER BY s.docExamStart ASC")
    List<ExamData> findByExamNameWithSchedules(@Param("keyword") String keyword);
}
