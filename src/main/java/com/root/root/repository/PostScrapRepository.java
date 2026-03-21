package com.root.root.repository;

import com.root.root.entity.PostScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {
    Optional<PostScrap> findByUserIdAndPostId(Long userId, Long postId);
    int countByPostId(Long postId);
    List<PostScrap> findByUserId(Long userId);
}