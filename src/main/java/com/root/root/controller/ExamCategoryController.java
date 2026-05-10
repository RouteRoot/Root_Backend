package com.root.root.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.root.root.dto.ExamCategoryResponseDto;
import com.root.root.entity.ExamCategory;
import com.root.root.repository.ExamCategoryRepository;
import com.root.root.service.ExamCategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exam-categories")
@RequiredArgsConstructor
public class ExamCategoryController {

    private final ExamCategoryService examCategoryService;
    private final ExamCategoryRepository examCategoryRepository;

    @PostMapping
    public ResponseEntity<ExamCategory> createCategory(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("examCategoryName");
        
        Long parentId = null;
        if (request.get("parentId") != null) {
            parentId = Long.valueOf(request.get("parentId").toString());
        }

        ExamCategory category = new ExamCategory();
        category.setExamCategoryName(name);

        if (parentId != null) {
            ExamCategory parent = examCategoryRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("부모 카테고리를 찾을 수 없습니다."));
            category.setParentCategory(parent);
        }

        return ResponseEntity.ok(examCategoryRepository.save(category));
    }

    @GetMapping
    public ResponseEntity<List<ExamCategoryResponseDto>> getCategoryTree() {
        return ResponseEntity.ok(examCategoryService.getExamCategoryTree());
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<ExamCategory> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody Map<String, Object> request) {
        
        ExamCategory category = examCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("해당 카테고리를 찾을 수 없습니다. ID: " + categoryId));

        if (request.containsKey("examCategoryName")) {
            category.setExamCategoryName((String) request.get("examCategoryName"));
        }
        
        if (request.containsKey("parentId")) {
            Object parentIdObj = request.get("parentId");
            if (parentIdObj != null) {
                Long parentId = Long.valueOf(parentIdObj.toString());
                ExamCategory parent = examCategoryRepository.findById(parentId)
                        .orElseThrow(() -> new RuntimeException("부모 카테고리를 찾을 수 없습니다. ID: " + parentId));
                category.setParentCategory(parent);
            } else {
                category.setParentCategory(null);
            }
        }

        return ResponseEntity.ok(examCategoryRepository.save(category));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        ExamCategory category = examCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("삭제하려는 카테고리가 존재하지 않습니다. ID: " + categoryId));

        if (!category.getSubCategories().isEmpty()) {
            return ResponseEntity.badRequest().body("하위 카테고리가 존재하는 항목은 삭제할 수 없습니다. 하위 항목을 먼저 삭제하세요.");
        }
        
        examCategoryRepository.delete(category);
        return ResponseEntity.ok("카테고리 삭제 성공: " + categoryId);
    }
}