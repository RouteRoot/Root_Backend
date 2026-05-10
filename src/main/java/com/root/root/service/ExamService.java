package com.root.root.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.root.root.dto.ExamResponseDto;
import com.root.root.entity.ExamCategory;
import com.root.root.entity.ExamData;
import com.root.root.repository.ExamCategoryRepository;
import com.root.root.repository.ExamDataRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {
    private final FileStorageService fileStorageService;
    private final ExamDataRepository examDataRepository;
    private final ExamCategoryRepository examCategoryRepository;
    
    public Page<ExamResponseDto> getAllExams(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("examCode").ascending());
        Page<ExamData> exams = examDataRepository.findAllWithSchedules(pageable);
        
        return exams.map(this::convertToDto);
    }

    public Page<ExamResponseDto> searchExams(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("examCode").ascending());
        Page<ExamData> exams = examDataRepository.findByExamNameWithSchedules(keyword, pageable);
        
        // 따로 빼둔 변환 로직(convertToDto)을 사용하여 코드가 매우 깔끔해집니다!
        return exams.map(this::convertToDto); 
    }

    private ExamResponseDto convertToDto(ExamData e) {
        return new ExamResponseDto(
                e.getExamCode(),
                e.getExamName(),
                e.getExamGroup(),
                e.getCategory(),
                e.getOrganization(),
                e.getDescription(),
                e.getImageUrl(),
                e.getOfficialUrl(),
                e.getIsActive(),
                e.getViewCount(),
                e.getSchedules().stream()
                        .map(s -> {
                            Long docDDay = null;
                            if (s.getDocExamStart() != null) {
                                docDDay = ChronoUnit.DAYS.between(LocalDate.now(), s.getDocExamStart());
                            }
                            Long pracDDay = null;
                            if (s.getPracExamStart() != null) {
                                pracDDay = ChronoUnit.DAYS.between(LocalDate.now(), s.getPracExamStart());
                            }
                            return new ExamResponseDto.ScheduleDto(
                                    s.getRound(), s.getDocExamStart(), docDDay, s.getPracExamStart(), pracDDay
                            );
                        }).collect(Collectors.toList())
        );
    }

    @Transactional
    public ExamData saveOrUpdateExam(ExamData examData, Long examCategoryId) {
        if (examData.getIsActive() == null) {
            examData.setIsActive(true);
        }
        if (examCategoryId != null) {
            ExamCategory category = examCategoryRepository.findById(examCategoryId)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다. ID: " + examCategoryId));
            examData.setExamCategory(category);
        }

        return examDataRepository.save(examData);
    }

    //특정 필드(설명, 카테고리 등)만 부분 수정
    @Transactional
    public ExamData patchExam(String examCode, ExamData updateInfo, Long examCategoryId) {
        ExamData exam = examDataRepository.findById(examCode)
                .orElseThrow(() -> new RuntimeException("해당 자격증을 찾을 수 없습니다: " + examCode));

        if (examCategoryId != null) {
            ExamCategory category = examCategoryRepository.findById(examCategoryId)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다. ID: " + examCategoryId));
            exam.setExamCategory(category);
        }

        if (updateInfo.getExamName() != null) exam.setExamName(updateInfo.getExamName());
        if (updateInfo.getCategory() != null) exam.setCategory(updateInfo.getCategory());
        if (updateInfo.getExamGroup() != null) exam.setExamGroup(updateInfo.getExamGroup());
        if (updateInfo.getOrganization() != null) exam.setOrganization(updateInfo.getOrganization());
        if (updateInfo.getDescription() != null) exam.setDescription(updateInfo.getDescription());
        if (updateInfo.getOfficialUrl() != null) exam.setOfficialUrl(updateInfo.getOfficialUrl());
        if (updateInfo.getIsActive() != null) exam.setIsActive(updateInfo.getIsActive());
        if (updateInfo.getImageUrl() != null) exam.setImageUrl(updateInfo.getImageUrl());

        return exam;
    }

    //자격증 정보 삭제
    @Transactional
    public void deleteExam(String examCode) {
        // 삭제 전 데이터 존재 여부 검증
        if (!examDataRepository.existsById(examCode)) {
            throw new RuntimeException("삭제하려는 자격증이 존재하지 않습니다: " + examCode);
        }
        examDataRepository.deleteById(examCode);
    }

    @Transactional
    public ExamData getExamDetailWithViewCount(String examCode) {
        // 조회수 1 증가 
        examDataRepository.incrementViewCount(examCode);
        
        // 업데이트된 최신 정보 조회 후 반환
        return examDataRepository.findById(examCode)
                .orElseThrow(() -> new RuntimeException("해당 자격증을 찾을 수 없습니다: " + examCode));
    }

    @Transactional
    public ExamData updateExamImage(String examCode, MultipartFile file) {
    ExamData exam = examDataRepository.findById(examCode)
            .orElseThrow(() -> new RuntimeException("해당 자격증을 찾을 수 없습니다: " + examCode));
    String imageUrl = fileStorageService.storeSingleImage(file); 

    exam.setImageUrl(imageUrl); 
    return exam;
    }

    @Transactional(readOnly = true)
    public Page<ExamResponseDto> getExamsByCategory(Long categoryId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("examName").ascending());
    
    ExamCategory category = examCategoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. ID: " + categoryId));


    List<Long> targetIds = new ArrayList<>();
    targetIds.add(category.getExamCategoryId()); 

    if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
    for (ExamCategory child : category.getSubCategories()) {
        targetIds.add(child.getExamCategoryId());
    }
}

    return examDataRepository.findByExamCategoryIdIn(targetIds, pageable)
            .map(this::convertToDto);
    }
}
