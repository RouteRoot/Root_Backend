package com.root.root.dto;

import com.root.root.entity.BoardType;
import com.root.root.entity.Post;
import com.root.root.entity.StudyStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponseDto {

    private Long postId;
    private String title;
    private String content;
    private String author;
    private BoardType boardType;
    private String category;
    private StudyStatus studyStatus;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;

    public PostResponseDto(Post post) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.author = post.getAuthor().getNickname();
        this.boardType = post.getBoardType();
        this.category = post.getCategory();
        this.studyStatus = post.getStudyStatus();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getPostLikes().size();
        this.commentCount = post.getComments().size();
        this.createdAt = post.getCreatedAt();
    }
}