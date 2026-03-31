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

    @Transactional
    public boolean toggleScrap(Long userId, Long postId) {
        // 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        // 게시글 존재 여부 검증
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // 이미 스크랩했으면 취소, 아니면 추가 (토글)
        return postScrapRepository.findByUserIdAndPostId(userId, postId)
                .map(scrap -> {
                    postScrapRepository.delete(scrap);
                    return false;
                })
                .orElseGet(() -> {
                    postScrapRepository.save(PostScrap.builder()
                            .user(user)
                            .post(post)
                            .build());
                    return true;
                });
    }

    public int getScrapCount(Long postId) {
        return postScrapRepository.countByPostId(postId);
    }

    public boolean isScrapped(Long userId, Long postId) {
        return postScrapRepository.findByUserIdAndPostId(userId, postId).isPresent();
    }

    public List<Long> getMyScraps(Long userId) {
        // 스크랩한 게시글의 ID 목록만 반환
        return postScrapRepository.findByUserId(userId)
                .stream()
                .map(scrap -> scrap.getPost().getId())
                .collect(Collectors.toList());
    }
}