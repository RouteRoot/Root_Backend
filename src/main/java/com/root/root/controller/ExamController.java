package com.root.root.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.root.root.dto.ExamResponseDto;
import com.root.root.entity.ExamData;
import com.root.root.repository.ExamDataRepository;
import com.root.root.service.ExamDataBatchService;
import com.root.root.service.ExamScheduleService;
import com.root.root.service.ExamService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamDataRepository examDataRepository;
    private final ExamDataBatchService batchService;
    private final ExamService examService;
    private final ExamScheduleService examScheduleService;

    // DB 데이터 전체 조회
    @GetMapping("/all")
    public List<ExamData> getAllExams() {
        return examDataRepository.findAll();
    }

    // 자격증 상세 조회
    @GetMapping("/{examCode}")
    public ExamData getExamByCode(@PathVariable String examCode) {
        return examDataRepository.findById(examCode)
                .orElseThrow(() -> new RuntimeException("해당 자격증을 찾을 수 없습니다."));
    }

    // 외부 공공데이터 수집 API 추가
    @GetMapping("/fetch-external")
    public String fetch() {
        batchService.fetchAndSaveExams();
        return "데이터 수집 완료!";
    }

    @GetMapping("/search")
    public ResponseEntity<List<ExamResponseDto>> searchExams(@RequestParam String keyword) {
        List<ExamResponseDto> results = examService.searchExams(keyword);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/test-fetch")
    public ResponseEntity<String> testFetchSchedules(@RequestParam String examCode) {
        // 서비스 로직 실행
        examScheduleService.fetchAndSaveSchedules(examCode);
        return ResponseEntity.ok(examCode + " 자격증 일정 데이터 수집 요청이 완료되었습니다. 콘솔과 DB를 확인해보세요!");
    }
    @GetMapping("/test-fetch-all")
    public ResponseEntity<String> fetchAllSchedules() {
        new Thread(() -> {
            examScheduleService.fetchAllSchedules();
        }).start();
        
        return ResponseEntity.ok("전체 자격증 일정 수집이 백그라운드에서 시작되었습니다! VS Code 콘솔 로그를 확인해주세요. (약 3~5분 소요)");
    }
}
