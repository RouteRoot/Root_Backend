package com.root.root.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<ExamResponseDto> searchExams(String keyword) {
        List<ExamData> exams = examDataRepository.findByExamNameWithSchedules(keyword);

        return exams.stream().map(e -> new ExamResponseDto(
                e.getExamCode(),
                e.getExamName(),
                e.getExamGroup(),
                e.getCategory(),
                e.getOrganization(),
                e.getDescription(),
                e.getSchedules().stream()
                        .map(s -> {
                            // 필기 D-Day 계산
                            Long docDDay = null;
                            if (s.getDocExamStart() != null) {
                                docDDay = ChronoUnit.DAYS.between(LocalDate.now(), s.getDocExamStart());
                            }

                            // 실기 D-Day 계산
                            Long pracDDay = null;
                            if (s.getPracExamStart() != null) {
                                pracDDay = ChronoUnit.DAYS.between(LocalDate.now(), s.getPracExamStart());
                            }

                            return new ExamResponseDto.ScheduleDto(
                                    s.getRound(),
                                    s.getDocExamStart(),
                                    docDDay,
                                    s.getPracExamStart(),
                                    pracDDay
                            );
                        }).collect(Collectors.toList())
        )).collect(Collectors.toList());
    }
}
