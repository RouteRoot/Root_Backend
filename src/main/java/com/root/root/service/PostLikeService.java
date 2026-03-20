package com.root.root.service;

import com.root.root.entity.Post;
import com.root.root.entity.PostLike;
import com.root.root.entity.User;
import com.root.root.repository.PostLikeRepository;
import com.root.root.repository.PostRepository;
import com.root.root.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 좋아요 토글
    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        return postLikeRepository.findByUserIdAndPostId(userId, postId)
                .map(like -> {
                    postLikeRepository.delete(like); // 이미 좋아요 → 취소
                    return false;
                })
                .orElseGet(() -> {
                    postLikeRepository.save(PostLike.builder()
                            .user(user)
                            .post(post)
                            .build()); // 좋아요 추가
                    return true;
                });
    }

    // 좋아요 수 조회
    public int getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    // 내가 좋아요 눌렀는지 확인
    public boolean isLiked(Long userId, Long postId) {
        return postLikeRepository.findByUserIdAndPostId(userId, postId).isPresent();
    }
}