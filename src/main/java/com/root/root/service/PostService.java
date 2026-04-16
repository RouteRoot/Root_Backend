package com.root.root.service;

import com.root.root.dto.PostRequestDto;
import com.root.root.dto.PostResponseDto;
import com.root.root.entity.*;
import com.root.root.repository.PostRepository;
import com.root.root.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPosts(BoardType boardType, String sort, int page, int size) {
        Sort sorting = "popular".equalsIgnoreCase(sort)
                ? Sort.by("viewCount").descending()
                : Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sorting);

        if (boardType == null) {
            return postRepository.findAll(pageable).map(PostResponseDto::new);
        }
        return postRepository.findByBoardType(boardType, pageable).map(PostResponseDto::new);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getStudyPosts(StudyStatus studyStatus, String sort) {
        List<Post> posts = postRepository.findByBoardTypeAndStudyStatus(BoardType.STUDY, studyStatus);
        return sortAndMap(posts, sort);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getPopularPosts(int limit) {
        return postRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(Post::getViewCount).reversed())
                .limit(limit)
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        post.incrementViewCount();
        return new PostResponseDto(post);
    }

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

    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto requestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        post.update(requestDto.getTitle(), requestDto.getContent(),
                requestDto.getCategory(), requestDto.getStudyStatus());
        return new PostResponseDto(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getMyPosts(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));
        return postRepository.findByAuthorId(user.getId())
                .stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    private List<PostResponseDto> sortAndMap(List<Post> posts, String sort) {
        Comparator<Post> comparator = "popular".equalsIgnoreCase(sort)
                ? Comparator.comparingInt(Post::getViewCount).reversed()
                : Comparator.comparing(Post::getCreatedAt).reversed();
        return posts.stream()
                .sorted(comparator)
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }
}