package com.root.root.service;

import com.root.root.dto.PostRequestDto;
import com.root.root.dto.PostResponseDto;
import com.root.root.entity.BoardType;
import com.root.root.entity.Post;
import com.root.root.entity.StudyStatus;
import com.root.root.entity.User;
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

    public List<PostResponseDto> getPosts(BoardType boardType) {
        return postRepository.findByBoardType(boardType)
                .stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<PostResponseDto> getStudyPosts(StudyStatus studyStatus) {
        // boardType을 STUDY로 고정하고 모집 상태로 필터링
        return postRepository.findByBoardTypeAndStudyStatus(BoardType.STUDY, studyStatus)
                .stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponseDto getPost(Long postId) {
        // 게시글 존재 여부 검증
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // 조회수 증가
        post.incrementViewCount();
        return new PostResponseDto(post);
    }

    @Transactional
    public PostResponseDto createPost(PostRequestDto requestDto) {
        // 유저 검증
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        // STUDY 게시판이 아닐 경우 studyStatus는 null로 처리 (Post 생성자에서 자동 처리)
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

    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto requestDto) {
        // 게시글 존재 여부 검증
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // STUDY 게시판이 아닐 경우 studyStatus 수정 무시 (Post.update()에서 자동 처리)
        post.update(requestDto.getTitle(), requestDto.getContent(),
                requestDto.getCategory(), requestDto.getStudyStatus());
        return new PostResponseDto(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        // 게시글 존재 여부 검증
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        postRepository.delete(post);
    }
}