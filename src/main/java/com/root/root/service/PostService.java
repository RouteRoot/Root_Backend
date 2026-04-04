package com.root.root.service;

import com.root.root.dto.PostRequestDto;
import com.root.root.dto.PostResponseDto;
import com.root.root.entity.*;
import com.root.root.repository.PostImageRepository;
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
    private final PostImageRepository postImageRepository;
    private final FileStorageService fileStorageService;

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
        // 이미지는 최대 5장, form-data로 전송
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

        postRepository.save(post);

        // 이미지가 있으면 로컬 저장 후 PostImage 엔티티로 DB에 저장
        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            List<String> imageUrls = fileStorageService.storeImages(requestDto.getImages());
            for (int i = 0; i < imageUrls.size(); i++) {
                postImageRepository.save(PostImage.builder()
                        .post(post)
                        .imageUrl(imageUrls.get(i))
                        .uploadOrder(i + 1)
                        .build());
            }
        }

        return new PostResponseDto(post);
    }

    // 게시글 수정
    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto requestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        post.update(requestDto.getTitle(), requestDto.getContent(),
                requestDto.getCategory(), requestDto.getStudyStatus());

        // 이미지를 새로 첨부하면 기존 이미지 전체 삭제 후 교체, 첨부하지 않으면 기존 이미지 유지
        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            // 기존 이미지 로컬 파일 및 DB 레코드 삭제
            List<PostImage> existingImages = postImageRepository.findByPostIdOrderByUploadOrder(postId);
            existingImages.forEach(img -> fileStorageService.deleteImage(img.getImageUrl()));
            postImageRepository.deleteAll(existingImages);
            postImageRepository.flush();

            // 새 이미지 저장
            List<String> imageUrls = fileStorageService.storeImages(requestDto.getImages());
            for (int i = 0; i < imageUrls.size(); i++) {
                postImageRepository.save(PostImage.builder()
                        .post(post)
                        .imageUrl(imageUrls.get(i))
                        .uploadOrder(i + 1)
                        .build());
            }
        }

        return new PostResponseDto(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // 로컬 이미지 파일 먼저 삭제
        postImageRepository.findByPostIdOrderByUploadOrder(postId)
                .forEach(img -> fileStorageService.deleteImage(img.getImageUrl()));

        // PostImage DB 레코드는 cascade 설정으로 자동 삭제
        postRepository.delete(post);
    }
}