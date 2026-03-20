package com.root.root.controller;

import com.root.root.service.PostScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scraps")
public class PostScrapController {

    private final PostScrapService postScrapService;

    // 스크랩 토글
    // POST /api/v1/scraps?userId=1&postId=1
    @PostMapping
    public ResponseEntity<?> toggleScrap(@RequestParam Long userId,
                                         @RequestParam Long postId) {
        try {
            boolean isScrapped = postScrapService.toggleScrap(userId, postId);
            return ResponseEntity.ok(isScrapped ? "스크랩했습니다." : "스크랩을 취소했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 스크랩 수 조회
    // GET /api/v1/scraps/count?postId=1
    @GetMapping("/count")
    public ResponseEntity<?> getScrapCount(@RequestParam Long postId) {
        try {
            return ResponseEntity.ok(postScrapService.getScrapCount(postId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 내가 스크랩했는지 확인
    // GET /api/v1/scraps/check?userId=1&postId=1
    @GetMapping("/check")
    public ResponseEntity<?> isScrapped(@RequestParam Long userId,
                                        @RequestParam Long postId) {
        try {
            return ResponseEntity.ok(postScrapService.isScrapped(userId, postId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 내가 스크랩한 글 목록
    // GET /api/v1/scraps?userId=1
    @GetMapping
    public ResponseEntity<?> getMyScraps(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(postScrapService.getMyScraps(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}