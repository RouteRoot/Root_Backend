package com.root.root.controller;

import com.root.root.dto.RoadmapRequestDto;
import com.root.root.dto.RoadmapResponseDto;
import com.root.root.entity.Roadmap;
import com.root.root.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {
    private final RoadmapService roadmapService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateRoadmap(@RequestBody RoadmapRequestDto request){
        // 로드맵 생성 및 DB 저장
        Roadmap savedRoadmap = roadmapService.generateAndSaveRoadmap(request);
        // response data
        Map<String, Object> response = new HashMap<>();
        response.put("message", "AI 로드맵이 성공적으로 생성되었습니다!");
        response.put("roadmapId", savedRoadmap.getId());
        // 응답 반환(201)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // 로드맵 Get
    @GetMapping("/{roadmapId}")
    public ResponseEntity<RoadmapResponseDto> getRoadmap(@PathVariable Long roadmapId){
        // JSON 요청
        RoadmapResponseDto response = roadmapService.getRoadmap(roadmapId);
        // JSON 반환(200)
        return ResponseEntity.ok(response);
    }
}
