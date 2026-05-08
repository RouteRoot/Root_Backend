package com.root.root.service;

import com.root.root.dto.*;
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
import java.time.format.DateTimeFormatter;
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

        examTask.setCertificationName(request.getCertificationName());
        examTask.setExamDate(request.getExamDate());
        examTask.setWeeklySchedule(request.getWeeklySchedule() != null ? request.getWeeklySchedule() : java.util.Map.of());
        examTask.setSkillLevel(request.getSkillLevel() != null ? request.getSkillLevel() : "정보 없음");
        examTask.setPersonalStory(request.getPersonalStory() != null && !request.getPersonalStory().trim().isEmpty() ? request.getPersonalStory() : "전체적인 흐름에 따라 학습하길 원함.");

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
        PlanResponseDto responseDto = requestToLlmWithRetry(prompt);

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
    public void migrateDailyPlan(String loginId, Long dailyPlanId, LocalDate targetDate){
        DailyPlan sourcePlan = dailyPlanRepository.findById(dailyPlanId).orElseThrow(() -> new IllegalArgumentException("해당 학습 플랜을 찾을 수 없습니다."));
        Long examTaskId = sourcePlan.getWeeklyPlan().getExamTask().getId();
        validateTaskOwnership(examTaskId, loginId);

        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));
        if(targetDate.isBefore(today)){
            throw new IllegalArgumentException("과거 날짜로는 플랜을 이동할 수 없습니다.");
        }

        DailyPlan targetPlan = dailyPlanRepository.findByTaskAndDate(examTaskId, targetDate).orElse(null);

        String formattedTargetDate = targetDate.format(DateTimeFormatter.ofPattern("M월 d일"));

        if(targetPlan == null){
            DailyPlan newPlan = new DailyPlan();
            newPlan.setWeeklyPlan(sourcePlan.getWeeklyPlan());
            newPlan.setDayNumber(sourcePlan.getDayNumber());
            newPlan.setStudyDate(targetDate);
            newPlan.setTopic(sourcePlan.getTopic());
            newPlan.setDescription(sourcePlan.getDescription());
            newPlan.setEstimatedHours(sourcePlan.getEstimatedHours());
            newPlan.setRest(false);
            newPlan.setCompleted(false);

            dailyPlanRepository.save(newPlan);
        }else{
            String delayMessage = "[밀린 학습] : " + sourcePlan.getStudyDate() + "에 수행하지 못한 [" + sourcePlan.getTopic() + "] 수행하기";

            if(targetPlan.isRest()){
                targetPlan.setTopic("밀린 학습 수행하기");
                targetPlan.setDescription(delayMessage);
            }else{
                String originalDesc = targetPlan.getDescription() != null ? targetPlan.getDescription() : "";
                targetPlan.setDescription(originalDesc + "\n" + delayMessage);
            }
            targetPlan.setEstimatedHours(targetPlan.getEstimatedHours() + sourcePlan.getEstimatedHours());
            targetPlan.setRest(false);
        }

        String originalDesc = sourcePlan.getDescription() != null ? sourcePlan.getDescription() : "";
        sourcePlan.setDescription("[" + formattedTargetDate + "로 미뤄짐]\n" + originalDesc);
        sourcePlan.setCompleted(true);
    }

    @Transactional
    public List<WeeklyPlan> redistributePlan(String loginId, PlanRedistributeRequestDto request){
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
        ExamTask examTask = examTaskRepository.findById(request.getExamTaskId()).orElseThrow(() -> new IllegalArgumentException("해당 자격증을 찾을 수 없습니다."));
        validateTaskOwnership(examTask.getId(), loginId);

        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));

        if (request.getNewExamDate() != null) {
            examTask.setExamDate(request.getNewExamDate());
        }
        if (request.getWeeklySchedule() != null) {
            examTask.setWeeklySchedule(request.getWeeklySchedule());
        }

        long totalRemainingDays = ChronoUnit.DAYS.between(today, examTask.getExamDate());
        if (totalRemainingDays <= 0) {
            throw new IllegalArgumentException("시험일이 이미 지났거나 오늘입니다. 플랜을 재분배할 수 없습니다.");
        }

        // ✨ 3. 밀린 학습 내용 수집 (Stateless 해결의 핵심)
        List<DailyPlan> incompletePlans = dailyPlanRepository.findIncompletePlansByTaskId(examTask.getId());
        if (incompletePlans.isEmpty()) {
            throw new IllegalStateException("밀린 학습(미완료 플랜)이 없습니다. 재분배가 필요하지 않습니다.");
        }

        StringBuilder leftoverTasks = new StringBuilder("[필수 학습 분량: 아래 내용들을 남은 기간 내에 반드시 모두 소화하도록 분배할 것]\n");
        for (DailyPlan plan : incompletePlans) {
            leftoverTasks.append("- 주제: ").append(plan.getTopic()).append(" (설명: ").append(plan.getDescription()).append(")\n");
        }

        WeeklyPlan currentWeeklyPlan = dailyPlanRepository.findWeeklyPlanByDate(examTask.getId(), today).orElse(null);
        int startWeekNumber = (currentWeeklyPlan != null) ? currentWeeklyPlan.getWeekNumber() : 1;
        long remainingWeeksCount = (totalRemainingDays / 7) + (totalRemainingDays % 7 > 0 ? 1 : 0);
        int endWeekNumber = startWeekNumber + (int) remainingWeeksCount - 1;
        long lastWeekDays = (totalRemainingDays % 7 == 0) ? 7 : (totalRemainingDays % 7);

        String startDayOfWeek = today.getDayOfWeek().name().substring(0, 3);
        StringBuilder scheduleInfo = new StringBuilder("[주차별 캘린더 매핑 가이드 (반드시 아래 명시된 시간/휴식대로 JSON 생성)]\n");
        int currentDayCount = 0;
        for (int w = startWeekNumber; w <= endWeekNumber; w++) {
            scheduleInfo.append(String.format("- %d주차: ", w));
            int daysInThisWeek = (w == endWeekNumber) ? (int) lastWeekDays : 7;
            for (int d = 1; d <= daysInThisWeek; d++) {
                String dayName = today.plusDays(currentDayCount).getDayOfWeek().name().substring(0, 3);
                int hours = examTask.getWeeklySchedule().getOrDefault(dayName, 0); // DB에서 업데이트된 스케줄 사용
                String status = (hours == 0) ? "휴식" : hours + "시간";
                scheduleInfo.append(String.format("Day %d(%s), ", d, status));
                currentDayCount++;
            }
            scheduleInfo.setLength(scheduleInfo.length() - 2);
            scheduleInfo.append("\n");
        }

        // ✨ 4. DB에 저장해 둔 유저의 과거 기억 불러오기
        String certName = examTask.getCertificationName();
        String skill = examTask.getSkillLevel();
        String story = examTask.getPersonalStory();

        String prompt = String.format("""
                [Role]
                너는 커리어 전문 1:1 학습 플래너 AI야. 사용자가 기존 학습 계획을 다 지키지 못해, 남은 기간 동안 다시 완주할 수 있도록 [필수 학습 분량]을 새롭게 분배(Redistribute)해 달라고 요청했어.
                
                [User Status & Target]
                목표 시험(Task): %s
                오늘부터 시험일까지 남은 기간: 총 %d일 (약 %d주 분량)
                플랜 재시작 주차 번호: %d주차 부터 %d주차 까지
                학습 시작 요일(오늘): %s
                실력 자가 진단: %s
                
                %s
                
                %s
                
                [User's Personal Story (고민사항)]
                "%s"
                
                [Rules]
                1. 이 플랜은 완전히 새로 짜는 것이 아니라 기존 %d주차부터 이어서 진행하는 플랜이다. 따라서 JSON의 첫 번째 주차 객체의 weekNumber는 반드시 %d로 시작해야 한다.
                2. 가이드에 '휴식'으로 적혀있는 날은 estimatedHours를 0, isRest를 true로 설정하고, 'topic'에 '휴식 및 자율 보충'이라고 적어라.
                3. 진도를 나가는 날(시간이 배정된 날)은 오직 내가 제공한 [필수 학습 분량] 내의 주제들만을 사용하여 분배하라. 외부 지식이나 다른 목차를 임의로 추가하지 마라.
                4. 단, [필수 학습 분량]을 모두 배치하고도 남은 날짜(시간)가 있다면, 합격을 위해 '전체 복습', '기출문제 풀이', '실전 모의고사 풀이'와 같은 실전 대비 일정을 배치하여 빈 공간을 낭비 없이 채워라.
                5. 사용자의 약점이나 고민(Personal Story)을 반영하여, 일일 계획의 'description' 란에 공부 방법 포인트와 1:1 멘토링 코멘트를 문장 형태로 포함하라.
                6. 하루에 할당된 학습량은 반드시 할당된 시간(estimatedHours) 안에 소화할 수 있는 분량이어야 한다.
                7. 마지막 %d주차는 %d일까지만 존재하므로, 해당 주차의 dailyPlans 배열은 반드시 %d개의 객체만 포함해야 한다. 시험일 이후의 플랜은 절대 생성하지 마라.
                8. **[Critical Rule]** 시스템이 직접 파싱해야 하므로 반드시 아래의 JSON 규격으로만 출력해야 한다. 마크다운 기호(```json), 인삿말, 부가 설명 등은 절대 포함하지 말고 오직 JSON 텍스트만 반환하라.
                
                [Output JSON Format]
                {
                    "targetExam": "String (목표 시험명)",
                    "totalWeeks": "Integer (총 학습 주차, 여기서는 재분배된 총 주차 수)",
                    "weeklyPlans": [
                        {
                            "weekNumber": "Integer (주차 번호, 예: %d)",
                            "weeklyGoal": "String (해당 주차의 핵심 달성 목표)",
                            "dailyPlans": [
                                {
                                    "dayNumber": "Integer (일차 번호, 1-7)",
                                    "topic": "String (오늘 공부할 핵심 주제. 휴식일인 경우 '휴식 및 자율 보충')",
                                    "description": "String (오늘 학습해야 할 세부 내용과 공부 방법 포인트)",
                                    "estimatedHours": "Integer (오늘 할당된 학습 시간. 휴식일이면 0)",
                                    "isRest": "Boolean (휴식일 여부. 학습일이면 false, 휴식일이면 true)"
                                }
                            ]
                        }
                    ]
                }
                """,
                certName, totalRemainingDays, remainingWeeksCount, startWeekNumber, endWeekNumber,
                startDayOfWeek, skill, leftoverTasks.toString(), scheduleInfo.toString(), story,
                startWeekNumber, startWeekNumber, endWeekNumber, lastWeekDays, lastWeekDays, startWeekNumber
        );

        PlanResponseDto responseDto = requestToLlmWithRetry(prompt);

        // 기존 덮어쓰기 로직
        dailyPlanRepository.deletePlansFromToday(examTask.getId(), today);
        weeklyPlanRepository.flush();
        dailyPlanRepository.deleteEmptyWeeklyPlans(examTask.getId());

        List<WeeklyPlan> savedPlans = new ArrayList<>();
        int daysAdded = 0;

        for (PlanResponseDto.WeeklyPlanDto dtoWeek : responseDto.getWeeklyPlans()) {
            WeeklyPlan weeklyPlan = weeklyPlanRepository.findByExamTaskIdAndWeekNumber(examTask.getId(), dtoWeek.getWeekNumber())
                    .orElseGet(() -> {
                        WeeklyPlan newWeek = new WeeklyPlan();
                        newWeek.setExamTask(examTask);
                        newWeek.setWeekNumber(dtoWeek.getWeekNumber());
                        return newWeek;
                    });
            weeklyPlan.setWeeklyGoal(dtoWeek.getWeeklyGoal());

            weeklyPlan.getDailyPlans().removeIf(plan -> plan.getStudyDate() != null && !plan.getStudyDate().isBefore(today));

            for (PlanResponseDto.DailyPlanDto dtoDay : dtoWeek.getDailyPlans()) {
                DailyPlan dailyPlan = new DailyPlan();
                dailyPlan.setWeeklyPlan(weeklyPlan);
                dailyPlan.setDayNumber(dtoDay.getDayNumber());
                dailyPlan.setTopic(dtoDay.getTopic());
                dailyPlan.setDescription(dtoDay.getDescription());
                dailyPlan.setEstimatedHours(dtoDay.getEstimatedHours());
                dailyPlan.setRest(dtoDay.getIsRest() != null ? dtoDay.getIsRest() : false);
                dailyPlan.setCompleted(false);

                dailyPlan.setStudyDate(today.plusDays(daysAdded));
                daysAdded++;

                weeklyPlan.getDailyPlans().add(dailyPlan);
            }
            savedPlans.add(weeklyPlanRepository.save(weeklyPlan));
        }
        return savedPlans;
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

    @Transactional(readOnly = true)
    public PlanSettingsResponseDto getPlanSettings(String loginId, Long examTaskId) {
        // 유저 및 권한 검증
        ExamTask examTask = examTaskRepository.findById(examTaskId)
                .orElseThrow(() -> new IllegalArgumentException("해당 자격증을 찾을 수 없습니다."));
        validateTaskOwnership(examTaskId, loginId);

        // DB에 저장된 데이터 꺼내서 DTO로 변환
        return PlanSettingsResponseDto.builder()
                .examTaskId(examTask.getId())
                .certificationName(examTask.getCertificationName())
                .examDate(examTask.getExamDate())
                .skillLevel(examTask.getSkillLevel())
                .personalStory(examTask.getPersonalStory())
                .weeklySchedule(examTask.getWeeklySchedule())
                .build();
    }

    // 보안 로직
    private void validateTaskOwnership(Long examTaskId, String loginId){
        if(!examTaskRepository.isOwnerOfTask(examTaskId, loginId)){
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }
    }

    private PlanResponseDto requestToLlmWithRetry(String prompt){
        int maxRetries = 3;
        int attempt = 0;

        while(attempt < maxRetries){
            attempt++;
            try{
                String jsonResponse = llmService.requestToLlm(prompt);
                return objectMapper.readValue(jsonResponse, PlanResponseDto.class);
            }catch(Exception e){
                if(attempt >= maxRetries){
                    throw new RuntimeException("LLM 응답 처리 중 오류가 발생했습니다: " + e.getMessage());
                }
            }
        }
        return null;
    }
}
