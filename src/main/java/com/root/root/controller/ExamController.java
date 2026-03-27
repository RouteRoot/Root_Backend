package com.root.root.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.root.root.entity.ExamData;
import com.root.root.repository.ExamDataRepository;
import com.root.root.service.ExamDataBatchService;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamDataRepository examDataRepository;
    private final ExamDataBatchService batchService;

    public ExamController(ExamDataRepository examDataRepository, ExamDataBatchService batchService) {
        this.examDataRepository = examDataRepository;
        this.batchService = batchService;
    }

    // DB 데이터 전체 조회
    @GetMapping("/all")
    public List<ExamData> getAllExams() {
        return examDataRepository.findAll();
    }

    // 자격증 상세 조회
    @GetMapping("/{id}")
    public ExamData getExamById(@PathVariable Long id) {
        return examDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 자격증을 찾을 수 없습니다."));
    }

    // 외부 공공데이터 수집 API 추가
    @GetMapping("/fetch-external")
    public String fetch() {
        batchService.fetchAndSaveExams();
        return "데이터 수집 완료!";
    }
}