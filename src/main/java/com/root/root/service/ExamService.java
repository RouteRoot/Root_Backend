package com.root.root.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.root.root.dto.ExamResponseDto;
import com.root.root.entity.ExamData;
import com.root.root.repository.ExamDataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

    private final ExamDataRepository examDataRepository;

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
    public ExamData saveOrUpdateExam(ExamData examData) {
        if (examData.getIsActive() == null) {
            examData.setIsActive(true);
        }
        return examDataRepository.save(examData);
    }

    //특정 필드(설명, 카테고리 등)만 부분 수정
    @Transactional
    public ExamData patchExam(String examCode, ExamData updateInfo) {
        ExamData exam = examDataRepository.findById(examCode)
                .orElseThrow(() -> new RuntimeException("해당 자격증을 찾을 수 없습니다: " + examCode));

        if (updateInfo.getExamName() != null) exam.setExamName(updateInfo.getExamName());
        if (updateInfo.getCategory() != null) exam.setCategory(updateInfo.getCategory());
        if (updateInfo.getExamGroup() != null) exam.setExamGroup(updateInfo.getExamGroup());
        if (updateInfo.getOrganization() != null) exam.setOrganization(updateInfo.getOrganization());
        if (updateInfo.getDescription() != null) exam.setDescription(updateInfo.getDescription());

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
}
