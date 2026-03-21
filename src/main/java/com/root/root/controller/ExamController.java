package com.root.root.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.root.root.entity.ExamData;
import com.root.root.repository.ExamDataRepository;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamDataRepository examDataRepository;

    public ExamController(ExamDataRepository examDataRepository) {
        this.examDataRepository = examDataRepository;
    }

    //테스트 API
    @GetMapping("/mock")
    public List<ExamData> getMockExams() {
        return Arrays.asList(
                ExamData.builder()
                        .id(1L)
                        .examName("정보처리기사 필기")
                        .category("IT")
                        .organization("한국산업인력공단")
                        .description("소프트웨어 설계, 개발, 데이터베이스 구축 등")
                        .build(),
                ExamData.builder()
                        .id(2L)
                        .examName("SQLD")
                        .category("IT")
                        .organization("한국데이터산업진흥원")
                        .description("데이터 모델링 및 SQL 활용 능력")
                        .build()
        );
    }
}
