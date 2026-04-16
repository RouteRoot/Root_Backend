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

    // 스크랩 토글 (추가/취소)
    @Transactional
    public boolean toggleScrap(String loginId, Long postId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        return postScrapRepository.findByUserIdAndPostId(user.getId(), postId)
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

    // 스크랩 수 조회
    public int getScrapCount(Long postId) {
        return postScrapRepository.countByPostId(postId);
    }

    // 스크랩 여부 확인
    public boolean isScrapped(String loginId, Long postId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        return postScrapRepository.findByUserIdAndPostId(user.getId(), postId).isPresent();
    }

    // 내가 스크랩한 게시글 ID 목록 조회
    public List<Long> getMyScraps(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        return postScrapRepository.findByUserId(user.getId())
                .stream()
                .map(scrap -> scrap.getPost().getId())
                .collect(Collectors.toList());
    }
}