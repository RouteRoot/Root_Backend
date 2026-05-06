package com.root.root.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.root.root.entity.ExamData;

@Repository
public interface ExamDataRepository extends JpaRepository<ExamData, String> {

    @Modifying
    @Query("UPDATE ExamData e SET e.viewCount = e.viewCount + 1 WHERE e.examCode = :examCode")
    int incrementViewCount(@Param("examCode") String examCode);

    @Query(
        value = "SELECT DISTINCT e FROM ExamData e LEFT JOIN FETCH e.schedules s WHERE e.examName LIKE %:keyword%",
        countQuery = "SELECT count(e) FROM ExamData e WHERE e.examName LIKE %:keyword%"
    )
    Page<ExamData> findByExamNameWithSchedules(@Param("keyword") String keyword, Pageable pageable);

    @Query(
        value = "SELECT DISTINCT e FROM ExamData e LEFT JOIN FETCH e.schedules",
        countQuery = "SELECT count(e) FROM ExamData e"
    )
    Page<ExamData> findAllWithSchedules(Pageable pageable);
}
