package com.root.root.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.root.root.dto.ExamResponseDto;
import com.root.root.entity.ExamData;
import com.root.root.entity.ExamSchedule;
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

    // 1. 조회 및 검색
    // ===============

    @GetMapping("/search")
    public ResponseEntity<Page<ExamResponseDto>> searchExams(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page, // 기본 0페이지 (첫 페이지)
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ExamResponseDto> result = examService.searchExams(keyword, page, size);
        return ResponseEntity.ok(result);
    }

    // 자격증 상세 조회
    @GetMapping("/{examCode}")
    public ResponseEntity<ExamData> getExamByCode(@PathVariable String examCode) {
        ExamData exam = examService.getExamDetailWithViewCount(examCode);
        
        return ResponseEntity.ok(exam);
    }

    // DB 데이터 전체 조회
    @GetMapping("/all")
    public ResponseEntity<Page<ExamResponseDto>> getAllExams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ExamResponseDto> result = examService.getAllExams(page, size);
        return ResponseEntity.ok(result);
    }

    // 2. 수동 데이터 관리
    // ==================

    //자격증 관리
    
    //자격증 정보 수동 생성 및 전체 수정
    @PostMapping("/manual")
    public ResponseEntity<ExamData> createExamManual(@RequestBody ExamData examData) {
        return ResponseEntity.ok(examService.saveOrUpdateExam(examData));
    }

    //특정 필드(설명, 카테고리 등)만 부분 수정
    @PatchMapping("/{examCode}")
    public ResponseEntity<ExamData> updateExamPartially(
            @PathVariable String examCode, 
            @RequestBody ExamData updateInfo) {
        return ResponseEntity.ok(examService.patchExam(examCode, updateInfo));
    }
    
    //자격증 정보 삭제
    @DeleteMapping("/{examCode}")
    public ResponseEntity<String> deleteExam(@PathVariable String examCode) {
        examService.deleteExam(examCode);
        return ResponseEntity.ok("자격증 삭제 완료: " + examCode);
    }

    //자격증 일정 관리

    //자격증 일정 정보 수동 생성 및 전체 수정
    @PostMapping("/{examCode}/schedules")
    public ResponseEntity<ExamSchedule> addSchedule(
            @PathVariable String examCode, 
            @RequestBody ExamSchedule scheduleData) {
        return ResponseEntity.ok(examScheduleService.addManualSchedule(examCode, scheduleData));
    }

    // 특정 일정 정보 수정 (ID 기준)
    @PatchMapping("/schedules/{scheduleId}")
    public ResponseEntity<ExamSchedule> updateSchedule(
            @PathVariable Long scheduleId, 
            @RequestBody ExamSchedule updateInfo) {
        return ResponseEntity.ok(examScheduleService.patchSchedule(scheduleId, updateInfo));
    }

    // 특정 일정 삭제
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<String> deleteSchedule(@PathVariable Long scheduleId) {
        examScheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok("시험 일정 삭제 성공 (ID: " + scheduleId + ")");
    }

    // 3. 외부 API 연동 및 일정 수집
    // ============================

    // 외부 공공데이터 수집 API 추가
    @GetMapping("/fetch-external")
    public String fetch() {
        batchService.fetchAndSaveExams();
        return "데이터 수집 완료!";
    }

    @GetMapping("/test-fetch")
    public ResponseEntity<String> testFetchSchedules(@RequestParam String examCode) {
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
