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
                e.getSchedules().stream()
                        (s -> {
                            Long dDay = null;
        if (s.getExamDate() != null) {
                 = ChronoUnit
                
                
        return new ExamResponseDto.ScheduleDto(
     

               s.getRound(),


                                    s.getExamDate(),
                                    dDay
 
                           );
                        }).collect(Collectors.toList())
        )).collect(Collectors.toList());
    }
}