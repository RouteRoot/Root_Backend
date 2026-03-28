package com.root.root.repository;

import com.root.root.entity.standard.StandardExam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StandardExamRepository extends JpaRepository<StandardExam, Long> {
    Optional<StandardExam> findByCertificationName(String certificationName);
}
