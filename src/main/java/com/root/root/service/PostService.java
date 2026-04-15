package com.root.root.service;

import com.root.root.dto.PostRequestDto;
import com.root.root.dto.PostResponseDto;
import com.root.root.entity.*;
import com.root.root.repository.PostRepository;
import com.root.root.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시판 타입별 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostResponseDto> getPosts(BoardType boardType, String sort) {
        List<Post> posts = postRepository.findByBoardType(boardType);
        return sortAndMap(posts, sort);
    }

    // 스터디 게시판 내 모집 상태별 필터링 조회
    @Transactional(readOnly = true)
    public List<PostResponseDto> getStudyPosts(StudyStatus studyStatus, String sort) {
        List<Post> posts = postRepository.findByBoardTypeAndStudyStatus(BoardType.STUDY, studyStatus);
        return sortAndMap(posts, sort);
    }

    // 인기글 조회
    @Transactional(readOnly = true)
    public List<PostResponseDto> getPopularPosts(int limit) {
        return postRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(
                        (Post p) -> p.getPostLikes().size()).reversed())
                .limit(limit)
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    @Transactional
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        post.incrementViewCount();
        return new PostResponseDto(post);
    }

    // 게시글 작성
    @Transactional
    public PostResponseDto createPost(String loginId, PostRequestDto requestDto) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        Post post = Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .author(user)
                .boardType(requestDto.getBoardType())
                .category(requestDto.getCategory())
                .studyStatus(requestDto.getStudyStatus())
                .build();

        return new PostResponseDto(postRepository.save(post));
    }

    // 게시글 수정
    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto requestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        post.update(requestDto.getTitle(), requestDto.getContent(),
                requestDto.getCategory(), requestDto.getStudyStatus());

        return new PostResponseDto(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        postRepository.delete(post);
    }

    // 내가 작성한 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostResponseDto> getMyPosts(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        return postRepository.findByAuthorId(user.getId())
                .stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    // 정렬 공통 처리 (인기순/최신순)
    private List<PostResponseDto> sortAndMap(List<Post> posts, String sort) {
        Comparator<Post> comparator = "popular".equalsIgnoreCase(sort)
                ? Comparator.comparingInt((Post p) -> p.getPostLikes().size()).reversed()
                : Comparator.comparing(Post::getCreatedAt).reversed();

        return posts.stream()
                .sorted(comparator)
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }
}