package com.root.root.service;

import com.root.root.dto.LLMRoadmapResponseDto;
import com.root.root.dto.RoadmapRequestDto;
import com.root.root.dto.RoadmapResponseDto;
import com.root.root.entity.ExamTask;
import com.root.root.entity.Phase;
import com.root.root.entity.Roadmap;
import com.root.root.entity.User;
import com.root.root.repository.RoadmapRepository;
import com.root.root.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RoadmapService {
    private final LLMService llmService;
    private final RoadmapRepository roadmapRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Roadmap generateAndSaveRoadmap(String loginId, RoadmapRequestDto request) {
        // 유저 찾기(DB에 없으면 에러)
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
        // LLM에게 보낼 프롬프트 완성
        String prompt = String.format("""
                        [Role]
                        너는 취업 및 커리어 설계 분야의 최고 전문가 AI 컨설턴트야.
                        너의 임무는 사용자의 현재 상황, 목표, 학습 가능 시간, 실력을 정밀하게 분석하여 가장 현실적이고 효율적인 맞춤형 '자격증' 및 '어학 시험' 취득 로드맵을 설계하는 거야.
                        
                        [User Input Data]
                        전공: %s
                        희망 직무: %s
                        기취득 스펙: %s
                        현재 신분: %s
                        확보 가능한 학습 시간: 일간 %d시간, 주간(주말 포함) %d시간
                        실력 자가 진단: %s
                        선호 기업 형태: %s
                        
                        [Rules]
                        1. [기취득 스펙]에 명시된 자격증은 추천에서 무조건 제외해라. 단, 어학 시험(예: 토익, 토스, 오픽 등)의 경우 사용자의 현재 점수가 [선호 기업 형태]의 일반적 합격 안정권보다 낮다면, 목표 점수를 상향 설정하여 로드맵에 포함시켜라.
                        2. 추천 항목은 오직 ‘공인 자격증’과 ‘어학 시험’으로만 제한해라. 개인적인 코딩 공부, 토이 프로젝트, 포트폴리오 준비 등은 절대 포함하지 마라.
                        3. [현재 신분]을 최우선으로 고려하여 응시 자격 요건을 계산해라. (예: 기사/산업기사 등 학력, 학년 제한이 있는 시험은 반드시 응시 가능 여부를 판단하라. 응시 불가능한 자격증은 절대 추천하지 마라. 필요 시 동일 직무군 내 응시 가능한 대체 자격증을 제시하라.)
                        4. [확보 가능한 학습 시간]과 [실력 자가 진단]을 바탕으로, 각 시험을 준비하고 합격하는 데 필요한 현실적인 소요 기간(주 단위)을 정확하게 산정하라. (소수점은 반드시 올림 처리하여 정수로 출력하라. 과도하게 낙관적인 기간 산정은 금지한다.)
                        5. [선호 기업 형태]에 맞춰 우선순위를 조정하라. (예: 공기업은 한국사, 컴활 등 가산점 자격증 우선 배치. 대기업은 토익 850 이상 또는 오픽 IH 이상 + 직무 기사 자격증. IT기업은 정보처리기사 우선. 단, 사용자 조건에서 현실적으로 도전 가능한 순서로 재배치하라.)
                        6. 로드맵은 사용자가 지치지 않도록 Phase 1(기초), Phase 2(심화), Phase 3(고급)의 3단계로 나누어 구성하라.(Phase 1: 성공 확률이 가장 높은 기초 경쟁력 확보 단계. Phase 2: 직무 직접 경쟁력 강화 단계. Phase 3: 상위 기업 안정권 진입 단계. 각 Phase는 기본적으로 3개의 자격증 또는 어학 시험을 포함해야 한다. 단, 해당 직무 분야에서 현실적으로 선택 가능한 공인 자격증 종류가 적거나, 사용자의 현재 조건에서 합격 가능성이 충분한 시험이 3개 미만일 경우에 한해 1-2개만 포함할 수 있다. 단순히 개수를 맞추기 위해 직무와 무관하거나 전략적으로 의미 없는 자격증을 추가하는 것은 금지한다. 난이도는 반드시 점진적으로 상승해야 한다. 전체 경로는 전략적으로 일관성을 유지해야 한다. )
                        7. 자격증 선택 시 다음을 내부적으로 비교 검토하라.(출력 금지) 현재 조건에서의 상대적 합격 가능성, 준비 부담도, 선행 자격증이 이후 시험에 주는 긍정 효과, 전체 경로의 누적 학습 피로도. 단순 나열이 아니라 가장 안정적인 순서를 선택하라.
                        8.  **[Critical Rule]** 너의 답변은 시스템이 곧바로 파싱해야 하므로 반드시 아래의 JSON 규격으로만 출력해야 한다. 마크다운 기호(```json), 인삿말, 부가 설명 등은 절대 포함하지 말고 오직 JSON 텍스트만 반환하라. 스마트 따옴표 금지, 순자는 문자열이 아닌 정수형으로 출력, JSON 외 추가 텍스트 절대 금지.
                        
                        [Output JSON Format]
                        {
                            "roadmap": [
                                {
                                    "phase": "Integer (1, 2, 3)",
                                    "phaseTitle": "String (해당 단계의 목표를 요약한 핵심 제목)",
                                    "estimatedWeeks": "Integer (해당 단계를 완료하는 데 걸리는 총 예상 주차)",
                                    "tasks": [
                                        {
                                            "taskName": "String (구체적인 자격증 또는 어학 시험명과 목표 점수)",
                                            "description": "String (해당 자격증/어학 시험을 왜 추천하는지, 그리고 어떻게 준비해야 하는지에 대한 상세 가이드)"
                                        }
                                    ]
                                }
                            ]
                        }
                        """,
                request.getMajor(), request.getHope(), request.getAcquired(), request.getStatus(), request.getDaily(), request.getWeekly(), request.getMylevel(), request.getTarget()
        );
        // LLM 호출해서 JSON 받아오기
        String jsonResponse = llmService.requestToLlm(prompt);

        try{
            // 파싱(JSON -> DTO)
            LLMRoadmapResponseDto responseDto = objectMapper.readValue(jsonResponse, LLMRoadmapResponseDto.class);
            // 엔티티 생성 및 데이터 입력
            Roadmap roadmap = new Roadmap();
            roadmap.setUser(user);
            roadmap.setMajor(request.getMajor());
            roadmap.setHope(request.getHope());
            roadmap.setAcquired(request.getAcquired());
            roadmap.setStatus(request.getStatus());
            roadmap.setDaily(request.getDaily());
            roadmap.setWeekly(request.getWeekly());
            roadmap.setMylevel(request.getMylevel());
            roadmap.setTarget(request.getTarget());
            // 관계
            for(LLMRoadmapResponseDto.PhaseDto phaseDto : responseDto.getRoadmap()){
                Phase phase = new Phase();
                phase.setPhase(phaseDto.getPhase());
                phase.setPhaseTitle(phaseDto.getPhaseTitle());
                phase.setEstimatedWeeks(phaseDto.getEstimatedWeeks());
                phase.setRoadmap(roadmap);

                for (LLMRoadmapResponseDto.TaskDto taskDto : phaseDto.getTasks()) {
                    ExamTask examTask = new ExamTask();
                    examTask.setTaskName(taskDto.getTaskName());
                    examTask.setDescription(taskDto.getDescription());
                    examTask.setStatus(ExamTask.TaskStatus.NOT_STARTED);
                    examTask.setPhase(phase);

                    phase.getTasks().add(examTask);
                }
                roadmap.getPhases().add(phase);
            }
            return roadmapRepository.save(roadmap);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("LLM JSON 파싱 또는 DB 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public RoadmapResponseDto getRoadmap(Long roadmapId, String loginId){
        // DB에서 로드맵 탐색
        Roadmap roadmap = roadmapRepository.findById(roadmapId).orElseThrow(() -> new IllegalArgumentException("해당 로드맵을 찾을 수 없습니다."));
        if(!roadmap.getUser().getLoginId().equals(loginId)){
            throw new IllegalArgumentException("자신의 로드맵만 조회할 수 있습니다.");
        }
        // DTO 반환
        return new RoadmapResponseDto(roadmap);
    }
}
