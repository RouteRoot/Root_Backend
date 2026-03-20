package com.root.root.service;

import com.root.root.entity.Post;
import com.root.root.entity.PostScrap;
import com.root.root.entity.User;
import com.root.root.repository.PostRepository;
import com.root.root.repository.PostScrapRepository;
import com.root.root.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostScrapService {

    private final PostScrapRepository postScrapRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 스크랩 토글
    @Transactional
    public boolean toggleScrap(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        return postScrapRepository.findByUserIdAndPostId(userId, postId)
                .map(scrap -> {
                    postScrapRepository.delete(scrap); // 이미 스크랩 → 취소
                    return false;
                })
                .orElseGet(() -> {
                    postScrapRepository.save(PostScrap.builder()
                            .user(user)
                            .post(post)
                            .build()); // 스크랩 추가
                    return true;
                });
    }

    // 스크랩 수 조회
    public int getScrapCount(Long postId) {
        return postScrapRepository.countByPostId(postId);
    }

    // 내가 스크랩했는지 확인
    public boolean isScrapped(Long userId, Long postId) {
        return postScrapRepository.findByUserIdAndPostId(userId, postId).isPresent();
    }

    // 내가 스크랩한 글 목록
    public List<Long> getMyScraps(Long userId) {
        return postScrapRepository.findByUserId(userId)
                .stream()
                .map(scrap -> scrap.getPost().getId())
                .collect(Collectors.toList());
    }
}