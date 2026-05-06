package com.root.root.repository;

import com.root.root.entity.BoardType;
import com.root.root.entity.Post;
import com.root.root.entity.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBoardType(BoardType boardType, Pageable pageable);
    List<Post> findByBoardTypeAndStudyStatus(BoardType boardType, StudyStatus studyStatus);
    List<Post> findByCategory(String category);
    List<Post> findByAuthorId(Long userId);
    List<Post> findByBoardTypeIn(List<BoardType> boardTypes);
    List<Post> findByAuthorIdAndBoardTypeIn(Long userId, List<BoardType> boardTypes);

    @Query("SELECT p FROM Post p WHERE " +
            "(:boardType IS NULL OR p.boardType = :boardType) AND " +
            "(p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> searchByKeyword(@Param("boardType") BoardType boardType,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    Page<Post> findByBoardTypeIn(List<BoardType> boardTypes, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
            "p.boardType IN :boardTypes AND " +
            "(p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> searchByKeywordAndBoardTypes(@Param("boardTypes") List<BoardType> boardTypes,
                                            @Param("keyword") String keyword,
                                            Pageable pageable);
}