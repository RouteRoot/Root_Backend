package com.root.root.controller;

import com.root.root.service.PostScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scraps")
public class PostScrapController {

    private final PostScrapService postScrapService;

    // 스크랩 토글
    // POST /api/scraps?userId={userId}&postId={postId}
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
    // GET /api/scraps/count?postId={postId}
    @GetMapping("/count")
    public ResponseEntity<?> getScrapCount(@RequestParam Long postId) {
        try {
            return ResponseEntity.ok(postScrapService.getScrapCount(postId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 내가 스크랩했는지 확인
    // GET /api/scraps/check?userId={userId}&postId={postId}
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
    // GET /api/scraps?userId={userId}
    @GetMapping
    public ResponseEntity<?> getMyScraps(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(postScrapService.getMyScraps(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}