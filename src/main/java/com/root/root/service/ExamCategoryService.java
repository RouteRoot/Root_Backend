package com.root.root.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.root.root.dto.ExamCategoryResponseDto;
import com.root.root.entity.ExamCategory;
import com.root.root.repository.ExamCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamCategoryService {

    private final ExamCategoryRepository examCategoryRepository;

    public List<ExamCategoryResponseDto> getExamCategoryTree() {
        List<ExamCategory> roots = examCategoryRepository.findAllRootCategoriesWithChildren();
        return roots.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ExamCategoryResponseDto convertToDto(ExamCategory category) {

        List<ExamCategoryResponseDto> subDtos = category.getSubCategories().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        long currentCount = category.getExams().size();
        long totalCount = currentCount + subDtos.stream()
                .mapToLong(ExamCategoryResponseDto::getExamCount)
                .sum();

        return ExamCategoryResponseDto.builder()
                .examCategoryId(category.getExamCategoryId())
                .examCategoryName(category.getExamCategoryName())
                .examCount(totalCount)
                .subCategories(subDtos)
                .build();
    }
}