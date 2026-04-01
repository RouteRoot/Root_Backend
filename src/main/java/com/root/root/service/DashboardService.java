package com.root.root.service;

import com.root.root.dto.DashboardResponseDto;
import com.root.root.entity.DailyPlan;
import com.root.root.entity.ExamTask;
import com.root.root.entity.Roadmap;
import com.root.root.entity.User;
import com.root.root.repository.DailyPlanRepository;
import com.root.root.repository.RoadmapRepository;
import com.root.root.repository.UserRepository;
import com.root.root.repository.ExamTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserRepository userRepository;
    private final RoadmapRepository roadmapRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final ExamTaskRepository examTaskRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDto getDashboardData(String loginId, Long requestedExamTaskId){
        // 유저 조회
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 로드맵 진행도 계산
        Roadmap roadmap = roadmapRepository.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("로드맵이 존재하지 않습니다."));

        // 전체 자격증 개수
        int totalTasks = roadmap.getPhases().stream().mapToInt(phase -> phase.getTasks().size()).sum();

        // 취득한 자격증 개수
        int completedTasks = (int)roadmap.getPhases().stream().flatMap(phase -> phase.getTasks().stream()).filter(task -> "COMPLETED".equals(task.getStatus())).count();

        DashboardResponseDto.RoadmapProgressDto roadmapProgress = DashboardResponseDto.RoadmapProgressDto.builder().totalTasks(totalTasks).completedTasks(completedTasks).build();

        Long targetExamTaskId = requestedExamTaskId;
        if(targetExamTaskId == null){
            targetExamTaskId = examTaskRepository.findTasksWithPlansByUserLoginId(loginId).stream().findFirst().map(ExamTask::getId).orElse(null);
        }

        // 해당 탭(자격증)의 일간 플랜 정보 가져오기 & 진행도 계산
        DashboardResponseDto.PlanProgressDto planProgress = DashboardResponseDto.PlanProgressDto.builder().totalPlanDays(0).completedPlanDays(0).build();
        DashboardResponseDto.CurrentStudyPlanDto currentStudyPlan = null;

        if(targetExamTaskId != null){
            List<DailyPlan> tabDailyPlans = dailyPlanRepository.findByUserIdAndExamTaskId(user.getId(), targetExamTaskId);

            planProgress = DashboardResponseDto.PlanProgressDto.builder().totalPlanDays(tabDailyPlans.size()).completedPlanDays((int) tabDailyPlans.stream().filter(DailyPlan::isCompleted).count()).build();

            LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));
            currentStudyPlan = tabDailyPlans.stream().filter(plan -> today.equals(plan.getStudyDate())).findFirst().map(plan -> DashboardResponseDto.CurrentStudyPlanDto.builder().weeklyPlanId(plan.getWeeklyPlan().getId()).weekNumber(plan.getWeeklyPlan().getWeekNumber()).weeklyGoal(plan.getWeeklyPlan().getWeeklyGoal()).dailyPlanId(plan.getId()).date(plan.getStudyDate()).topic(plan.getTopic()).isCompleted(plan.isCompleted()).build()).orElse(null);
        }

        // 최종 DTO 반환
        return DashboardResponseDto.builder().currentStudyPlan(currentStudyPlan).planProgress(planProgress).roadmapProgress(roadmapProgress).build();
    }
}
