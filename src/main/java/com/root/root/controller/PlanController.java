package com.root.root.controller;

import com.root.root.dto.PlanCreateRequestDto;
import com.root.root.dto.PlanResponseDto;
import com.root.root.entity.DailyPlan;
import com.root.root.entity.WeeklyPlan;
import com.root.root.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;

    @PostMapping
    public ResponseEntity<PlanResponseDto> createPlan(Authentication authentication, @RequestBody PlanCreateRequestDto request){
        // 토큰에서 파싱된 로그인 아이디 get
        String loginId = authentication.getName();

        // 서비스 로직 호출
        List<WeeklyPlan> savedPlans = planService.generateAndSavePlan(loginId, request);

        // DTO 변환
        PlanResponseDto responseDto = convertToDto(savedPlans, request.getCertificationName());

        return ResponseEntity.ok(responseDto);
    }

    private PlanResponseDto convertToDto(List<WeeklyPlan> savedPlans, String targetExam){
        PlanResponseDto response = new PlanResponseDto();
        response.setTargetExam(targetExam);
        response.setTotalWeeks(savedPlans.size());

        List<PlanResponseDto.WeeklyPlanDto> weeklyDtoList = new ArrayList<>();

        for(WeeklyPlan wp : savedPlans){
            PlanResponseDto.WeeklyPlanDto weeklyDto = new PlanResponseDto.WeeklyPlanDto();
            weeklyDto.setWeekNumber(wp.getWeekNumber());
            weeklyDto.setWeeklyGoal(wp.getWeeklyGoal());

            List<PlanResponseDto.DailyPlanDto> dailyDtoList = new ArrayList<>();
            for(DailyPlan dp : wp.getDailyPlans()){
                PlanResponseDto.DailyPlanDto dailyDto = new PlanResponseDto.DailyPlanDto();
                dailyDto.setDayNumber(dp.getDayNumber());
                dailyDto.setTopic(dp.getTopic());
                dailyDto.setDescription(dp.getDescription());
                dailyDto.setEstimatedHours(dp.getEstimatedHours());
                dailyDto.setRest(dp.isRest());
                //dailyDto.setStudyDate(dp.getStudyDate());
                //dailyDto.setCompleted(dp.isCompleted());
                //dailyDto.setDailyPlanId(dp.getId());

                dailyDtoList.add(dailyDto);
            }
            weeklyDto.setDailyPlans(dailyDtoList);
            weeklyDtoList.add(weeklyDto);
        }
        response.setWeeklyPlans(weeklyDtoList);
        return response;
    }
}
