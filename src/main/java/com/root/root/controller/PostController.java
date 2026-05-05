package com.root.root.controller;

import com.root.root.dto.PostRequestDto;
import com.root.root.entity.BoardType;
import com.root.root.entity.StudyStatus;
import com.root.root.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping
    public ResponseEntity<?> getPosts(@RequestParam(required = false) BoardType boardType,
                                      @RequestParam(defaultValue = "latest") String sort,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "6") int size,
                                      @RequestParam(required = false) String keyword) {
        try {
            return ResponseEntity.ok(postService.getPosts(boardType, sort, page, size, keyword));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/study")
    public ResponseEntity<?> getStudyPosts(@RequestParam StudyStatus status,
                                           @RequestParam(defaultValue = "latest") String sort) {
        try {
            return ResponseEntity.ok(postService.getStudyPosts(status, sort));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularPosts(@RequestParam(defaultValue = "5") int limit) {
        try {
            return ResponseEntity.ok(postService.getPopularPosts(limit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

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