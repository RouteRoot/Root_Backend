package com.root.root.service;

import com.root.root.dto.PlanCreateRequestDto;
import com.root.root.dto.PlanDetailResponseDto;
import com.root.root.dto.PlanResponseDto;
import com.root.root.dto.PlanTabResponseDto;
import com.root.root.entity.DailyPlan;
import com.root.root.entity.ExamTask;
import com.root.root.entity.User;
import com.root.root.entity.WeeklyPlan;
import com.root.root.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final ObjectMapper objectMapper; // JSON 파싱용
    private final LLMService llmService; // LLM 호출 클래스
    private final UserRepository userRepository;
    private final ExamTaskRepository examTaskRepository;
    private final WeeklyPlanRepository weeklyPlanRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final StandardExamRepository standardExamRepository;

    @Transactional
    public List<WeeklyPlan> generateAndSavePlan(String loginId, PlanCreateRequestDto request) {
        // 유저 검증
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
        ExamTask examTask = examTaskRepository.findById(request.getExamTaskId()).orElseThrow(() -> new IllegalArgumentException("해당 자격증을 찾을 수 없습니다."));

        // 보안 로직
        validateTaskOwnership(examTask.getId(), loginId);

        // 기존 학습 플랜 존재 여부 검증
        boolean hasExistingPlan = weeklyPlanRepository.existsByExamTaskId(examTask.getId());
        if (hasExistingPlan) {
            throw new IllegalStateException("이미 해당 자격증에 대한 학습 플랜이 존재합니다. 새로운 플랜을 생성할 수 없습니다.");
        }

        // D-Day 역산 로직
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));
        long totalDays = ChronoUnit.DAYS.between(today, request.getExamDate());

        // 남은 주차 계산(올림 처리)
        long remainingWeeks = (totalDays / 7) + (totalDays % 7 > 0 ? 1 : 0);

        // 마지막 주차의 실제 일수 계산
        long lastWeekDays = (totalDays % 7 == 0) ? 7 : (totalDays % 7);

        // 요일 구하기
        String startDayOfWeek = today.getDayOfWeek().name().substring(0, 3);

        // 요일별 스케줄 순서 가이드 생성
        StringBuilder scheduleInfo = new StringBuilder();
        java.util.Map<String, Integer> weeklyMap = request.getWeeklySchedule() != null ? request.getWeeklySchedule() : java.util.Map.of();

        scheduleInfo.append("[전체 주차별 캘린더 매핑 가이드 (반드시 아래 명시된 시간과 휴식 여부대로 JSON을 생성할 것]\n");
        int currentDayCount = 0;
        for (int w = 1; w <= remainingWeeks; w++) {
            scheduleInfo.append(String.format("- %d주차: ", w));

            // 마지막 주차는 7일이 아닐 수 있으므로 일수 계산
            int daysInThisWeek = (w == remainingWeeks) ? (int) lastWeekDays : 7;

            for (int d = 1; d <= daysInThisWeek; d++) {
                String dayName = today.plusDays(currentDayCount).getDayOfWeek().name().substring(0, 3);
                int hours = weeklyMap.getOrDefault(dayName, 0);
                String status = (hours == 0) ? "휴식" : hours + "시간";

                scheduleInfo.append(String.format("Day %d(%s), ", d, status));
                currentDayCount++;
            }
            // 마지막 쉼표 제거 및 줄바꿈
            scheduleInfo.setLength(scheduleInfo.length() - 2);
            scheduleInfo.append("\n");
        }

        // 서술형 입력창 비어있을 경우 디폴트 데이터
        String personalStory = request.getPersonalStory() != null && !request.getPersonalStory().trim().isEmpty() ? request.getPersonalStory() : "특별한 약점이나 고민은 없으며, 전체적인 흐름에 따라 학습하길 원함.";

        // 표준 데이터(공식 목차) 조회
        List<String> standardSyllabusList = new ArrayList<>();
        StringBuilder syllabusPrompt = new StringBuilder();

        standardExamRepository.findByCertificationName(request.getCertificationName()).ifPresent(exam -> {
            syllabusPrompt.append("\n[Standard Syllabus: 반드시 포함해야 할 공식 출제 기준/목차]\n");
            exam.getSyllabuses().forEach(syllabus -> {
                standardSyllabusList.add(syllabus.getSubjectName());
                syllabusPrompt.append("- ").append(syllabus.getSubjectName()).append("\n");
            });
        });

        // 프롬프트
        String prompt = String.format("""
                        [Role]
                        너는 커리어 전문 1:1 학습 플래너 AI야. 너의 임무는 사용자가 선택한 특정 자격증/어학 시험의 공식 출제 기준(목차)을 바탕으로 시험일(D-Day)까지 남은 기간과 사용자의 요일별 가용 시간, 개인적인 고민에 맞춰 완벽하게 분배된 일일/주간 학습 커리큘럼을 설계하는 거야.
                        
                        [User Status & Target]
                        목표 시험(Task): %s
                        시험일까지 남은 기간: 총 %d일 (약 %d주)
                        학습 시작 요일(오늘): %s
                        실력 자가 진단: %s
                        
                        %s
                        
                        [User's Personal Story (강점/약점/고민)]
                        "%s"
                        
                        %s
                        
                        [Rules]
                        1. 학습의 1주차 1일차(dayNumber: 1)는 반드시 학습 시작 요일(%s)로 간주한다. 이후 날짜가 지남에 따라 요일이 순환됨을 인지하고 dayNumber에 맞춰 위에 제공된 [전체 주차별 캘린더 매핑 가이드]를 그대로 보고 똑같이 반영하라.
                        2. 가이드에 '휴식'으로 적혀있는 Day는 estimatedHours를 0, isRest를 true로 설정하고, 'topic'에 '휴식 및 자율 보충'이라고 적어라. '시간'이 적혀있는 Day는 해당 시간을 estimatedHours에 할당하고 진도를 나가라.
                        3. 사용자가 작성한 [User's Personal Story]를 철저하게 분석하라. 사용자가 약점이라고 언급한 파트는 개념 학습 시간을 1.5배로 배분하거나 복습 일정을 추가하라.
                        4. 일일 계획의 'description' 란에는 단순히 공부할 내용을 나열하는 것을 넘어, 사용자의 고민을 반영한 1:1 멘토링 코멘트를 문장 형태로 포함하라.
                        5. [Standard Syllabus]가 제공된 경우, 이 목차는 공식 주관처의 절대적인 출제 기준이다. 진도를 나가는 날은 오직 이 목차 내의 주제들을 조합하고 분배하여 작성하라.
                        6. 단, 진도를 나가는 것 외에 사용자의 합격을 위해 '오답 노트 복습', '전체 복습', '기출문제 풀이', '실전 모의고사 풀이'와 같은 일정은 반드시 필요하다. 특히 시험 직전 주차에는 이 실전 대비 일정을 집중적으로 배치하라.
                        7. [목표 시험]의 실제 출제 기준과 목차를 바탕으로 학습 분량을 쪼개라.
                        8. 만약 [목표 시험]이 필기와 실기로 구분되는 시험(예: 정보처리기사, 컴퓨터활용능력 등)이라면, 입력된 남은 기간은 '필기 시험' 기준이다. 따라서 반드시 '필기' 출제 기준과 목차만을 바탕으로 학습 플랜을 작성하라.
                        9. [실력 자가 진단]을 고려해라. '하(노베이스)'라면 개념 이해에 시간을 더 배분하고, '상'이라면 기출문제 풀이와 실전 모의고사 비중을 높여라.
                        10. [시험일까지 남은 기간]과 [요일별 확보 가능한 학습 시간]을 엄격하게 지켜라. 하루에 할당된 학습량은 반드시 할당된 시간 안에 소화할 수 있는 분량이어야 한다.
                        11. 총 %d주차 커리큘럼이다. 단 마지막 %d주차는 %d일까지만 존재하는 주차이다. 따라서 마지막 %d주차의 dailyPlans는 dayNumber 1부터 %d까지만 생성하고, 시험일 이후의 플랜은 절대로 생성하지 마라.
                        12. 사용자가 하루 단위로 체크리스트를 달성하며 성취감을 느낄 수 있도록, 뭉뚱그린 계획이 아닌 "어떤 파트의 어떤 개념을 공부할지" 구체적으로 작성하라.
                        13. 반드시 1주차부터 마지막 %d주차까지 모든 주차(총 %d개)의 weeklyPlan을 배열에 빠짐없이 생성하라. 절대로 1주차만 생성하고 응답을 종료하거나 내용을 중략하지 마라.
                        14. **[Critical Rule]** 너의 답변은 시스템이 곧바로 파싱해야 하므로 반드시 아래의 JSON 규격으로만 출력해야 한다. 마크다운 기호(```json), 인삿말, 부가 설명 등은 절대 포함하지 말고 오직 JSON 텍스트만 반환하라. 스마트 따옴표 금지, 숫자는 문자열이 아닌 정수형으로 출력, JSON 외 추가 텍스트 절대 금지. 
                        
                        [Output JSON Format]
                        {
                            "targetExam": "String (목표 시험명)",
                            "totalWeeks": "Integer (총 학습 주차)",
                            "weeklyPlans": [
                                {
                                    "weekNumber": "Integer (주차 번호, 예: 1)",
                                    "weeklyGoal": "String (해당 주차의 핵심 달성 목표)",
                                    "dailyPlans": [
                                        {
                                            "dayNumber": "Integer (일차 번호, 1-7)",
                                            "topic": "String (오늘 공부할 핵심 목차/주제. 휴식일인 경우 '휴식 및 자율 보충')",
                                            "description": "String (오늘 학습해야 할 세부 내용과 공부 방법 포인트. 휴식일인 경우 짧은 응원 메시지)",
                                            "estimatedHours": "Integer (오늘 할당된 학습 시간. 휴식일이면 0)",
                                            "isRest": "Boolean (휴식일 여부. 학습일이면 false, 휴식일이면 true)"
                                        }
                                    ]
                                }
                            ]
                        }
                        """,
                request.getCertificationName(), totalDays, remainingWeeks, startDayOfWeek, request.getSkillLevel(), scheduleInfo.toString(), personalStory, syllabusPrompt.toString(), startDayOfWeek, remainingWeeks, remainingWeeks, lastWeekDays, remainingWeeks, lastWeekDays, remainingWeeks, remainingWeeks
        );

        // Auto-Retry 로직(3회)
        int maxRetries = 3;
        int attempt = 0;
        PlanResponseDto responseDto = null;

        while (attempt < maxRetries) {
            attempt++;
            try {
                // LLM 호출해서 JSON 받아오기
                String jsonResponse = llmService.requestToLlm(prompt);
                // JSON -> DTO 파싱
                responseDto = objectMapper.readValue(jsonResponse, PlanResponseDto.class);

                break;
            } catch (Exception e) {
                if (attempt >= maxRetries) {
                    throw new RuntimeException("LLM 응답 처리 중 오류가 발생했습니다: " + e.getMessage());
                }
            }
        }

        try {
            List<WeeklyPlan> savedPlans = new ArrayList<>();

            for (PlanResponseDto.WeeklyPlanDto dtoWeek : responseDto.getWeeklyPlans()) {
                WeeklyPlan weeklyPlan = new WeeklyPlan();
                weeklyPlan.setExamTask(examTask);
                weeklyPlan.setWeekNumber(dtoWeek.getWeekNumber());
                weeklyPlan.setWeeklyGoal(dtoWeek.getWeeklyGoal());

                for (PlanResponseDto.DailyPlanDto dtoDay : dtoWeek.getDailyPlans()) {
                    DailyPlan dailyPlan = new DailyPlan();
                    dailyPlan.setWeeklyPlan(weeklyPlan);
                    dailyPlan.setDayNumber(dtoDay.getDayNumber());
                    dailyPlan.setTopic(dtoDay.getTopic());
                    dailyPlan.setDescription(dtoDay.getDescription());
                    dailyPlan.setEstimatedHours(dtoDay.getEstimatedHours());
                    dailyPlan.setRest(dtoDay.getIsRest() != null ? dtoDay.getIsRest() : false);
                    dailyPlan.setCompleted(false);

                    // 날짜 계산 로직
                    long daysToAdd = ((dtoWeek.getWeekNumber() - 1) * 7L) + (dtoDay.getDayNumber() - 1);
                    dailyPlan.setStudyDate(today.plusDays(daysToAdd));

                    weeklyPlan.getDailyPlans().add(dailyPlan);
                }
                // Cascade
                savedPlans.add(weeklyPlanRepository.save(weeklyPlan));
            }
            examTask.setStatus("IN_PROGRESS");

            weeklyPlanRepository.flush();
            return savedPlans;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("LLM 학습 플랜 JSON 파싱 또는 DB 저장 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    @Transactional
    public PlanDetailResponseDto getStudyPlan(String loginId, Long examTaskId){
        // 유저 검증
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 자격증 존재 여부 검증
        ExamTask examTask = examTaskRepository.findById(examTaskId).orElseThrow(() -> new IllegalArgumentException("해당 자격증을 찾을 수 없습니다."));

        // 보안 로직
        validateTaskOwnership(examTask.getId(), loginId);

        // 플랜 조회
        List<WeeklyPlan> weeklyPlans = weeklyPlanRepository.findByExamTaskIdOrderByWeekNumberAsc(examTaskId);

        if(weeklyPlans.isEmpty()){
            throw new IllegalArgumentException("해당 자격증에 대한 학습 플랜이 아직 생성되지 않았습니다.");
        }
        return PlanDetailResponseDto.builder().status(examTask.getStatus()).weeklyPlans(weeklyPlans).build();
    }

    @Transactional
    public void deleteStudyPlan(String loginId, Long examTaskId){
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
        ExamTask examTask = examTaskRepository.findById(examTaskId).orElseThrow(() -> new IllegalArgumentException("해당 자격증을 찾을 수 없습니다."));

        validateTaskOwnership(examTask.getId(), loginId);

        if(!"IN_PROGRESS".equals(examTask.getStatus())){
            throw new IllegalArgumentException("이미 완료된 건 삭제 불가능");
        }

        List<WeeklyPlan> plansToDelete = weeklyPlanRepository.findByExamTaskIdOrderByWeekNumberAsc(examTaskId);
        if(plansToDelete.isEmpty()){
            throw new IllegalArgumentException("해당 플랜이 존재하지 않음");
        }
        weeklyPlanRepository.deleteAll(plansToDelete);

        examTask.setStatus("NOT_STARTED");
    }

    @Transactional
    public boolean togglePlanCompletion(String loginId, Long dailyPlanId){
        // 유저 검증
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 해당 DailyPlan 탐색
        DailyPlan dailyPlan = dailyPlanRepository.findById(dailyPlanId).orElseThrow(() -> new IllegalArgumentException("해당 학습 플랜을 찾을 수 없습니다."));

        // 보안 로직
        validateTaskOwnership(dailyPlan.getWeeklyPlan().getExamTask().getId(), loginId);

        // Toggle
        boolean currentStatus = dailyPlan.isCompleted();
        dailyPlan.setCompleted(!currentStatus);

        // 변경된 상태값 반환
        return dailyPlan.isCompleted();
    }

    @Transactional(readOnly = true)
    public List<PlanTabResponseDto> getMyPlanTabs(String loginId){
        List<ExamTask> activeTasks = examTaskRepository.findTasksWithPlansByUserLoginId(loginId);

        return activeTasks.stream().map(task -> PlanTabResponseDto.builder().examTaskId(task.getId()).taskName(task.getTaskName()).build()).toList();
    }

    // 보안 로직
    private void validateTaskOwnership(Long examTaskId, String loginId){
        if(!examTaskRepository.isOwnerOfTask(examTaskId, loginId)){
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }
    }
}
