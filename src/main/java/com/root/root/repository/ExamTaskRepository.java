package com.root.root.repository;

import com.root.root.entity.ExamTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamTaskRepository extends JpaRepository<ExamTask, Long> {
}
