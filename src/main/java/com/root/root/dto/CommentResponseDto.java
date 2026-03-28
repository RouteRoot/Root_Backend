package com.root.root.dto;

import com.root.root.entity.Comment;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {

    private Long commentId;
    private String content;
    private String author;
    private Long postId;
    private LocalDateTime createdAt;

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.author = comment.getAuthor().getNickname();
        this.postId = comment.getPost().getId();
        this.createdAt = comment.getCreatedAt();
    }
}