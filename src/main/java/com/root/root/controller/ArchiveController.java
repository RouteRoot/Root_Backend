package com.root.root.controller;

import com.root.root.dto.PostRequestDto;
import com.root.root.entity.BoardType;
import com.root.root.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/archive")
public class ArchiveController {

    private final PostService postService;

    private String getLoginId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 게시글 목록 조회 (키워드 검색 포함)
    // GET /api/archive
    @GetMapping
    public ResponseEntity<?> getArchivePosts(@RequestParam(required = false) BoardType boardType,
                                             @RequestParam(defaultValue = "latest") String sort,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "6") int size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String category) {
        try {
            return ResponseEntity.ok(postService.getArchivePosts(boardType, sort, page, size, keyword, category));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 인기글 조회
    // GET /api/archive/popular?limit=5
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularPosts(@RequestParam(defaultValue = "5") int limit) {
        try {
            return ResponseEntity.ok(postService.getArchivePopularPosts(limit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 내 게시글 목록 조회
    // GET /api/archive/my
    @GetMapping("/my")
    public ResponseEntity<?> getMyPosts() {
        try {
            String loginId = getLoginId();
            return ResponseEntity.ok(postService.getArchiveMyPosts(loginId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 게시글 상세 조회
    // GET /api/archive/{postId}
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
    // POST /api/archive
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
    // PUT /api/archive/{postId}
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
    // DELETE /api/archive/{postId}
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