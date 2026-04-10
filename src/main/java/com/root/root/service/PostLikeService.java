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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 좋아요 토글 (추가/취소)
    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

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

    // 좋아요 수 조회
    public int getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    // 좋아요 여부 확인
    public boolean isLiked(Long userId, Long postId) {
        return postLikeRepository.findByUserIdAndPostId(userId, postId).isPresent();
    }

    // 내가 좋아요한 게시글 ID 목록 조회
    public List<Long> getMyLikes(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        return postLikeRepository.findByUserId(userId)
                .stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toList());
    }
}