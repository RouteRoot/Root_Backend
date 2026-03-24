package com.root.root.controller;

import com.root.root.dto.DashboardResponseDto;
import com.root.root.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDto> getDashboard(Authentication authentication){
        String loginId = authentication.getName();
        DashboardResponseDto response = dashboardService.getDashboardData(loginId);

        return ResponseEntity.ok(response);
    }
}
