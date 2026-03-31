package com.root.root.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.root.root.entity.ExamData;
import com.root.root.entity.ExamSchedule;
import com.root.root.repository.ExamDataRepository;
import com.root.root.repository.ExamScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamScheduleService {

    private final ExamScheduleRepository examScheduleRepository;
    private final ExamDataRepository examDataRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openapi.service-key}")
    private String serviceKey;

    @Transactional
    public void fetchAndSaveSchedules(String examCode) {

        ExamData examData = examDataRepository.findById(examCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자격증 코드입니다: " + examCode));


        String url = "http://openapi.q-net.or.kr/api/service/rest/InquiryTestInformationNTQSVC/getJMList"
                + "?jmCd=" + examCode
                + "&ServiceKey=" + serviceKey
                + "&_type=json";

        try {
            // API 호출
            String response = restTemplate.getForObject(url, String.class);

            // JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isMissingNode()) {
                log.warn("API 응답에 일정 데이터가 없습니다. examCode: {}", examCode);
                return;
            }

            if (items.isArray()) {
                for (JsonNode item : items) {
                    saveSchedule(examData, item);
                }
            } else {
                saveSchedule(examData, items);
            }

            log.info("자격증 일정 연동 완료: {}", examData.getExamName());

        } catch (Exception e) {
            log.error("API 연동 중 오류 발생 examCode: {}", examCode, e);
        }
    }

    private void saveSchedule(ExamData examData, JsonNode item) {
        String round = item.path("implplannm").asText();

        // 1. 필기 일정 파싱
        LocalDate docRegStart = parseDate(item.path("docregstartdt").asText()); 
        LocalDate docRegEnd = parseDate(item.path("docregenddt").asText());     
        LocalDate docExamStart = parseDate(item.path("docexamstartdt").asText());
        LocalDate docPassDate = parseDate(item.path("docpassdt").asText());   

        // 2. 실기 일정 파싱
        LocalDate pracRegStart = parseDate(item.path("pracregstartdt").asText()); 
        LocalDate pracRegEnd = parseDate(item.path("pracregenddt").asText());     
        LocalDate pracExamStart = parseDate(item.path("pracexamstartdt").asText());
        LocalDate pracPassDate = parseDate(item.path("pracpassdt").asText());

        ExamSchedule schedule = examScheduleRepository.findByExamDataAndRound(examData, round)
                .orElse(new ExamSchedule());

        schedule.setExamData(examData);
        schedule.setRound(round);
        
        // 필기 데이터 삽입
        schedule.setDocRegStart(docRegStart);
        schedule.setDocRegEnd(docRegEnd);
        schedule.setDocExamStart(docExamStart);
        schedule.setDocPassDate(docPassDate);
        
        // 실기 데이터 삽입
        schedule.setPracRegStart(pracRegStart);
        schedule.setPracRegEnd(pracRegEnd);
        schedule.setPracExamStart(pracExamStart);
        schedule.setPracPassDate(pracPassDate);

        examScheduleRepository.save(schedule);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank() || "null".equalsIgnoreCase(dateStr)) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    public void fetchAllSchedules() {
        log.info("전체 자격증(약 600개) 일정 데이터 수집을 시작합니다...");
        List<ExamData> allExams = examDataRepository.findAll();
        
        int successCount = 0;
        
        for (ExamData exam : allExams) {
            try {
                fetchAndSaveSchedules(exam.getExamCode());
                successCount++;
                
                // 공공데이터 API 서버 차단(IP Block) 방지를 위해 0.2초씩 대기
                Thread.sleep(200); 
            } catch (InterruptedException e) {
                log.error("수집 중단됨", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("개별 수집 실패 - examCode: {}", exam.getExamCode(), e);
            }
        }
        
        log.info("전체 자격증 일정 수집 완료! 총 {}개 조회 시도됨", successCount);
    }
}
