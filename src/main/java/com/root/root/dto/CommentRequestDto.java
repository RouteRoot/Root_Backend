package com.root.root.dto;

import lombok.Getter;

@Getter
public class CommentRequestDto {
    private String content;
    private Long postId;
}