package com.root.root.repository;

import com.root.root.entity.BoardType;
import com.root.root.entity.Post;
import com.root.root.entity.StudyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByBoardType(BoardType boardType);
    List<Post> findByBoardTypeAndStudyStatus(BoardType boardType, StudyStatus studyStatus);
    List<Post> findByCategory(String category);
    List<Post> findByAuthorId(Long userId);
}