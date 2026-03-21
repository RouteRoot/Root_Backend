package com.root.root.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.root.root.entity.ExamData;

@Repository
public interface ExamDataRepository extends JpaRepository<ExamData, Long> {
}