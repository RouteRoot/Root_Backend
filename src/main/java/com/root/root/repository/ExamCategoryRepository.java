package com.root.root.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.root.root.entity.ExamCategory;

@Repository
public interface ExamCategoryRepository extends JpaRepository<ExamCategory, Long> {
    @Query("SELECT DISTINCT c FROM ExamCategory c LEFT JOIN FETCH c.subCategories WHERE c.parentCategory IS NULL")
    List<ExamCategory> findAllRootCategoriesWithChildren();
}