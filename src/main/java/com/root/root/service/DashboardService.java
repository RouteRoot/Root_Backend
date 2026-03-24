package com.root.root.service;

import com.root.root.dto.DashboardResponseDto;
import com.root.root.entity.DailyPlan;
import com.root.root.entity.Roadmap;
import com.root.root.entity.User;
import com.root.root.repository.DailyPlanRepository;
import com.root.root.repository.RoadmapRepository;
import com.root.root.repository.UserRepository;
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

    @Transactional(readOnly = true)
    public DashboardResponseDto getDashboardData(String loginId){
        // 유저 조회
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 로드맵 진행도 계산
        Roadmap roadmap = roadmapRepository.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("로드맵이 존재하지 않습니다."));

        // 전체 자격증 개수
        int totalTasks = roadmap.getPhases().stream().mapToInt(phase -> phase.getTasks().size()).sum();

        // 취득한 자격증 개수
        int completedTasks = (int)roadmap.getPhases().stream().flatMap(phase -> phase.getTasks().stream()).filter(task -> "COMPLETED".equals(task.getStatus())).count();

        DashboardResponseDto.RoadmapProgressDto roadmapProgress = DashboardResponseDto.RoadmapProgressDto.builder().totalTasks(totalTasks).completedTasks(completedTasks).build();

        // 오늘의 플랜 및 전체 플랜 진행도 조회
        List<DailyPlan> allDailyPlans = dailyPlanRepository.findAllByUserId(user.getId());

        int totalPlanDays = allDailyPlans.size();
        int completedPlanDays = (int)allDailyPlans.stream().filter(DailyPlan::isCompleted).count();

        DashboardResponseDto.PlanProgressDto planProgress = DashboardResponseDto.PlanProgressDto.builder().totalPlanDays(totalPlanDays).completedPlanDays(completedPlanDays).build();

        // 오늘 날짜에 맞는 학습 플랜 탐색, 계획 없으면 null 반환(프론트에서 처리)
        LocalDate today = LocalDate.now();
        DashboardResponseDto.CurrentStudyPlanDto currentStudyPlan = allDailyPlans.stream().filter(plan -> today.equals(plan.getStudyDate())).findFirst().map(plan -> DashboardResponseDto.CurrentStudyPlanDto.builder().weeklyPlanId(plan.getWeeklyPlan().getId()).weekNumber(plan.getWeeklyPlan().getWeekNumber()).weeklyGoal(plan.getWeeklyPlan().getWeeklyGoal()).dailyPlanId(plan.getId()).date(plan.getStudyDate()).topic(plan.getTopic()).isCompleted(plan.isCompleted()).build()).orElse(null);

        // 최종 DTO 반환
        return DashboardResponseDto.builder().currentStudyPlan(currentStudyPlan).planProgress(planProgress).roadmapProgress(roadmapProgress).build();
    }
}
