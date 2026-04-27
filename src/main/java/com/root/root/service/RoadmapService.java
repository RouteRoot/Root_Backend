package com.root.root.service;

import com.root.root.dto.LLMRoadmapResponseDto;
import com.root.root.dto.RoadmapRequestDto;
import com.root.root.dto.RoadmapResponseDto;
import com.root.root.entity.ExamTask;
import com.root.root.entity.Phase;
import com.root.root.entity.Roadmap;
import com.root.root.entity.User;
import com.root.root.entity.standard.StandardJobRequirement;
import com.root.root.repository.RoadmapRepository;
import com.root.root.repository.StandardExamRepository;
import com.root.root.repository.StandardJobRepository;
import com.root.root.repository.UserRepository;
import com.root.root.repository.ExamTaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoadmapService {
    private final LLMService llmService;
    private final RoadmapRepository roadmapRepository;
    private final UserRepository userRepository;
    private final ExamTaskRepository examTaskRepository;
    private final ObjectMapper objectMapper;

    private final StandardJobRepository standardJobRepository;
    private final StandardExamRepository standardExamRepository;

    @Transactional
    public Roadmap generateAndSaveRoadmap(String loginId, RoadmapRequestDto request) {
        // 유저 찾기(DB에 없으면 에러)
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
        // 기존 로드맵이 존재한다면 덮어쓰기 위해 삭제
        Optional<Roadmap> existingRoadmap = roadmapRepository.findByUserId(user.getId());
        if(existingRoadmap.isPresent()){
            user.setRoadmap(null);
            roadmapRepository.delete(existingRoadmap.get());
            roadmapRepository.flush();
        }
        // 입력받은 로드맵 조건 저장 및 업데이트
        user.setEducationStatus(request.getEducationStatus());
        user.setGrade(request.getGrade());
        user.setMajor(request.getMajor());
        user.setHope(request.getHope());
        user.setMajorRelated(request.isMajorRelated());
        user.setCareer(request.getCareer());
        //user.setDaily(request.getDaily());
        //user.setWeekly(request.getWeekly());
        user.setMylevel(request.getMylevel());
        user.setTarget(request.getTarget());
        user.setPersonalStory(request.getPersonalStory());
        user.setOnboardingCompleted(true);

        user.getAcquired().clear();
        if(request.getAcquired() != null && !request.getAcquired().isEmpty()){
            user.getAcquired().addAll(request.getAcquired());
        }

        String personalStory = request.getPersonalStory() != null && !request.getPersonalStory().trim().isEmpty() ? request.getPersonalStory() : "특별한 약점이나 고민은 없으며, 일반적이고 안정적인 흐름에 따라 커리어를 설계하길 원함.";

        StringBuilder standardDataPrompt = new StringBuilder();
        List<String> mandatoryTasks = new ArrayList<>();

        standardJobRepository.findByHope(request.getHope()).ifPresent(job -> {
            standardDataPrompt.append("\n[Standard Data: 직무 필수 자격증 및 큐넷 응시 조건]\n");

            for(StandardJobRequirement req : job.getRequirements()){
                mandatoryTasks.add(req.getTaskName());

                standardExamRepository.findByCertificationName(req.getTaskName()).ifPresent(exam -> {
                    standardDataPrompt.append(String.format("- %s (응시 자격: %s)\n", req.getTaskName(), exam.getEligibilityCondition()));
                });
            }
        });
        // LLM에게 보낼 프롬프트 완성
        String prompt = String.format("""
                        [Role]
                        너는 취업 및 커리어 설계 분야의 최고 전문가 AI 컨설턴트야.
                        너의 임무는 사용자의 현재 상황, 목표, 학습 가능 시간, 실력을 정밀하게 분석하여 가장 현실적이고 효율적인 맞춤형 '자격증' 및 '어학 시험' 취득 로드맵을 설계하는 거야.
                        
                        [User Input Data]
                        학위 상태: %s
                        현재 학년: %d (0이면 졸업)
                        전공: %s
                        희망 직무: %s
                        관련 전공 여부: %b
                        실무 경력: %d년
                        기취득 스펙: %s
                        실력 자가 진단: %s
                        선호 기업 형태: %s
                        
                        [User's Personal Story (강점/약점/고민)]
                        "%s"
                        
                        %s
                        
                        [Rules]
                        1. [기취득 스펙]에 명시된 자격증은 추천에서 무조건 제외하라. 단, 어학 시험(예: 토익, 토스, 오픽 등)의 경우 사용자의 현재 점수가 [선호 기업 형태]의 일반적 합격 안정권보다 낮다면, 목표 점수를 상향 설정하여 로드맵에 포함시켜라.
                        2. 추천 항목은 오직 ‘공인 자격증’과 ‘어학 시험’으로만 제한하라. 개인적인 코딩 공부, 토이 프로젝트, 포트폴리오 준비 등은 절대 포함하지 마라.
                        3. [taskName]에는 반드시 대한민국 및 국제적으로 실존하는 공식 자격증/어학 시험의 정확한 명칭만 단답형으로 기재하라. (예: 정보처리기사, 리눅스마스터 2급, SQLD, TOEIC)
                        4. [taskName]에 절대 ‘운영체제 관련 자격증(예: 리눅스마스터 2급)’처럼 부연 설명, 카테고리, 괄호(), ‘예시’라는 단어를 덧붙이지 마라. 오직 공식 명칭만 출력하라.
                        5. [taskName]에 임의로 가상의 자격증을 창작하거나 존재하지 않는 등급(예: 정보처리기사 고급, 파이썬 마스터 초급 등)을 절대 붙이지 마라. 공식 등급이 있는 경우에만 기재하고, 단일 자격증은 명칭만 정확히 기재하라.
                        6. [Standard Data]에 제공된 필수 자격증은 로드맵 어딘가에 반드시 100%% 포함시켜라. 단, 그것만으로 로드맵을 끝내지 마라. 사용자의 희망 직무에 실질적으로 도움이 되는 다른 관련 공인 자격증을 너의 전문가적 판단하에 자율적으로 덧붙여라.
                        7. [User Input Data]의 현재 학위 상태, 학년, 경력과 [Standard Data]의 응시 자격을 논리적으로 분석하고 엄격하게 대조하라. 만약 사용자가 현재 시점에 응시 불가능하다면 부족한 학년/경력을 계산하여 응시 자격이 충족되는 미래의 시점(Phase 2 또는 3)으로 해당 자격증을 미뤄서 배치하라. 응시 불가능하다고 누락시키는 것을 절대 금지한다. 
                        8. 응시 불가로 인한 초반 공백기(Phase 1)에는 응시 제한이 없는 필수 자격증이나 어학 시험을 우선 배치하라.
                        9. 각 자격증 및 어학 시험은 전체 로드맵을 통틀어 오직 1개의 Phase에 딱 한 번만 등장해야 한다. 미리 공부하기 위해 앞선 Phase에 배치하는 것을 절대 금지한다. 오직 해당 시험에 실제로 응시하여 취득하는 시점의 Phase에 단 한 번만 배치하라. (예: 동일 자격증을 Phase 1, 2, 3에 걸쳐 쪼개서 넣지 마라)
                        10. 모든 자격증을 하나의 Phase에 몰아넣는 것은 절대 금지한다. 3개의 Phase에 골고루 분산 배치하라. 
                        10. [확보 가능한 학습 시간]과 [실력 자가 진단]을 바탕으로, 각 시험을 준비하고 합격하는 데 필요한 현실적인 소요 기간(주 단위)을 정확하게 산정하라. (소수점은 반드시 올림 처리하여 정수로 출력하라. 과도하게 낙관적인 기간 산정은 금지한다.)
                        11. [선호 기업 형태]에 맞춰 우선순위를 조정하라. (예: 공기업은 한국사, 컴활 등 가산점 자격증 우선 배치. 대기업은 토익 850 이상 또는 오픽 IH 이상 + 직무 기사 자격증. IT기업은 정보처리기사 우선. 단, 사용자 조건에서 현실적으로 도전 가능한 순서로 재배치하라.)
                        12. 로드맵은 사용자가 지치지 않도록 Phase 1(기초), Phase 2(심화), Phase 3(고급)의 3단계로 나누어 구성하라.(Phase 1: 성공 확률이 가장 높은 기초 경쟁력 확보 단계. Phase 2: 직무 직접 경쟁력 강화 단계. Phase 3: 상위 기업 안정권 진입 단계.)
                        13. 각 Phase는 기본적으로 3개의 자격증 또는 어학 시험을 포함해야 한다. 하지만 해당 직무 분야에서 현실적으로 선택 가능한 공인 자격증 종류가 적거나, 사용자의 현재 조건에서 합격 가능성이 충분한 시험이 3개 미만일 경우에 한해 1-2개만 포함할 수 있다. 단순히 개수를 맞추기 위해 직무와 무관하거나 전략적으로 의미 없는 자격증을 추가하는 것은 금지한다. 전체 로드맵에 걸쳐 중복 없이 적절한 시점에 분산 배치하는 것이 최우선이다. 
                        14. 자격증 선택 시 다음을 내부적으로 비교 검토하라.(출력 금지) 현재 조건에서의 상대적 합격 가능성, 준비 부담도, 선행 자격증이 이후 시험에 주는 긍정 효과, 전체 경로의 누적 학습 피로도. 단순 나열이 아니라 가장 안정적인 순서를 선택하라.
                        15. 사용자가 작성한 [User's Personal Story]를 철저하게 분석하여 description 작성 시 사용자의 고민을 덜어주고 방향성을 제시하는 맞춤형 멘토링 코멘트를 자연스럽게 포함하라. 
                        16.  **[Critical Rule]** 너의 답변은 시스템이 곧바로 파싱해야 하므로 반드시 아래의 JSON 규격으로만 출력해야 한다. 마크다운 기호(```json), 인삿말, 부가 설명 등은 절대 포함하지 말고 오직 JSON 텍스트만 반환하라. 스마트 따옴표 금지, 순자는 문자열이 아닌 정수형으로 출력, JSON 외 추가 텍스트 절대 금지.
                        
                        [Output JSON Format]
                        {
                            "roadmap": [
                                {
                                    "phaseNumber": "Integer (Phase의 3단계)",
                                    "phaseTitle": "String (해당 단계의 목표를 요약한 핵심 제목. 절대 null 금지)",
                                    "estimatedWeeks": "Integer (해당 단계를 완료하는 데 걸리는 총 예상 주차)",
                                    "tasks": [
                                        {
                                            "taskName": "String (구체적인 자격증 또는 어학 시험명과 목표 점수. 중복 절대 금지)",
                                            "description": "String (해당 자격증/어학 시험을 왜 추천하는지, 그리고 어떻게 준비해야 하는지에 대한 상세 가이드)"
                                        }
                                    ]
                                }
                            ]
                        }
                        """,
                request.getEducationStatus(), request.getGrade(), request.getMajor(), request.getHope(), request.isMajorRelated(), request.getCareer(), (request.getAcquired() != null && !request.getAcquired().isEmpty()) ? request.getAcquired().toString() : "없음", request.getMylevel(), request.getTarget(), personalStory, standardDataPrompt.toString()
        );

        // Auto-Retry 로직(3회)
        int maxRetries = 3;
        int attempt = 0;
        boolean isValid = false;
        LLMRoadmapResponseDto responseDto = null;

        while(attempt < maxRetries && !isValid){
            attempt++;
            try{
                // LLM 호출해서 JSON 받아오기
                String jsonResponse = llmService.requestToLlm(prompt);
                // JSON -> DTO 파싱
                responseDto = objectMapper.readValue(jsonResponse, LLMRoadmapResponseDto.class);
                // 필수 자격증 누락 여부 검증 로직
                validateMandatoryTasks(responseDto, mandatoryTasks);

                isValid = true;
            }catch(IllegalStateException e){
                if(attempt >= maxRetries){
                    throw new RuntimeException("로드맵 생성 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
                }
            }catch(Exception e){
                if(attempt >= maxRetries){
                    throw new RuntimeException("LLM 응답 처리 중 오류가 발생했습니다: " + e.getMessage());
                }
            }
        }

        try{
            Roadmap roadmap = new Roadmap();
            roadmap.setUser(user);
            user.setRoadmap(roadmap);

            for(LLMRoadmapResponseDto.PhaseDto phaseDto : responseDto.getRoadmap()){
                Phase phase = new Phase();
                phase.setPhaseNumber(phaseDto.getPhaseNumber());
                phase.setPhaseTitle(phaseDto.getPhaseTitle());
                phase.setEstimatedWeeks(phaseDto.getEstimatedWeeks());
                phase.setRoadmap(roadmap);

                for(LLMRoadmapResponseDto.TaskDto taskDto : phaseDto.getTasks()){
                    ExamTask examTask = new ExamTask();
                    examTask.setTaskName(taskDto.getTaskName());
                    examTask.setDescription(taskDto.getDescription());
                    examTask.setStatus("NOT_STARTED");
                    examTask.setPhase(phase);

                    phase.getTasks().add(examTask);
                }
                roadmap.getPhases().add(phase);
            }
            return roadmapRepository.save(roadmap);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("LLM 로드맵 JSON 파싱 또는 DB 저장 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    private void validateMandatoryTasks(LLMRoadmapResponseDto responseDto, List<String> mandatoryTasks){
        if(mandatoryTasks.isEmpty()) return;

        List<String> generatedTasks = new ArrayList<>();
        for(LLMRoadmapResponseDto.PhaseDto phase : responseDto.getRoadmap()){
            for(LLMRoadmapResponseDto.TaskDto task : phase.getTasks()){
                generatedTasks.add(task.getTaskName());
            }
        }

        for(String mandatoryTask : mandatoryTasks){
            boolean isIncluded = generatedTasks.stream().anyMatch(taskName -> taskName.contains(mandatoryTask));
            if(!isIncluded){
                throw new IllegalStateException("로드맵 생성 중 검증 오류. 다시 시도");
            }
        }
    }

    @Transactional
    public void markTaskAsCompleted(String loginId, Long taskId){
        ExamTask examTask = getExamTaskWithAuthority(loginId, taskId);

        if(!"IN_PROGRESS".equals(examTask.getStatus())){
            throw new IllegalArgumentException("학습 플랜 생성 후 학습을 시작해야 완료 처리 가능함");
        }

        examTask.setStatus("COMPLETED");
    }

    @Transactional
    public void cancelTaskCompletion(String loginId, Long taskId){
        ExamTask examTask = getExamTaskWithAuthority(loginId, taskId);

        if(!"COMPLETED".equals(examTask.getStatus())){
            throw new IllegalArgumentException("취득 완료된 자격증만 상태를 되돌릴 수 있음");
        }

        examTask.setStatus("IN_PROGRESS");
    }

    @Transactional
    public RoadmapResponseDto getRoadmap(String loginId){
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
        // DB에서 로드맵 탐색
        Roadmap roadmap = roadmapRepository.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("아직 생성된 로드맵이 없습니다."));
        // DTO 반환
        return new RoadmapResponseDto(roadmap, user);
    }

    // 유저 권한 및 자격증 확인 로직
    private ExamTask getExamTaskWithAuthority(String loginId, Long taskId){
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
        ExamTask examTask = examTaskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("해당 자격증을 찾을 수 없습니다."));

        if(!examTask.getPhase().getRoadmap().getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("본인의 로드맵만 관리할 수 있습니다.");
        }
        return examTask;
    }
}
