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

    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        // 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        // 게시글 존재 여부 검증
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // 이미 좋아요 눌렀으면 취소, 아니면 추가 (토글)
        return postLikeRepository.findByUserIdAndPostId(userId, postId)
                .map(like -> {
                    postLikeRepository.delete(like);
                    return false;
                })
                .orElseGet(() -> {
                    postLikeRepository.save(PostLike.builder()
                            .user(user)
                            .post(post)
                            .build());
                    return true;
                });
    }

    public int getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    public boolean isLiked(Long userId, Long postId) {
        return postLikeRepository.findByUserIdAndPostId(userId, postId).isPresent();
    }
}