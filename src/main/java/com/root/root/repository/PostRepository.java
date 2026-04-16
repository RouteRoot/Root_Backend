package com.root.root.repository;

import com.root.root.entity.BoardType;
import com.root.root.entity.Post;
import com.root.root.entity.StudyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBoardType(BoardType boardType, Pageable pageable);
    List<Post> findByBoardTypeAndStudyStatus(BoardType boardType, StudyStatus studyStatus);
    List<Post> findByCategory(String category);
    List<Post> findByAuthorId(Long userId);
}