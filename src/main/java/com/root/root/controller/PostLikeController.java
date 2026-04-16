package com.root.root.controller;

import com.root.root.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class PostLikeController {

    private final PostLikeService postLikeService;

    private String getLoginId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 좋아요 토글
    // POST /api/likes?postId={postId}
    @PostMapping
    public ResponseEntity<?> toggleLike(@RequestParam Long postId) {
        try {
            String loginId = getLoginId();
            boolean isLiked = postLikeService.toggleLike(loginId, postId);
            return ResponseEntity.ok(isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 좋아요 수 조회
    // GET /api/likes/count?postId={postId}
    @GetMapping("/count")
    public ResponseEntity<?> getLikeCount(@RequestParam Long postId) {
        try {
            return ResponseEntity.ok(postLikeService.getLikeCount(postId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 좋아요 여부 확인
    // GET /api/likes/check?postId={postId}
    @GetMapping("/check")
    public ResponseEntity<?> isLiked(@RequestParam Long postId) {
        try {
            String loginId = getLoginId();
            return ResponseEntity.ok(postLikeService.isLiked(loginId, postId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 내가 좋아요한 게시글 ID 목록 조회
    // GET /api/likes
    @GetMapping
    public ResponseEntity<?> getMyLikes() {
        try {
            String loginId = getLoginId();
            return ResponseEntity.ok(postLikeService.getMyLikes(loginId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}