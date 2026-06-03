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

    @Transactional
    public boolean toggleLike(String loginId, Long postId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        return postLikeRepository.findByUserIdAndPostId(user.getId(), postId)
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

    public boolean isLiked(String loginId, Long postId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        return postLikeRepository.findByUserIdAndPostId(user.getId(), postId).isPresent();
    }

    public List<Long> getMyLikes(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        return postLikeRepository.findByUserId(user.getId())
                .stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toList());
    }
}