package com.root.root.repository;

import com.root.root.entity.PostScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {

    // 유저가 해당 게시글 스크랩했는지 확인
    Optional<PostScrap> findByUserIdAndPostId(Long userId, Long postId);

    // 해당 게시글 스크랩 수
    int countByPostId(Long postId);

    // 유저가 스크랩한 글 목록
    List<PostScrap> findByUserId(Long userId);
}