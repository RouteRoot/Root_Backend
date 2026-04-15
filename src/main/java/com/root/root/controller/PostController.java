package com.root.root.controller;

import com.root.root.dto.PostRequestDto;
import com.root.root.entity.BoardType;
import com.root.root.entity.StudyStatus;
import com.root.root.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    private String getLoginId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 게시글 목록 조회
    // GET /api/posts?boardType=FREE&sort=latest
    // GET /api/posts?boardType=FREE&sort=popular
    @GetMapping
    public ResponseEntity<?> getPosts(@RequestParam(required = false) BoardType boardType,
                                      @RequestParam(defaultValue = "latest") String sort) {
        try {
            return ResponseEntity.ok(postService.getPosts(boardType, sort));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 스터디 모집상태 필터링
    // GET /api/posts/study?status=RECRUITING&sort=popular
    @GetMapping("/study")
    public ResponseEntity<?> getStudyPosts(@RequestParam StudyStatus status,
                                           @RequestParam(defaultValue = "latest") String sort) {
        try {
            return ResponseEntity.ok(postService.getStudyPosts(status, sort));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 인기글 조회
    // GET /api/posts/popular?limit=3
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularPosts(@RequestParam(defaultValue = "5") int limit) {
        try {
            return ResponseEntity.ok(postService.getPopularPosts(limit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 내가 작성한 게시글 목록 조회
    // GET /api/posts/my
    @GetMapping("/my")
    public ResponseEntity<?> getMyPosts() {
        try {
            String loginId = getLoginId();
            return ResponseEntity.ok(postService.getMyPosts(loginId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 게시글 상세 조회
    // GET /api/posts/{postId}
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(postService.getPost(postId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 게시글 작성
    // POST /api/posts
    @PostMapping
    public ResponseEntity<?> createPost(@ModelAttribute PostRequestDto requestDto) {
        try {
            String loginId = getLoginId();
            return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(loginId, requestDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 게시글 수정
    // PUT /api/posts/{postId}
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @ModelAttribute PostRequestDto requestDto) {
        try {
            return ResponseEntity.ok(postService.updatePost(postId, requestDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 게시글 삭제
    // DELETE /api/posts/{postId}
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            postService.deletePost(postId);
            return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}