package com.root.root.service;

import com.root.root.dto.PostRequestDto;
import com.root.root.dto.PostResponseDto;
import com.root.root.entity.*;
import com.root.root.repository.PostRepository;
import com.root.root.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시판 타입별 게시글 목록 조회 (FREE, STUDY, REVIEW)
    @Transactional(readOnly = true)
    public List<PostResponseDto> getPosts(BoardType boardType) {
        return postRepository.findByBoardType(boardType)
                .stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    // 스터디 게시판 내 모집 상태별 필터링 조회 (RECRUITING, CLOSED)
    @Transactional(readOnly = true)
    public List<PostResponseDto> getStudyPosts(StudyStatus studyStatus) {
        return postRepository.findByBoardTypeAndStudyStatus(BoardType.STUDY, studyStatus)
                .stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    @Transactional
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // 조회할 때마다 viewCount 1 증가
        post.incrementViewCount();
        return new PostResponseDto(post);
    }

    // 게시글 작성
    // STUDY 게시판이 아닌 경우 studyStatus는 Post 생성자에서 자동으로 null 처리
    @Transactional
    public PostResponseDto createPost(PostRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
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
}