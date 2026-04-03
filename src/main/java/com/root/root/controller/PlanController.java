package com.root.root.controller;

import com.root.root.dto.PlanCreateRequestDto;
import com.root.root.dto.PlanDetailResponseDto;
import com.root.root.dto.PlanResponseDto;
import com.root.root.dto.PlanTabResponseDto;
import com.root.root.entity.DailyPlan;
import com.root.root.entity.ExamTask;
import com.root.root.entity.WeeklyPlan;
import com.root.root.repository.ExamTaskRepository;
import com.root.root.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
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

    @DeleteMapping("/tasks/{examTaskId}")
    public ResponseEntity<?> deleteStudyPlan(@PathVariable Long examTaskId, Authentication authentication){
        String loginId = authentication.getName();
        try{
            planService.deleteStudyPlan(loginId, examTaskId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "학습 플랜 삭제 완료");
            return ResponseEntity.ok(response);
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        }
    }

    @GetMapping("/{examTaskId}")
    public ResponseEntity<PlanResponseDto> getPlan(Authentication authentication, @PathVariable Long examTaskId){
        // 토큰에서 로그인 아이디 파싱
        String loginId = authentication.getName();

        // Service에서 엔티티 리스트 조회
        PlanDetailResponseDto detail = planService.getStudyPlan(loginId, examTaskId);

        // 타겟 자격증 이름 get
        String taskName = detail.getWeeklyPlans().get(0).getExamTask().getTaskName();

        // DTO 변환
        PlanResponseDto responseDto = convertToDto(detail.getWeeklyPlans(), examTaskId, taskName);
        responseDto.setStatus(detail.getStatus());

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

    @GetMapping("/tabs")
    public ResponseEntity<List<PlanTabResponseDto>> getPlanTabs(Authentication authentication){
        String loginId = authentication.getName();
        List<PlanTabResponseDto> tabs = planService.getMyPlanTabs(loginId);
        return ResponseEntity.ok(tabs);
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
