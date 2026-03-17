package com.root.root.controller;

import com.root.root.dto.PlanCreateRequestDto;
import com.root.root.dto.PlanResponseDto;
import com.root.root.entity.DailyPlan;
import com.root.root.entity.WeeklyPlan;
import com.root.root.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;

    @PostMapping
    public ResponseEntity<PlanResponseDto> createPlan(Authentication authentication, @RequestBody PlanCreateRequestDto request){
        // 토큰에서 로그인 아이디 파싱
        String loginId = authentication.getName();

        // 서비스 로직 호출
        List<WeeklyPlan> savedPlans = planService.generateAndSavePlan(loginId, request);

        // DTO 변환
        PlanResponseDto responseDto = convertToDto(savedPlans, request.getExamTaskId(), request.getCertificationName());

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{examTaskId}")
    public ResponseEntity<PlanResponseDto> getPlan(Authentication authentication, @PathVariable Long examTaskId){
        // 토큰에서 로그인 아이디 파싱
        String loginId = authentication.getName();

        // Service에서 엔티티 리스트 조회
        List<WeeklyPlan> weeklyPlans = planService.getStudyPlan(loginId, examTaskId);

        // 타겟 자격증 이름 get
        String taskName = weeklyPlans.get(0).getExamTask().getTaskName();

        // DTO 변환
        PlanResponseDto responseDto = convertToDto(weeklyPlans, examTaskId, taskName);

        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/daily/{dailyPlanId}/check")
    public ResponseEntity<Map<String, Object>> checkDailyPlan(Authentication authentication, @PathVariable Long dailyPlanId){
        String loginId = authentication.getName();

        // 서비스 호출, 상태 변경, 결과 get
        boolean updatedStatus = planService.togglePlanCompletion(loginId, dailyPlanId);

        // JSON 응답
        Map<String, Object> response = new HashMap<>();
        response.put("dailyPlanId", dailyPlanId);
        response.put("isCompleted", updatedStatus);
        response.put("message", updatedStatus ? "학습 완료" : "학습 완료 취소");

        return ResponseEntity.ok(response);
    }

    private PlanResponseDto convertToDto(List<WeeklyPlan> savedPlans, Long examTaskId, String taskName){
        PlanResponseDto response = new PlanResponseDto();
        response.setExamTaskId(examTaskId);
        response.setTaskName(taskName);
        response.setTotalWeeks(savedPlans.size());

        List<PlanResponseDto.WeeklyPlanDto> weeklyDtoList = new ArrayList<>();

        for(WeeklyPlan wp : savedPlans){
            PlanResponseDto.WeeklyPlanDto weeklyDto = new PlanResponseDto.WeeklyPlanDto();
            weeklyDto.setWeeklyPlanId(wp.getId());
            weeklyDto.setWeekNumber(wp.getWeekNumber());
            weeklyDto.setWeeklyGoal(wp.getWeeklyGoal());

            List<PlanResponseDto.DailyPlanDto> dailyDtoList = new ArrayList<>();
            for(DailyPlan dp : wp.getDailyPlans()){
                PlanResponseDto.DailyPlanDto dailyDto = new PlanResponseDto.DailyPlanDto();
                dailyDto.setDayNumber(dp.getDayNumber());
                dailyDto.setTopic(dp.getTopic());
                dailyDto.setDescription(dp.getDescription());
                dailyDto.setEstimatedHours(dp.getEstimatedHours());
                dailyDto.setIsRest(dp.isRest());
                dailyDto.setStudyDate(dp.getStudyDate());
                dailyDto.setIsCompleted(dp.isCompleted());
                dailyDto.setDailyPlanId(dp.getId());

                dailyDtoList.add(dailyDto);
            }
            weeklyDto.setDailyPlans(dailyDtoList);
            weeklyDtoList.add(weeklyDto);
        }
        response.setWeeklyPlans(weeklyDtoList);
        return response;
    }
}
